package TTT;

//*That's the implementation for the messages containing the move performed by every player in the TTT game
public class StepMessage extends MoveMessage {
	private static final long serialVersionUID = 1968425744483152937L;
	
	private int[] move;
	private String queue;
	private String user;

	public StepMessage(String name, String item, int[] bid) {
		this.user = name;
		this.queue = item;
		this.move = bid;
		this.type = MessageT.STEP;
	}

	public String getUser() {
		return this.user;
	}

	public int[] getMove() {
		return this.move;
	}

	public String getQueue() {
		return this.queue;
	}

}

