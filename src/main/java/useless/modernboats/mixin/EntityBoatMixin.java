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

	public EntityBoatMixin(World world) {
		super(world);
	}
	@Inject(method = "tick()V", at = @At("HEAD"), cancellable = true)
	public void tickCustom(CallbackInfo ci) {
		double d19;
		double maxSpeed = 0.8;
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

		if (this.world.isClientSide) {
			clientSideTick();
			return;
		}

		if (percentageSubmerged < 1.0) {
			double d3 = percentageSubmerged * 2.0 - 1.0;
			this.yd += (double)0.04f * d3;
		} else {
			if (this.yd < 0.0) {
				this.yd /= 2.0;
			}
			this.yd += 0.007f;
		}

		if (this.passenger != null) {
			this.xd += this.passenger.xd * 0.4;
			this.zd += this.passenger.zd * 0.4;
		}

		// Cap speed
		this.xd = bindToRange(this.xd, -maxSpeed, maxSpeed);
		this.zd = bindToRange(this.zd, -maxSpeed, maxSpeed);

		if (this.onGround) {
			this.xd *= 0.5;
			this.yd *= 0.5;
			this.zd *= 0.5;
		}
		this.move(this.xd, this.yd, this.zd);
		double d8 = Math.sqrt(this.xd * this.xd + this.zd * this.zd);
		if (d8 > 0.15) {
			double d12 = Math.cos((double)this.yRot * Math.PI / 180.0);
			double d15 = Math.sin((double)this.yRot * Math.PI / 180.0);
			int i1 = 0;
			while ((double)i1 < 1.0 + d8 * 60.0) {
				double d18 = this.random.nextFloat() * 2.0f - 1.0f;
				double d20 = (double)(this.random.nextInt(2) * 2 - 1) * 0.7;
				if (this.random.nextBoolean()) {
					double d21 = this.x - d12 * d18 * 0.8 + d15 * d20;
					double d23 = this.z - d15 * d18 * 0.8 - d12 * d20;
					this.world.spawnParticle("splash", d21, this.y - 0.125, d23, this.xd, this.yd, this.zd);
				} else {
					double d22 = this.x + d12 + d15 * d18 * 0.7;
					double d24 = this.z + d15 - d12 * d18 * 0.7;
					this.world.spawnParticle("splash", d22, this.y - 0.125, d24, this.xd, this.yd, this.zd);
				}
				++i1;
			}
		}
		this.xd *= 0.99f;
		this.yd *= 0.95f;
		this.zd *= 0.99f;
		this.xRot = 0.0f;
		double d13 = this.yRot;
		double d16 = this.xo - this.x;
		double d17 = this.zo - this.z;
		if (d16 * d16 + d17 * d17 > 0.001) {
			d13 = (float)(Math.atan2(d17, d16) * 180.0 / Math.PI);
		}

		d19 = d13 - (double)this.yRot;
		d19 = constrainAngle(d19);
		d19 = bindToRange(d19, -20, 20); // Restrict angle rate of change??
		this.yRot = (float)((double)this.yRot + d19);
		this.setRot(this.yRot, this.xRot);

		List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.bb.expand(0.2f, 0.0, 0.2f));
		if (list != null && list.size() > 0) {
			for (Entity entity : list) {
				if (entity == this.passenger || !entity.isPushable() || !(entity instanceof EntityBoat)) continue;
				entity.push(this);
			}
		}
		for (int k1 = 0; k1 < 4; ++k1) {
			int j2;
			int i2;
			int l1 = MathHelper.floor_double(this.x + ((double)(k1 % 2) - 0.5) * 0.8);
			if (this.world.getBlockId(l1, i2 = MathHelper.floor_double(this.y), j2 = MathHelper.floor_double(this.z + ((double)(k1 / 2) - 0.5) * 0.8)) != Block.layerSnow.id) continue;
			this.world.setBlockWithNotify(l1, i2, j2, 0);
		}
		if (this.passenger != null && this.passenger.removed) {
			this.passenger = null;
		}
		ci.cancel();
	}
	@Unique
	public double constrainAngle(double angle){
		if (angle >= -180 && angle < 180){
			return angle;
		}
		return ((angle - 180.0)%360)-180.0;
	}
	@Unique
	public double getPercentageSubmerged(int sliceAmount){
		double percentageSubmerged = 0d;
		for (int j = 0; j < sliceAmount; ++j) {
			double sliceBottom = this.bb.minY + (this.bb.maxY - this.bb.minY) * (double)j / (double)sliceAmount - 0.125;
			double sliceTop = this.bb.minY + (this.bb.maxY - this.bb.minY) * (double)(j + 1) / (double)sliceAmount - 0.125;
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
			double d14;
			double d1 = this.x + (this.field_9393_e - this.x) / (double)this.field_9394_d;
			double d6 = this.y + (this.field_9392_f - this.y) / (double)this.field_9394_d;
			double d10 = this.z + (this.field_9391_g - this.z) / (double)this.field_9394_d;
			d14 = this.field_9390_h - (double)this.yRot;
			d14 = constrainAngle(d14);
			this.yRot = (float)((double)this.yRot + d14 / (double)this.field_9394_d);
			this.xRot = (float)((double)this.xRot + (this.boatPitch - (double)this.xRot) / (double)this.field_9394_d);
			--this.field_9394_d;
			this.setPos(d1, d6, d10);
			this.setRot(this.yRot, this.xRot);
		} else {
			double d2 = this.x + this.xd;
			double d7 = this.y + this.yd;
			double d11 = this.z + this.zd;
			this.setPos(d2, d7, d11);
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
