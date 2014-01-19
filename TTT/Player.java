package TTT;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.swing.Timer;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;


public class Player /*extends Thread implements MessageListener*/ {
	private static Context ictx;

	private String name;
	private String room;
	private String coda;
	private Queue altro;
	private String altroName;
	private int[] pos = new int[2];
	static String v;
	private boolean ferma =false;
	boolean iplay = false;
	boolean stop= false;
	TicTacToe ttt;
	int counter =0;
	int moves = 0;
	boolean ora = false;
	boolean fine =false;
	boolean starting = false;
	private int volta =0;
	int tyy;
	
	private QueueConnectionFactory qcf;
	private QueueConnection senderConnection;
	private QueueConnection receiverConnection;
	private QueueSession senderSession;
	private QueueSession receiverSession;
	private QueueReceiver receiver;
	private QueueSender sender;
	private Queue que;

	private TopicConnectionFactory tcf;
	private TopicConnection subConn;
	private TopicSession subSession;
	private TopicSubscriber subscriber;
	
	CreateMessageListener top;
	
	Topic topic;
	Topic topic2;
	Queue queue;
	
	Timer timer;
	int delay = 20000; //milliseconds
	  

	public Player(String name, String coda) {
		this.name = name;
		this.coda = coda;
	}
	
	//main
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Usage: Player name (unique) + name_of_queue");
			System.exit(1);
		}
		Player participant = new Player(args[0], args[1]);
		Player.run(participant);
	}//end main
	
	
	public static void run(Player a){
		try {
			a.init();
		} catch (NamingException e) {
			System.out.println("Errore nell'inserimento del nome del Player"); 
			e.printStackTrace();
		} catch (JMSException e) {
			System.out.println("Errore generico"); 
			e.printStackTrace();
		}
	}

	
private void init() throws NamingException, JMSException {
	tyy =0;
	if (volta ==0){
		ictx = new InitialContext();
		this.tcf = (TopicConnectionFactory) ictx.lookup("tcf");
		this.qcf = (QueueConnectionFactory) ictx.lookup("qcf");
		try {
			Player.addQ(this.coda);
		} catch (ConnectException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AdminException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  catch (NameAlreadyBoundException e){
			System.out.println("Queue already taken change the name of the queue");
			System.exit(1);
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		topic = (Topic) ictx.lookup("rooms_available");
		queue = (Queue) ictx.lookup("players");
		this.que = (Queue) ictx.lookup(this.coda);
		ictx.close();
	}
	if (volta >0){
		ictx = new InitialContext();
		this.tcf = (TopicConnectionFactory) ictx.lookup("tcf");
		this.qcf = (QueueConnectionFactory) ictx.lookup("qcf");
		try {
			ictx.unbind(this.coda);
			if (volta >1)
			ictx.unbind(this.coda+(volta-1));
			Player.addQ(this.coda+volta);
		} catch (ConnectException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AdminException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		topic = (Topic) ictx.lookup("rooms_available");
		queue = (Queue) ictx.lookup("players");
		this.que = (Queue) ictx.lookup(this.coda+volta);
		ictx.close();
	}
		 top = new CreateMessageListener(this.name);
		
		//queue point to point 
		//sender
		this.senderConnection = this.qcf.createQueueConnection();
		this.senderSession = this.senderConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		this.sender = this.senderSession.createSender(queue);
		this.senderConnection.start();
		//receiver
		this.receiverConnection = this.qcf.createQueueConnection();
		this.receiverSession = this.receiverConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		this.receiver = this.receiverSession.createReceiver(this.que);
		this.receiver.setMessageListener(top);
		this.receiverConnection.start();
		
		//pub-sub domain
		/*this.subConn = this.tcf.createTopicConnection();
		this.subSession = this.subConn.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		this.subscriber = this.subSession.createSubscriber(topic, "multy = true", false);
		this.subscriber.setMessageListener(top);
		this.subConn.start();*/
		
		//send to house
		CreateMessage creMess = new CreateMessage(this.name, this.que); // advise the house
		ObjectMessage roomObjectMess = this.senderSession.createObjectMessage(creMess);
		roomObjectMess.setBooleanProperty("multy", true);
		this.sender = this.senderSession.createSender(queue);
		this.sender.send(roomObjectMess);
		
		parseInput();
	}
	
	 
	  
private synchronized void parseInput() throws JMSException {
		 ttt= new TicTacToe(); 
		 if(stop != true && ferma != true){
		 this.subConn = this.tcf.createTopicConnection();
		this.subSession = this.subConn.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		this.subscriber = this.subSession.createSubscriber(topic, "multy = true", false);
		this.subscriber.setMessageListener(top);
		this.subConn.start();}
		 
		 ActionListener taskPerformer = new ActionListener() {
		      public void actionPerformed(ActionEvent evt) {
		    	/*try {
					resend();
				} catch (JMSException e) {
					System.out.println("parseInput() check the code"); 
					e.printStackTrace();
				}*/

		  }};
		 timer =new Timer(delay, taskPerformer);
		try {
			
			String s;
			while (!stop && !ferma) {
				System.out.println("\n" + this.name + " , Waiting new command: ");
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				s = in.readLine();
				String command[] = s.split(" ");
				if (command.length == 0) {
					System.out.println("Error: Accepted commands: create | join room_name | exit ");
					continue;
				}
				switch (command[0]) {
					case "create":
						if (command.length != 2) {
						System.out.println("Accepted commands: create|join room_name");
							break;
					}
					String it = command[1];
					room = it;
					//create a Message to send to the house 
					CreateMessage creMess = new CreateMessage(this.name, it, 1); // 1 for create a new room
					ObjectMessage roomObjectMess = this.senderSession.createObjectMessage(creMess);
					roomObjectMess.setBooleanProperty("multy", true);
					roomObjectMess.setJMSReplyTo(que);
					this.sender = this.senderSession.createSender(queue);
					this.sender.send(roomObjectMess);
					timer.start();
					timer.setRepeats(false);
					//stop=true;
					   break;
						
					case "join":
						if (command.length != 2) {
							System.out.println("Accepted commands: create|join room_name");
							break;
						}
						String item = command[1];
						room =item;
						CreateMessage joinMess = new CreateMessage(this.name, item, 5); // 5 for join
						ObjectMessage join = this.senderSession.createObjectMessage(joinMess);
						join.setBooleanProperty("multy", true);
						join.setJMSReplyTo(que);
						this.sender = this.senderSession.createSender(queue);
						this.sender.send(join);
						//stop=true;
						break;
						
					case "exit":
						CreateMessage eMess = new CreateMessage(this.name); // quit
						ObjectMessage quit = this.senderSession.createObjectMessage(eMess);
						quit.setBooleanProperty("multy", true);
						quit.setJMSReplyTo(que);
						this.sender = this.senderSession.createSender(queue);
						this.sender.send(quit);
						//stop=true;
						break;
						
					default:
						System.out.println("Accepted commands: create|join room_name | exit ");
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
	
	
	
private synchronized boolean inizia(String v) throws JMSException{
		int ora[] = new int[2];
		final String X = "X";
		final String O = "O"; 
		       
		if(counter ==0){
		System.out.println("-------------------------------------");
		System.out.println("-------------------------------------");
		System.out.println("--------------Tic Tac Toe-------------");
		System.out.println("-------------------------------------");
		System.out.println("I play as ### " + v);
		counter++;
		moves++;
        if(v.equals("X")){
        	System.out.println("Let's play TicTacToe -- X goes first");
        	ferma =true;
        	Scanner stdin = new Scanner(System.in);  
        	ora = ttt.makeMove(stdin, X);
        	invia(v, ora[0], ora[1]);
            ttt.printBoard();
            
       }}
		else{
			System.out.println("Please..");
		if(v.equals("X")){
			//case play ad X
			   if (ttt.checkWinner(X)) {
				   winner("X"); stop = false; ferma = false; iplay = false;
			       System.out.println(this.name + " You Win!!!" + " using the element " + X);
			       System.out.println("Lets play another time if you want..");
			       fine =true; moves =0;
			       counter = 0; v=null;
			       return fine;
			}
			   if (ttt.checkWinner(O)) {
		    	   System.out.println(altroName + " Win!!!" + " using the element " + O);
		    	   System.out.println("Don't worry next time will be better!.....");
		           System.out.println("Lets play another time.....");
		           fine = true; moves =0;
		           counter = 0; v=null;
		           iplay = false; stop =false; ferma = false;
					counter =0; moves =0;
		           return fine;
		            }
			   System.out.println("Move number " + (moves+1));
			   	Scanner stdin = new Scanner(System.in);  
			   	ora = ttt.makeMove(stdin, X);
				  moves++;
				  if (ttt.checkWinner(X)) {
					   winner("X"); stop = false; ferma = false; iplay = false;
				       System.out.println(this.name + " You Win!!!" + " using the element " + X);
				       System.out.println("Lets play another time if you want..");
				       fine =true; moves =0;
				       counter = 0; v=null;
				       return fine;
				}
				  if (moves >4){stop = false; ferma = false; iplay = false;
			       System.out.println(this.name + "--- finish the game in deuce. ");
			       System.out.println("Lets play another time if you want..");
			       fine =true; moves = 0;
			       counter = 0; v=null; deuce("X"); return fine;
				  }
				  
			     ttt.printBoard();
			     invia(v, ora[0], ora[1]);
			        }
		
		//case play as O
			 if(v.equals("O")){
			       if (ttt.checkWinner(O)) {
			    	   winner("O"); stop = false; ferma = false; iplay = false;
			    	   System.out.println(this.name + " You Win!!!" + " using the element " + O);
			           System.out.println("Lets play another time.....");
			           fine = true; moves =0;
			           counter = 0; v=null;
			           return fine;
			            }
			       if (ttt.checkWinner(X)) {
			    	   System.out.println(altroName + " Win!!!" + " using the element " + X);
			    	   System.out.println("Don't worry next time will be better!.....");
			           System.out.println("Lets play another time.....");
			           fine = true; moves =0;
			           counter = 0; v=null;
			           iplay = false; stop =false; ferma = false;
						counter =0; moves =0;
			           return fine;
			            }
			       System.out.println("Move number " + (moves ));
			       Scanner stdin = new Scanner(System.in);  
			       ora = ttt.makeMove(stdin, O);
				     moves++;
				     if (ttt.checkWinner(O)) {
				    	   winner("O"); stop = false; ferma = false; iplay = false;
				    	   System.out.println(this.name + " You Win!!!" + " using the element " + O);
				           System.out.println("Lets play another time.....");
				           fine = true; moves =0;
				           counter = 0; v=null;
				           return fine;
				          }
				     if (moves >4){ stop = false; ferma = false; iplay = false;
				       System.out.println(this.name + "--- finish the game in deuce. ");
				       System.out.println("Lets play another time if you want..");
				       fine =true; moves =0;
				       counter = 0; v=null; deuce("O"); return fine;
				       }
				   
			       ttt.printBoard();
			       invia(v, ora[0], ora[1]);
			        }
		} 	
		
		return fine;
       
}	 ///inizia
	
	
	
	 private synchronized void winner (String v) throws JMSException {
			QueueConnection qc =null;
			QueueSession qs =null;
			
      ///prova mess
      try {
			qc = qcf.createQueueConnection();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
			qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		try {
			this.sender = qs.createSender( altro);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//manda al rivale
		 CreateMessage Mess = new CreateMessage(this.name, this.room, this.name, 1); //STEP Message
		 ObjectMessage j = qs.createObjectMessage(Mess);
			j.setBooleanProperty("multy", true);
			j.setJMSReplyTo(que);
		qc.start();
		System.out.println("Invio messaggio di vittoria a " + altro.toString());
		this.sender = this.senderSession.createSender(altro);
		this.sender.send(j);
		qc.close();
		
		//manda in topic via House
		CreateMessage creMess = new CreateMessage(this.name, this.room, this.name, 1); // winner
		ObjectMessage roomObjectMess = this.senderSession.createObjectMessage(creMess);
		roomObjectMess.setBooleanProperty("multy", true);
		roomObjectMess.setJMSReplyTo(que);
		this.sender = this.senderSession.createSender(queue);
		this.sender.send(roomObjectMess);
  }
	 
	 
	 private synchronized void deuce (String v) throws JMSException {
			QueueConnection qc =null;
			QueueSession qs =null;
			
   ///prova mess
   try {
			qc = qcf.createQueueConnection();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
			qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		try {
			this.sender = qs.createSender( altro);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//manda al rivale
		 CreateMessage Mess = new CreateMessage(this.name, this.room, this.name, 3); //STEP Message
		 ObjectMessage j = qs.createObjectMessage(Mess);
			j.setBooleanProperty("multy", true);
			j.setJMSReplyTo(que);
		qc.start();
		System.out.println("Invio messaggio deuce " + altro.toString());
		this.sender = this.senderSession.createSender(altro);
		this.sender.send(j);
		qc.close();
		
		//manda in topic via House
		CreateMessage creMess = new CreateMessage(this.name, this.room, this.name, 3); // winner
		ObjectMessage roomObjectMess = this.senderSession.createObjectMessage(creMess);
		roomObjectMess.setBooleanProperty("multy", true);
		roomObjectMess.setJMSReplyTo(que);
		this.sender = this.senderSession.createSender(queue);
		this.sender.send(roomObjectMess);
}
	 
	 
	
	 private synchronized void invia (String v, int a, int b) throws JMSException {
			QueueConnection qc =null;
			QueueSession qs =null;
			
         ///prova mess
         try {
 			qc = qcf.createQueueConnection();
 		} catch (JMSException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		 try {
 			qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
 		} catch (JMSException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println("Prova connessione ");
 		
 		try {
 			this.sender = qs.createSender( altro);
 		} catch (JMSException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}//manda al rivale
 		 CreateMessage Mess = new CreateMessage(this.name, this.room, v,  a, b); //STEP Message
 		 ObjectMessage j = qs.createObjectMessage(Mess);
 			j.setBooleanProperty("multy", true);
 			j.setJMSReplyTo(que);
 		qc.start();
 		System.out.println("Invio messaggio a " + altro.toString());
 		this.sender.send(j);
 		qc.close();
     	
     }
	 
	 private synchronized void resend () throws JMSException {
			QueueConnection qc =null;
			QueueSession qs =null;
			
      ///prova mess
      try {
			qc = qcf.createQueueConnection();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
			qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			this.sender = qs.createSender( altro);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//manda al rivale
		CreateMessage creMess = new CreateMessage(this.name, room, 1); // 1 for create a new room
		ObjectMessage roomObjectMess = this.senderSession.createObjectMessage(creMess);
		roomObjectMess.setBooleanProperty("multy", true);
		roomObjectMess.setJMSReplyTo(que);
		this.sender = this.senderSession.createSender(queue);
		this.sender.send(roomObjectMess);
  }

	 
	private void exit() throws JMSException{
		//this.pubConn.close();
		this.subConn.close();
		this.senderConnection.close();
		this.senderSession.close();
		System.exit(0);
	}
	
	//public String play(String a, )


//////////////
////////////////////////////
/////////////////////////////////fine Player


public static void addQ(String a) throws Exception,  ConnectException, UnknownHostException, AdminException, NamingException{
	 
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


//Listener
class CreateMessageListener implements MessageListener{
	
	private String name;
	
	public CreateMessageListener(String who){
		this.name = who;
	}
	
	public synchronized void onMessage(Message message){
		try {
			ObjectMessage obj = (ObjectMessage) message;
			MoveMessage msg = (MoveMessage) obj.getObject();
			switch (msg.getType()) {
				case INIT:
					CreateMessage itemMessage = (CreateMessage) msg;
					if(!itemMessage.getPlayer().equals(this.name) && iplay ==false){
					System.out.println("New request:  The player " + itemMessage.getPlayer() + " invite you to partecipate to TTT game on Room " + itemMessage.getRoom());}
					if(itemMessage.getPlayer().equals(this.name) && iplay ==false){
						System.out.println("Room " + itemMessage.getRoom() + " created by " + itemMessage.getPlayer());}
					break;
					
					
				case JOIN:
					CreateMessage joinMessage = (CreateMessage) msg;
					if(!joinMessage.getPlayer().equals(this.name) && iplay ==false){
					System.out.println("INFO:  The player " + joinMessage.getPlayer() + " wants to join the Room " + joinMessage.getRoom());
					timer.stop();
					}
					break;
					
				case START:
					CreateMessage jMessage = (CreateMessage) msg;
					if(!jMessage.getPlayer().equals(this.name) && iplay ==false){
						altro = jMessage.getQueue();
						altroName =jMessage.getPlayer();
						room = jMessage.getRoom();
					System.out.println("INFO: You("+this.name + ")'re  a second to start the game with " + jMessage.getPlayer());}
					ferma =true; stop =true;
					if(jMessage.getPlayer().equals("house")){ v= "O"; subConn.stop();}
					if(!jMessage.getPlayer().equals("house")){ v= "X"; subConn.stop();}
					if(moves<5){
					inizia(v);}
					else{
						System.out.println("Run out of movments! No Winner in this match, restart!...");
						volta++; init();
					}
					break;
					
				case STEP:
					CreateMessage Message = (CreateMessage) msg;
					iplay = true;
					if(!Message.getPlayer().equals(this.name)){
						pos = Message.getPosition();
					System.out.println("Stiamo giocando sono " + Message.getV() +" , ho scelto di giocare [ " + pos[0] + " , " + pos[1] + " ] " );
					}
					//check the correctness of the step
					ttt.addPos(pos, Message.getV());
					ttt.printBoard();
					boolean aaa = inizia(v);
					if (aaa){ volta++; fine = false; room =null; init(); }
					break;
					
				case TEXT:
					CreateMessage tMessage = (CreateMessage) msg;
					if (tMessage.getNow().equals("no")){
						System.out.println("INFO: -- " + tMessage.getPlayer() + " tell you_ " + tMessage.getMessage() + " ! .");
						room = null;
					}
					else{
					System.out.println("INFO: -- " + tMessage.getPlayer() + " tell you_ " + tMessage.getMessage());}
					break;
					
				case AD:
					CreateMessage adMessage = (CreateMessage) msg;
					System.out.println("INFO: Leader -- \n" + adMessage.getNomi());
					break;
					
				case CLOSING:
					System.out.println("House sent command close, Shutting down!");
					exit();
					break;
					
				case QUIT:
					System.out.println("House sent command close, Shutting down!");
					exit();
					break;
					
				case WINNER:
					CreateMessage winnerMessage = (CreateMessage) msg;
					if (!winnerMessage.getPlayer().equals(this.name)) {
						System.out.println(winnerMessage.getWinner() +" won the TTT game hold in room " + winnerMessage.getRoom());
					}
					
					if (!winnerMessage.getPlayer().equals(this.name) && winnerMessage.getRoom().equals(room)){
					iplay = false; stop =false; ferma = false;
					counter =0; volta++; moves =0; altroName =null; room =null;
					init();
					}
					break;
					
				case DEUCE:
					CreateMessage deuceMessage = (CreateMessage) msg;
					if (!deuceMessage.getPlayer().equals(this.name)) {
						System.out.println(deuceMessage.getRoom() +" ended without winner! ");
					}
					
					if (!deuceMessage.getPlayer().equals(this.name) && deuceMessage.getRoom().equals(room)){
					iplay = false; stop =false; ferma = false;
					counter =0; moves =0; altroName =null; room =null; volta++;
					init();
					}
					break;
					
			default:
				System.out.println("Messaggio criptato! .. ");
				break;
			}
		}
		catch (JMSException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}//lstner


}
