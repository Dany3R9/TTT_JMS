package TTT;

public class ControlMessage extends MoveMessage {
		private static final long serialVersionUID = -8038796534277657014L;

		private String who;
		private String what;
		private int number;

		public ControlMessage(String who, String what) {
			this.who = who;
			this.what= what;
			this.type = MessageT.CONTROL;
		}

		public ControlMessage(String who) {
			this.who = who;
			this.type = MessageT.CONTROL;
		}
	

		public String getWho() {
			return this.who;
		}

		public String getWhat() {
			return this.what;
		}
	}
