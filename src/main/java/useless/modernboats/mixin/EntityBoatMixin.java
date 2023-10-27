package useless.modernboats.mixin;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.vehicle.EntityBoat;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.util.phys.AABB;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = EntityBoat.class, remap = false)
public abstract class EntityBoatMixin extends Entity {
	@Shadow
	public int boatTimeSinceHit;

	@Shadow
	public int boatCurrentDamage;

	@Shadow
	private int field_9394_d;

	@Shadow
	private double field_9393_e;

	@Shadow
	private double field_9392_f;

	@Shadow
	private double field_9391_g;

	@Shadow
	private double field_9390_h;

	@Shadow
	private double boatPitch;
	@Unique
	private final double maxSpeed = 0.8;

	public EntityBoatMixin(World world) {
		super(world);
	}
	@Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
	public void tickCustom(CallbackInfo ci) {
		double deltaYRot;
		super.tick();

		// Tick Attack Timers
		if (this.boatTimeSinceHit > 0) {
			--this.boatTimeSinceHit;
		}
		if (this.boatCurrentDamage > 0) {
			--this.boatCurrentDamage;
		}

		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;

		double percentageSubmerged = getPercentageSubmerged(5);

		if (world.isClientSide) {
			clientSideTick();
			ci.cancel();
			return;
		}

		// buoyancy calculations
		if (percentageSubmerged < 1.0) {
			double d3 = percentageSubmerged * 2.0 - 1.0;
			this.yd += (double)0.04f * d3;
		} else {
			if (this.yd < 0.0) {
				this.yd /= 2.0;
			}
			this.yd += 0.007f;
		}

		// Default boat controls
		if (this.passenger != null) {
			this.xd += this.passenger.xd * 0.4;
			this.zd += this.passenger.zd * 0.4;
		}

		// Cap speed
		this.xd = bindToRange(this.xd, -maxSpeed, maxSpeed);
		this.zd = bindToRange(this.zd, -maxSpeed, maxSpeed);

		// Slow down speed on the ground
		if (this.onGround) {
			this.xd *= 0.5;
			this.yd *= 0.5;
			this.zd *= 0.5;
		}

		this.move(this.xd, this.yd, this.zd);

		// Splash Effects
		double horizontalSpeed = Math.sqrt(this.xd * this.xd + this.zd * this.zd);
		if (horizontalSpeed > 0.15) {
			double d12 = Math.cos(Math.toRadians(this.yRot));
			double d15 = Math.sin(Math.toRadians(this.yRot));
			int particleAmount = (int) (1.0 + horizontalSpeed * 60.0);
			for (int i = 0; i < particleAmount; i++) {
				double d18 = this.random.nextFloat() * 2.0f - 1.0f;
				double d20 = (double)(this.random.nextInt(2) * 2 - 1) * 0.7;
				if (this.random.nextBoolean()) {
					double particleX = this.x - d12 * d18 * 0.8 + d15 * d20;
					double particleZ = this.z - d15 * d18 * 0.8 - d12 * d20;
					this.world.spawnParticle("splash", particleX, this.y - 0.125, particleZ, this.xd, this.yd, this.zd);
				} else {
					double particleX = this.x + d12 + d15 * d18 * 0.7;
					double particleZ = this.z + d15 - d12 * d18 * 0.7;
					this.world.spawnParticle("splash", particleX, this.y - 0.125, particleZ, this.xd, this.yd, this.zd);
				}
			}
		}

		this.xd *= 0.99f;
		this.yd *= 0.95f;
		this.zd *= 0.99f;
		this.xRot = 0.0f;
		double newYRot = this.yRot;
		double deltaX = this.xo - this.x;
		double deltaZ = this.zo - this.z;
		if (deltaX * deltaX + deltaZ * deltaZ > 0.001) {
			newYRot = (float)(Math.toDegrees(Math.atan2(deltaZ, deltaX)));
		}

		deltaYRot = newYRot - (double)this.yRot;
		deltaYRot = constrainAngle(deltaYRot);
		deltaYRot = bindToRange(deltaYRot, -20, 20); // Restrict angle rate of change??
		this.yRot = (float)((double)this.yRot + deltaYRot);
		this.setRot(this.yRot, this.xRot);

		List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.bb.expand(0.2f, 0.0, 0.2f));
		if (list != null && list.size() > 0) {
			for (Entity entity : list) {
				if (entity == this.passenger || !entity.isPushable() || !(entity instanceof EntityBoat)) continue;
				entity.push(this);
			}
		}

		// Break Snow
		for (int i = 0; i < 4; ++i) {
			int blockX = MathHelper.floor_double(this.x + ((double)(i % 2) - 0.5) * 0.8);
			int blockY = MathHelper.floor_double(this.y);
			int blockZ = MathHelper.floor_double(this.z + ((double)(i / 2) - 0.5) * 0.8);
			if (this.world.getBlockId(blockX, blockY, blockZ) != Block.layerSnow.id) continue;
			this.world.setBlockWithNotify(blockX, blockY, blockZ, 0);
		}

		if (this.passenger != null && this.passenger.removed) {
			this.passenger = null;
		}
		ci.cancel();
	}
	@Unique
	public double constrainAngle(double angle){
		while (angle >= 180){
			angle -= 360;
		}
		while (angle < -180){
			angle += 360;
		}
		return angle;
	}
	@Unique
	public double getPercentageSubmerged(int sliceAmount){
		double percentageSubmerged = 0d;
		for (int i = 0; i < sliceAmount; ++i) {
			double sliceBottom = this.bb.minY + (this.bb.maxY - this.bb.minY) * (double)i / (double)sliceAmount - 0.125;
			double sliceTop = this.bb.minY + (this.bb.maxY - this.bb.minY) * (double)(i + 1) / (double)sliceAmount - 0.125;
			AABB axisalignedbb = AABB.getBoundingBoxFromPool(this.bb.minX, sliceBottom, this.bb.minZ, this.bb.maxX, sliceTop, this.bb.maxZ);
			if (!this.world.isAABBInMaterial(axisalignedbb, Material.water)) continue;
			percentageSubmerged += 1.0 / (double)sliceAmount;
		}
		return percentageSubmerged;
	}
	@Unique
	public double bindToRange(double input, double minVal, double maxVal){
		return Math.min(Math.max(input, minVal), maxVal);
	}
	@Unique
	public void clientSideTick(){
		if (this.field_9394_d > 0) {
			double newX = this.x + (this.field_9393_e - this.x) / (double)this.field_9394_d;
			double newY = this.y + (this.field_9392_f - this.y) / (double)this.field_9394_d;
			double newZ = this.z + (this.field_9391_g - this.z) / (double)this.field_9394_d;
			double deltaYRot = this.field_9390_h - (double)this.yRot;
			deltaYRot = constrainAngle(deltaYRot);
			this.yRot = (float)((double)this.yRot + deltaYRot / (double)this.field_9394_d);
			this.xRot = (float)((double)this.xRot + (this.boatPitch - (double)this.xRot) / (double)this.field_9394_d);
			--this.field_9394_d;
			this.setPos(newX, newY, newZ);
			this.setRot(this.yRot, this.xRot);
		} else {
			this.setPos(this.x + this.xd, this.y + this.yd, this.z + this.zd);
			if (this.onGround) {
				this.xd *= 0.5;
				this.yd *= 0.5;
				this.zd *= 0.5;
			}
			this.xd *= 0.99f;
			this.yd *= 0.95f;
			this.zd *= 0.99f;
		}
	}
}
