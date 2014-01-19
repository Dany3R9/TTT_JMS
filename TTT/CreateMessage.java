package TTT;

import java.util.HashMap;
import java.util.TreeMap;

import javax.jms.TemporaryQueue;

import org.objectweb.joram.client.jms.Queue;

///Object Message for INIT (create a new room) or Join an existing one!
public class CreateMessage extends MoveMessage {
	private static final long serialVersionUID = -8038796534277657014L;

	private String room;
	private String house;
	private String player;
	private int a;
	private Queue queue;
	private TemporaryQueue u;
	private String messaggio;
	private int x,y;
	private String v;
	private String winner;
	private String now;
	
	public CreateMessage(String player, String room, int e) {
		this.room = room;
		this.player = player;
		a=e;
		if (a ==1)
		this.type = MessageT.INIT;
		else this.type = MessageT.JOIN;
	}
	
	public CreateMessage(String player) {
		this.player = player;
		this.type = MessageT.QUIT;
	}
	
	public CreateMessage(String player, Queue temp) {
		this.player = player;
		this.queue = temp;
		this.type = MessageT.IN;
	}
	
	//house shutting down
	public CreateMessage(String house, int a){
		this.house = house;
		this.type = MessageT.CLOSING;
	}
	public CreateMessage(String player, String room, Queue a) {
		this.room = room;
		this.player = player;
		this.queue =a;
		this.type = MessageT.START;
	}
	
	public CreateMessage (String player, String message, Integer a){
		this.player=player;
		this.messaggio = message;
		Integer tr =2;
		if (a.equals(tr)){
			this.now = "no";
		}
		else {
			this.now = "";
		}
		this.type = MessageT.TEXT;
		
	}
	//winner
	public CreateMessage (String player, String room, String winner, int sel){
		this.player =player;
		this.room = room;
		this.winner=winner;
		a = sel;
		if (a==1)
		this.type = MessageT.WINNER;
		
		if (a==3)
		this.type = MessageT.DEUCE;

	}
	
	public CreateMessage (String player, String room, String v, int x, int y){
		this.player=player;
		this.room = room;
		this.x =x;
		this.y=y;
		this.v =v;
		this.type = MessageT.STEP;

	}
	
	public CreateMessage(String tr, long a){
		this.now = tr;
		this.type = MessageT.AD;
	}

	public String getRoom() {
		return this.room;
	}
	
	public Queue getQueue(){
		return this.queue;
	}
	

	public String getPlayer() {
		return this.player;
	}
	
	public String getV() {
		return this.v;
	}
	
	public int[] getPosition(){
		int[] a ={x,y};
		return a;
	}
	
	public String getWinner(){
		return this.winner;
	}
	
	public String getMessage(){
		return this.messaggio;
	}
	
	public String getNow(){
		return this.now;
	}
	
	public String getNomi(){
		return this.now.toString();
	}
}