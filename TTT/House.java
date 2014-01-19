
package TTT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
//import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;

import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;


public class House /*implements MessageListener*/ {
	private static Context ictx;
	private String name;
	private int maxUsers;
	private String curWinner;
	private QueueSession senderSession;
	private QueueSession receiverSession;
	private QueueConnectionFactory qcf;
	private QueueConnection senderConnection;
	private QueueConnection receiverConnection;
	private QueueReceiver receiver;
	private QueueSender sender;
	private TopicSession pubSession;
	private TopicSession subSession;
	private TopicConnectionFactory tcf;
	private TopicConnection subConn;
	private TopicConnection pubConn;
	private TopicPublisher publisher;
	private TopicSubscriber subscriber;
	
	private Vector<Object> pl;//code
	private Map<String, Object> players;///nome e coda
	private Vector<String> giocatori;
	private Vector<String> tavoli;
	private Map<String, String> rooms;//stanza e nome creatore
	private Map<String, Boolean> lock;//stanza e lock
	private Map<String, Integer> winner;
	//private ValueComparator bvc;
	CreateMessageListener top;
	
	Topic topic;
	Topic topic2;
	Queue queue;
	Queue queue2;
	
	private Vector<String[][]> board; 
	static String[][] tavolo;
    static String X = "X";
    static String O = "O";    

public House(String name, int maxPlayers) {
		this.name = name;
		this.maxUsers = maxPlayers;
		this.curWinner = "";
		board = new Vector<String[][]>();
	}

public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Usage: House(TTT) name + maxUsers");
			System.exit(1);
		}
		
		House seller = new House(args[0], Integer.parseInt(args[1]));
		seller.init();
	}


private void init() throws NamingException, JMSException {
		ictx = new InitialContext();
		this.tcf = (TopicConnectionFactory) ictx.lookup("tcf");
		this.qcf = (QueueConnectionFactory) ictx.lookup("qcf");
		
		topic = (Topic) ictx.lookup("rooms_available");
		//topic2 = (Topic) ictx.lookup("richieste");
		queue = (Queue) ictx.lookup("players");
		//queue2 =  (Queue) ictx.lookup("prova");
		ictx.close();
		
		//this.subConn = this.tcf.createTopicConnection();
		this.pubConn = this.tcf.createTopicConnection();
		this.pubSession = this.pubConn.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		//this.subSession = this.subConn.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		this.publisher = this.pubSession.createPublisher(topic);
		//this.subscriber = this.subSession.createSubscriber(topic2, "multy = true", false);
		//this.subscriber.setMessageListener(this);
		//this.subConn.start();
		
		this.senderConnection = this.qcf.createQueueConnection();
		this.receiverConnection = this.qcf.createQueueConnection();
		this.senderSession = this.senderConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		this.receiverSession = this.receiverConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		//this.sender = this.senderSession.createSender(queue2);
		
		top = new CreateMessageListener(this.name);
		this.receiver= this.receiverSession.createReceiver(queue);
		this.receiver.setMessageListener(top);
		this.receiverConnection.start();
		
		rooms = new HashMap<String, String>();
		lock = new HashMap<String, Boolean>();
		winner = new HashMap<String,Integer>();
		giocatori= new Vector<String>();
		tavoli = new Vector<String>();
		pl = new Vector<Object>();
		players = new HashMap<String, Object>();
		
		Deamon nuo= new Deamon(this);
		Thread a = new Thread(nuo);
		a.start();
		
		parseInput();
	}

public synchronized void getWS() throws JMSException{
	TopicConnection tc =null;
	TopicSession ts =null;
	TopicPublisher tpub=null;
	String[] now = new String[11];
	int y=11;
	
	if (!winner.isEmpty()){
		ValueComparator bvc =  new ValueComparator(winner);
        TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
	sorted_map.putAll(winner);
	System.out.println("results: "+ sorted_map);
	//advice
	tc = tcf.createTopicConnection();
	ts = tc.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
	tpub= ts.createPublisher(topic);
	String string = sorted_map.toString();
	long a = (long) 12.3;
	CreateMessage crMess = new CreateMessage(string, a); // 1 for create a new room
	ObjectMessage roomObjectMess = ts.createObjectMessage(crMess);
	roomObjectMess.setBooleanProperty("multy", true);
	roomObjectMess.setJMSReplyTo(topic);
	tc.start();
	tpub.send(roomObjectMess);
	tc.close();
	}
}

private void parseInput() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String s;
			while (true) {
				System.out.println("\nWaiting new command: ");
				s = in.readLine();
				switch (s) {
					case "info":
						System.out.println("For House " + this.name + " the current rooms available are: \n");
						if (rooms.isEmpty()) System.out.println("No rooms currently available!");
						else{
							System.out.println("Stanze create in ###  " + this.name );
							System.out.println("------------------------------------");
							for (String a : tavoli){
								System.out.println("Room name# " + a + " ,created by " + rooms.get(a).toString() );
								}; System.out.println("__________________________________________");
							}
						if(players.isEmpty()) System.out.println("No players in the house at the moment!");
						else{
							System.out.println("Players in  ###  " + this.name );
							System.out.println("------------------------------------");	
						    System.out.println(players.toString());
						}
						
						break;
						
					case "close":
						CreateMessage closingMessage = new CreateMessage(this.name, 4); //number, why house closing
						ObjectMessage closingObjectMessage = this.pubSession.createObjectMessage(closingMessage);
						closingObjectMessage.setBooleanProperty("multy", true);
						this.publisher.publish(closingObjectMessage);
						break;
						
					case "leaderboard":
						System.out.println("For House " + this.name + " the current ranking is: \n");
						if (winner.isEmpty()) System.out.println("No player has already played!");
						else{
							//bvc=  new ValueComparator(winner);
							//sorted_map = new TreeMap<String, Integer>(bvc);
							System.out.println(winner);
							 //sorted_map.putAll(winner);
						     //System.out.println("results: "+sorted_map);
						}
						break;
					default:
						System.out.println("Accepted commands: info| leaderboard | close");
						break;
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (JMSException e) {
			e.printStackTrace();
		}
	}


private void exit() throws JMSException {
		this.pubConn.close();
		this.subConn.close();
		System.exit(0);
	}
	

	
	//////////// 
public static void addQ(String a) throws ConnectException, UnknownHostException, AdminException, NamingException{
		 
		 	String queueName = a;
			AdminModule.connect("root", "root", 60);

			Queue queue = Queue.create(queueName);
			User.create("anonymous", "anonymous");
			queue.setFreeReading();
			queue.setFreeWriting();

			javax.naming.Context jndiCtx = new javax.naming.InitialContext();
			jndiCtx.bind(a, queue);
			jndiCtx.close();

			AdminModule.disconnect();
	 }


///listener
class CreateMessageListener implements MessageListener{
	
	private String name;
	
	public CreateMessageListener(String who){
		this.name = who;
	}
	
public synchronized void onMessage(Message arg0) {
		
		QueueConnection qc =null;
		QueueSession qs =null;
		Queue tempQueue =null;
		QueueSender qsend=null;
		TopicConnection tc =null;
		TopicSession ts =null;
		TopicPublisher tpub=null;
		try {
			ObjectMessage obj = (ObjectMessage) arg0;
			MoveMessage msg = (MoveMessage) obj.getObject();
			switch (msg.getType()) {
				case IN:
					CreateMessage messa = (CreateMessage) msg;
					players.put(messa.getPlayer(), messa.getQueue());
				break;
				
				case INIT:
					CreateMessage messaggio = (CreateMessage) msg;
					if (!rooms.containsKey(messaggio.getRoom())){
						tavoli.add(messaggio.getRoom());
						giocatori.add(messaggio.getPlayer());
						if(!winner.containsKey(messaggio.getPlayer())) winner.put(messaggio.getPlayer(),0);
						rooms.put(messaggio.getRoom(), messaggio.getPlayer());
						lock.put(messaggio.getRoom(), false);
						pl.add((Queue) arg0.getJMSReplyTo());
						
						System.out.println("New room created. # " + messaggio.getRoom() + " Lock is " + lock.get(messaggio.getRoom()));
						

						qc = qcf.createQueueConnection();
						qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
						tempQueue = (Queue) qs.createTemporaryQueue();
						qsend = qs.createSender((Queue) obj.getJMSReplyTo());
						String text = "Room " + messaggio.getRoom() +  " created";
						Integer ty =0;
						CreateMessage creMess = new CreateMessage(this.name, text, ty); // 1 for create a new room
						ObjectMessage c = qs.createObjectMessage(creMess);
						c.setBooleanProperty("multy", true);
						qc.start();
						qsend.send(c);
						qc.close();
						
						tc = tcf.createTopicConnection();
						ts = tc.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
						tpub= ts.createPublisher(topic);
						CreateMessage crMess = new CreateMessage(messaggio.getPlayer(), messaggio.getRoom() , 1); // 1 for create a new room
						ObjectMessage roomObjectMess = ts.createObjectMessage(crMess);
						roomObjectMess.setBooleanProperty("multy", true);
						roomObjectMess.setJMSReplyTo(topic);
						tc.start();
						tpub.send(roomObjectMess);
						tc.close();
						
						}
					else {
						System.out.println("The room " + messaggio.getRoom() + " already exist! Create a new Room.");
						qc = qcf.createQueueConnection();
						qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
						tempQueue = (Queue) qs.createTemporaryQueue();
						qsend = qs.createSender((Queue) obj.getJMSReplyTo());
						String text = "Room " + messaggio.getRoom() +  " already exist. Create a new Room ";
						Integer t = 0;
						CreateMessage creMess = new CreateMessage(this.name, text, t); // 1 for create a new room
						 ObjectMessage c = qs.createObjectMessage(creMess);
							c.setBooleanProperty("multy", true);
							c.setJMSReplyTo(tempQueue);
						qc.start();
						qsend.send(c);
						qc.close();
					}
					
					break;
					
				case JOIN:	
					CreateMessage bidMessage = (CreateMessage) msg;
					if (!rooms.containsKey(bidMessage.getRoom())){
						qc = qcf.createQueueConnection();
						qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
						qsend = qs.createSender((Queue) obj.getJMSReplyTo());
						//No room to join
						String text = "Room " + bidMessage.getRoom() +  " not found. Create a new Room or Join an existed one.";
						Integer tr= 2;
						CreateMessage joinMess = new CreateMessage(this.name, text, tr ); //manda al joiner
						ObjectMessage join = qs.createObjectMessage(joinMess);
						join.setBooleanProperty("multy", true);
						qc.start();
						qsend.send(join);
						qc.close();
						break;
					}
					
					if(!lock.get(bidMessage.getRoom())){
						if(!giocatori.contains(bidMessage.getPlayer())) {giocatori.add(bidMessage.getPlayer());
						if (!winner.containsKey(bidMessage.getPlayer())){winner.put(bidMessage.getPlayer(), 0);}}
							pl.add((Queue) arg0.getJMSReplyTo());
							//players.put(bidMessage.getPlayer(),(Queue) obj.getJMSReplyTo() );
							
							qc = qcf.createQueueConnection();
							qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
							qsend = qs.createSender((Queue) obj.getJMSReplyTo());
							String creatore = rooms.get(bidMessage.getRoom());
							Queue pass = (Queue) players.get(creatore);
							//START to joiner
							CreateMessage joinMess = new CreateMessage(this.name, bidMessage.getRoom(), (Queue) pass ); //manda al joiner
							ObjectMessage join = qs.createObjectMessage(joinMess);
							join.setBooleanProperty("multy", true);
							System.out.println("Primo obj " + obj.getJMSReplyTo());
							
							qc.start();
							//Mandare coda del creatore room a player
							qsend.send(join);
							qc.close();
							
							//////
							
							qc = qcf.createQueueConnection();
							qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
							System.out.println("Pass: " +pass);
							qsend = qs.createSender((Queue) pass);//manda al creatore
							CreateMessage Mess = new CreateMessage(bidMessage.getPlayer(), bidMessage.getRoom(), (Queue) obj.getJMSReplyTo()); // manda al creatore
							ObjectMessage j = qs.createObjectMessage(Mess);
							j.setBooleanProperty("multy", true);
							qc.start();
							qsend.send(j);
							qc.close();
							lock.remove(bidMessage.getRoom());
							lock.put(bidMessage.getRoom(), true);
							
							//avvisa gli altri

							tc = tcf.createTopicConnection();
							ts = tc.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
							tpub= ts.createPublisher(topic);
							CreateMessage crMess = new CreateMessage(bidMessage.getPlayer(), bidMessage.getRoom() , 5); // 1 for create a new room
							ObjectMessage roomObjectMess = ts.createObjectMessage(crMess);
							roomObjectMess.setBooleanProperty("multy", true);
							roomObjectMess.setJMSReplyTo(topic);
							tc.start();
							tpub.send(roomObjectMess);
							tc.close();
							}
					else{
						System.out.println("The room " + bidMessage.getRoom() + " is already locked. Create a new Room for play");
						qc = qcf.createQueueConnection();
						qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
						tempQueue = (Queue) qs.createTemporaryQueue();
						qsend = qs.createSender((Queue) obj.getJMSReplyTo());
						String text = "Room " + bidMessage.getRoom() +  " already running. Create a new Room for play";
						Integer r =2;
						CreateMessage creMess = new CreateMessage(this.name, text, r); // 1 for create a new room
						 ObjectMessage c = qs.createObjectMessage(creMess);
							c.setBooleanProperty("multy", true);
							c.setJMSReplyTo(tempQueue);
						qc.start();
						qsend.send(c);
						qc.close();
					}
					
					break;
					
				case ROOM:
					//possible implementation of chat inside a room controlled or not by house
					break;
					
				case STEP:
					//Private session, not used in House
					break;
					
				case QUIT:
					CreateMessage quit = (CreateMessage) msg;
					qc = qcf.createQueueConnection();
					qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
					qsend = qs.createSender((Queue) obj.getJMSReplyTo());
					System.out.println("The player " + quit.getPlayer() + " wants to leave the house . Saving the progress");
					players.remove(quit.getPlayer());
					CreateMessage Mess = new CreateMessage(this.name); // permetti la chiusura
					ObjectMessage j = qs.createObjectMessage(Mess);
					j.setBooleanProperty("multy", true);
					qc.start();
					qsend.send(j);
					qc.close();
					break;
					
				case CLOSING:
					CreateMessage creMess = new CreateMessage("House shutting down!"); // 1 for create a new room
					tc = tcf.createTopicConnection();
					ts = tc.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
					tpub= ts.createPublisher(topic);
					ObjectMessage roomObjectMess = ts.createObjectMessage(creMess);
					roomObjectMess.setBooleanProperty("multy", true);
					tc.start();
					tpub.send(roomObjectMess);
					tc.close();
					exit();
					break;
					
				case WINNER:
					CreateMessage w = (CreateMessage) msg;
						System.out.println("LOG: -- " + w.getPlayer() +" won the TTT game hold in room " + w.getRoom());
						int volte=winner.get(w.getPlayer());
						System.out.println("LOG: -- " + w.getPlayer() +" won " + (volte+1)+ " times");
						winner.remove(w.getPlayer());
						winner.put(w.getPlayer(), (volte+1));
						//advice
						tc = tcf.createTopicConnection();
						ts = tc.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
						tpub= ts.createPublisher(topic);
						CreateMessage wMess = new CreateMessage(this.name, w.getRoom() , w.getPlayer(), 1); // 1 for create a new room
						ObjectMessage rMess = ts.createObjectMessage(wMess);
						rMess.setBooleanProperty("multy", true);
						rMess.setJMSReplyTo(topic);
						tc.start();
						tpub.send(rMess);
						tc.close();
					break;
					
				case DEUCE:
					CreateMessage deu = (CreateMessage) msg;
					System.out.println("LOG: -- " + deu.getRoom() +" ended without winner! ");
					break;
			default:
				break;
			}
		}
		catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
}


////
class Deamon implements Runnable{
	House in;
	
	public Deamon(House i){
		in =i;
	}
	
	@Override
	public void run() {
		try {
	        while (true) {
	            System.out.println(new Date());
	            Thread.sleep(6 * 10000);
	            try {
					in.getWS();
				} catch (JMSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
	}
	
}

class ValueComparator implements Comparator<String> {

    Map<String, Integer> base;
public ValueComparator(Map<String, Integer> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return  -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}
