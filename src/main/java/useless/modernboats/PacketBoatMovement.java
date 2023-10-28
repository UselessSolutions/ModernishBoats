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
	public double xPos;
	public double zPos;
	public PacketBoatMovement(){

	}
	public PacketBoatMovement(float boatYRot, double velocity, double xPos, double zPos){
		this.boatYRot = boatYRot;
		this.velocity = velocity;
		this.xPos = xPos;
		this.zPos = zPos;
	}
	@Override
	public void readPacketData(DataInputStream dataInputStream) throws IOException {
		this.boatYRot = dataInputStream.readFloat();
		this.velocity = dataInputStream.readDouble();
		this.xPos = dataInputStream.readDouble();
		this.zPos = dataInputStream.readDouble();
	}

	@Override
	public void writePacketData(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeFloat(boatYRot);
		dataOutputStream.writeDouble(velocity);
		dataOutputStream.writeDouble(xPos);
		dataOutputStream.writeDouble(zPos);
	}

	@Override
	public void processPacket(NetHandler netHandler) {
		((ICustomPackets)netHandler).handleBoatMovement(this);
	}

	@Override
	public int getPacketSize() {
		return 28;
	}
}
