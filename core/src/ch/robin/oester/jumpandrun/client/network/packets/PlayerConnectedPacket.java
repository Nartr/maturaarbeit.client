package ch.robin.oester.jumpandrun.client.network.packets;

import java.util.Map;

public class PlayerConnectedPacket {
	public String newPlayer;
	public int newPlayerColor;
	public Map<String, Integer> players;												//only used if packet is sent to new player
}
