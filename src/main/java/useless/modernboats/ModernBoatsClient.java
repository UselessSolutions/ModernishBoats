package useless.modernboats;

import net.minecraft.client.Minecraft;
import net.minecraft.core.net.packet.Packet;

public class ModernBoatsClient {
	public static Minecraft minecraft = Minecraft.getMinecraft(Minecraft.class);
	public static void addToSendQueue(Packet packet){
		minecraft.getSendQueue().addToSendQueue(packet);
	}
	public static void init(){}
}
