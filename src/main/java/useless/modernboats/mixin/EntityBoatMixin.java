package useless.modernboats.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.EntityPlayerSP;
import net.minecraft.client.input.Input;
import net.minecraft.core.Global;
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
import useless.modernboats.PacketBoatMovement;
import useless.modernboats.interfaces.IBoatExtras;

import java.util.List;

@Mixin(value = EntityBoat.class, remap = false)
public abstract class EntityBoatMixin extends Entity implements IBoatExtras {
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
	@Unique
	private final double backwardsMaxSpeed = 0.4;
	@Unique
	private double velocity = 0;
	@Unique
	private final double accelerationForwards = 0.005;
	@Unique
	private final double accelerationBackwards = accelerationForwards/2;
	@Unique
	private final float maxRotationSpeed = 8;
	@Unique
	private float rotationVelocity = 0;
	@Unique
	private final float rotationAcceleration = 4;

	public EntityBoatMixin(World world) {
		super(world);
	}
	@Unique
	public void setBoatControls(float yRot, double velocity){
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

		// Default boat controls
		if (this.passenger != null && passenger instanceof EntityPlayerSP && !Global.isServer) {
			Input passangerInput = ((EntityPlayerSP)passenger).input;
			velocity = bindToRange(velocity, -maxSpeed, backwardsMaxSpeed);

			if (Math.abs(passangerInput.moveStrafe) > 0.1f){
				rotationVelocity -= rotationAcceleration * passangerInput.moveStrafe;
				velocity *= .95;
			} else {
				rotationVelocity *= 0.5;
				if (Math.abs(rotationVelocity) < 0.5){
					rotationVelocity = 0;
				}
			}

			rotationVelocity = (float) bindToRange(rotationVelocity, -maxRotationSpeed, maxRotationSpeed);

			float newAngle = yRot + rotationVelocity;


			if (passangerInput.moveForward > 0.1f){
				velocity += -passangerInput.moveForward * accelerationForwards;
			} else if (passangerInput.moveForward < -0.1f){
				velocity += -passangerInput.moveForward * accelerationBackwards;
			} else {
				velocity *= 0.8;
				if (Math.abs(velocity) < 0.005){
					velocity = 0;
				}
			}

			setBoatControls(newAngle, velocity);
			if (world.isClientSide){
				Minecraft.getMinecraft(Minecraft.class).getSendQueue().addToSendQueue(new PacketBoatMovement(newAngle, velocity));
			}
		}

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
			--this.field_9394_d;
			this.setPos(newX, newY, newZ);
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
