package useless.modernboats.mixin;

import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.vehicle.EntityBoat;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(value = EntityBoat.class, remap = false)
public abstract class EntityBoatMixin extends Entity {
	public EntityBoatMixin(World world) {
		super(world);
	}
	@Inject(method = "tick()V", at = @At("HEAD"),cancellable = true)
	public void tickCustom() {

	}
}
