package useless.modernboats.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import useless.modernboats.interfaces.IBoatExtras;

import java.util.List;
@Environment(EnvType.SERVER)
@Mixin(value = EntityBoat.class, remap = false)
public abstract class EntityBoatMixinServer extends Entity implements IBoatExtras {
	@Shadow
	public int boatTimeSinceHit;
	@Shadow
	public int boatCurrentDamage;
	@Unique
	private final double maxSpeed = 0.8;
	@Unique
	private final double backwardsMaxSpeed = 0.4;

	public EntityBoatMixinServer(World world) {
		super(world);
	}
	@Unique
	public void modernishBoats$setBoatControls(float yRot, double velocity){
		setRot(yRot, this.xRot);
		velocity = bindToRange(velocity, -maxSpeed, backwardsMaxSpeed);

		double uX = Math.cos(Math.toRadians(this.yRot));
		double uZ = Math.sin(Math.toRadians(this.yRot));

		this.xd = uX * velocity;
		this.zd = uZ * velocity;

	}
	@Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
	public void tickCustom(CallbackInfo ci) {
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
			double uX = Math.cos(Math.toRadians(this.yRot));
			double uZ = Math.sin(Math.toRadians(this.yRot));
			int particleAmount = (int) (1.0 + horizontalSpeed * 60.0);
			for (int i = 0; i < particleAmount; i++) {
				double particleDistance = this.random.nextFloat() * 2.0f - 1.0f;
				double d20 = (double)(this.random.nextInt(2) * 2 - 1) * 0.7;
				if (this.random.nextBoolean()) {
					double particleX = this.x - uX * particleDistance * 0.8 + uZ * d20;
					double particleZ = this.z - uZ * particleDistance * 0.8 - uX * d20;
					this.world.spawnParticle("splash", particleX, this.y - 0.125, particleZ, this.xd, this.yd, this.zd);
				} else {
					double particleX = this.x + uX + uZ * particleDistance * 0.7;
					double particleZ = this.z + uZ - uX * particleDistance * 0.7;
					this.world.spawnParticle("splash", particleX, this.y - 0.125, particleZ, this.xd, this.yd, this.zd);
				}
			}
		}

		this.xd *= 0.99f;
		this.yd *= 0.95f;
		this.zd *= 0.99f;

		List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.bb.expand(0.2f, 0.0, 0.2f));
		if (list != null && !list.isEmpty()) {
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
}
