package useless.modernboats.mixin;

import net.minecraft.core.entity.vehicle.EntityBoat;
import net.minecraft.server.entity.player.EntityPlayerMP;
import net.minecraft.server.net.handler.NetServerHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import useless.modernboats.PacketBoatMovement;
import useless.modernboats.interfaces.IBoatExtras;
import useless.modernboats.interfaces.ICustomPackets;
@Mixin(value = NetServerHandler.class, remap = false)
public abstract class NetServerHandlerMixin implements ICustomPackets {
	@Shadow
	private EntityPlayerMP playerEntity;

	@Override
	public void handleBoatMovement(PacketBoatMovement packet) {
		if (playerEntity.vehicle != null && playerEntity.vehicle instanceof EntityBoat){
			IBoatExtras boat = (IBoatExtras) playerEntity.vehicle;
			boat.setBoatControls(packet.boatYRot, packet.velocity);
		}
	}
}
