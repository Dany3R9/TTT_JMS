package TTT;

public class WinMessage extends MoveMessage{
	private static final long serialVersionUID = 3556333835790793593L;

	private String name;
	private String room;

	public WinMessage(String name, String item) {
		this.name = name;
		this.room = item;
		this.type = MessageT.WINNER;
	}

	public String getName() {
		return this.name;
	}

	public String getRoom() {
		return this.room;
	}

}
