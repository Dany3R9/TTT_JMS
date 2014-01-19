package TTT;

public class RoomMessage extends MoveMessage{
private static final long serialVersionUID = 1968426744483152937L;
	
	private String name;
	private String user;

	public RoomMessage(String name, String user) {
		this.name = name;
		this.user = user;
		this.type = MessageT.ROOM;
	}

	public String getRoom() {
		return this.name;
	}

	public String getUser() {
		return this.user.toString();
	}

	

}
