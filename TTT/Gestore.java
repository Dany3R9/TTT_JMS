package TTT;

import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;

public class Gestore {
	public static void main(String[] args) throws Exception {
		AdminModule.connect("root", "root", 60);
		TopicConnectionFactory tcf = TopicTcpConnectionFactory.create("localhost", 16010);
		QueueConnectionFactory qcf = QueueTcpConnectionFactory.create("localhost", 16010);
		//ConnectionFactory cf= TcpConnectionFactory.create("localhost", 16010);
		Topic topic = Topic.create("rooms_available");
		Topic topic2 = Topic.create("richieste");
		Queue queue = Queue.create("players");
		Queue queue2 = Queue.create("prova");
		Queue queue3 = Queue.create("risposta");
		Queue queue4 = Queue.create("ettore");
		Queue queue5 = Queue.create("achille");
		User.create("anonymous", "anonymous");
		topic.setFreeReading();
		topic.setFreeWriting();
		topic2.setFreeReading();
		topic2.setFreeWriting();
		queue.setFreeReading();
		queue.setFreeWriting();
		queue2.setFreeReading();
		queue2.setFreeWriting();
		queue3.setFreeReading();
		queue3.setFreeWriting();
		queue4.setFreeReading();
		queue4.setFreeWriting();
		queue5.setFreeReading();
		queue5.setFreeWriting();
		javax.naming.Context jndiCtx = new javax.naming.InitialContext();
		jndiCtx.bind("tcf", tcf);
		jndiCtx.bind("qcf", qcf);
		jndiCtx.bind("rooms_available", topic);
		jndiCtx.bind("richieste", topic2);
		jndiCtx.bind("players", queue);
		jndiCtx.bind("prova", queue2);
		jndiCtx.bind("risposta", queue3);
		jndiCtx.bind("ettore", queue4);
		jndiCtx.bind("achille", queue5);
		jndiCtx.close();
		AdminModule.disconnect();
		System.out.println("Done");
	}
}
