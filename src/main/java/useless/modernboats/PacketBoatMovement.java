package useless.modernboats;

import net.minecraft.core.net.handler.NetHandler;
import net.minecraft.core.net.packet.Packet;
import useless.modernboats.interfaces.ICustomPackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketBoatMovement extends Packet {
	public float boatYRot;
	public double velocity;
	public PacketBoatMovement(){

	}
	public PacketBoatMovement(float boatYRot, double velocity){
		this.boatYRot = boatYRot;
		this.velocity = velocity;
	}
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.boatYRot = dataInputStream.readFloat();
		this.velocity = dataInputStream.readDouble();
	}

	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeFloat(boatYRot);
		dataOutputStream.writeDouble(velocity);
	}

	@Override
	public void processPacket(NetHandler netHandler) {
		((ICustomPackets)netHandler).handleBoatMovement(this);
	}

	@Override
	public int getPacketSize() {
		return 12;
	}
}
