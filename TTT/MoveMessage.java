package TTT;

import java.io.Serializable;


public abstract class MoveMessage implements Serializable {
	private static final long serialVersionUID = -2212946269841641950L;

	protected MessageT type;

	public TTT.MessageT getType() {
		return this.type;
	}

}
