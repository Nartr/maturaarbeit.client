package ch.robin.oester.jumpandrun.client.network.packets;

public class Reason {																	//static parameters to identify log in state
	public static final int VALID = 0;
	public static final int INVALID_USERNAME = 1;
	public static final int WRONG_PASWORD = 2;
	public static final int USER_EXIST = 3;
	public static final int TOO_MANY_PLAYERS = 4;
	public static final int ALREADY_LOGGED_IN = 5;
}
