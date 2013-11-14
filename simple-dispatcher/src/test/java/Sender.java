import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;


public class Sender {

    private ActiveMQConnectionFactory factory;

    public Sender(){
        init();

    }



    void init(){
        factory = new ActiveMQConnectionFactory("failover:(tcp://10.11.132.36:61616,tcp://10.11.132.36:61616)?randomize=false");
        factory.setUseAsyncSend(true);
        factory.setOptimizeAcknowledge(true);
        factory.setCopyMessageOnSend(false);

//      pooledFactory = new PooledConnectionFactory(factory);
//      pooledFactory.setMaxConnections(30);
//      pooledFactory.setMaximumActive(30);
//      pooledFactory.setExpiryTimeout(0);
//      pooledFactory.setIdleTimeout(300000);
//      pooledFactory.start();
    }

    public Destination getDestination() {
        return new ActiveMQTopic("TOPIC.SIMPLEPUSH");
    }

    public Connection getConnection() {
        return getConnection(null, null);
    }

    public Connection getConnection(String user, String pwd) {

        try {
            if (user != null && pwd != null) {
                return factory.createConnection(user, pwd);
            } else {
                return factory.createConnection();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }



    private Connection con;

    MessageProducer producer;

    Session session;

    public void start(){
        con = getConnection();
        try {
            con.start();
            session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            producer = session.createProducer(getDestination());
            System.out.println("jms start!");
        } catch (JMSException e) {
            e.printStackTrace();
        }



    }

    public void send(String message){
        try {
            TextMessage msg = session.createTextMessage(message);
            producer.send(msg);
            System.out.println("send:" + message);

        } catch (JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        Sender test = new Sender();
        test.start();
        System.out.println("start...");

        
        String message = "{\"topic\":\"b-topic\",\"info\":{\"name\":\"hello\",\"age\":11,\"ids\":[111,222,333]}}";
        test.send(message);
        System.out.println(message);
        message = "{\"topic\":\"b-topic\",\"info\":{\"name\":\"hello\",\"age\":11}}";
        test.send(message);
        System.out.println(message);
        
        message = "{\"topic\":\"b-topic\",\"info\":\"你好啊！！！！\"}";
        
        test.send(message);
        System.out.println(message);
        
        
        message = "{\"topic\":\"a-topic\",\"info\":\"welcome to a-topic!\"}";
        
        test.send(message);
        System.out.println(message);
        
        message = "{\"topic\":\"a-topic\",\"info\":{\"a\":111}}";
        
        test.send(message);
        System.out.println(message);


    }

}
