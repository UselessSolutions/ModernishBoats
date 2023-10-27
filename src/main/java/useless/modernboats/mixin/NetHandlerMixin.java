package useless.modernboats.mixin;

import net.minecraft.core.net.handler.NetHandler;
import net.minecraft.core.net.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import useless.modernboats.PacketBoatMovement;
import useless.modernboats.interfaces.ICustomPackets;

@Mixin(value = NetHandler.class, remap = false)
public abstract class NetHandlerMixin implements ICustomPackets {
	@Shadow
	public abstract void handleInvalidPacket(Packet packet);

	@Override
	public void handleBoatMovement(PacketBoatMovement packet) {
		handleInvalidPacket(packet);
	}
}
