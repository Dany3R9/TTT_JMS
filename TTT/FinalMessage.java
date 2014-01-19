package TTT;

//Final message for WIN/LOSE or equal
public class FinalMessage extends MoveMessage {
	private static final long serialVersionUID = 1871448985768110291L;

	private String queue;

	public FinalMessage(String item) {
		this.queue = item;
		this.type = MessageT.CLOSING;
	}

	public String getQueue() {
		return this.queue;
	}

}
