package com.cadit.mdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 *
 * esempio per richiedere dalla cache il valore per la chiave key1
 * http://localhost:8080/webcaching/SendMessageServlet?entry=key1&operation=GETFROMCACHE
 *
 * esempio per salvare il nuovo valore di key1
 * http://localhost:8080/webcaching/SendMessageServlet?entry=key1&operation=SETTOCACHE
 */
@WebServlet("/SendMessageServlet")
public class SendMessageServlet extends HttpServlet {

    @PersistenceContext(name = "cachePU")
    private EntityManager entityManager;//JTA

    private final Logger logger = LoggerFactory.getLogger(ReadMessageMDB.class);
    private Connection connection;

    private MessageConsumer receiver;



    @Override
    public void init() throws ServletException {
        //ServletConfig servletConfig = getServletConfig();
        try {
            InitialContext ic = new InitialContext();
            connection = getConnection(ic);
            Session jmsSession = getJMSSession(ic);
            Queue queue = (Queue) ic.lookup("jms/queue/ResponseEventCachingQueue");
            receiver = jmsSession.createConsumer(queue);
            connection.start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String text = req.getParameter("entry") != null ? req.getParameter("entry") : "key1=val1";
        TypeMSG type = req.getParameter("operation") != null ? TypeMSG.valueOf(req.getParameter("operation")) : TypeMSG.SETTOCACHE;
        Session jmsSession = null;

        try {
            long count = entityManager.createQuery("select count(c) from CacheEntity c", Long.class).getSingleResult();
            logger.info("records count:" + count);
            Context ic = new InitialContext();
            //ic.lookup("java:comp/UserTransaction");
//            connection = getConnection(ic);
            jmsSession = getJMSSession(ic);
            publish(ic, jmsSession, text, type);
            if (receiver!= null && type.equals(TypeMSG.GETFROMCACHE)){
                Message reply = receiver.receive(TimeUnit.SECONDS.toMillis(60));
                TextMessage reply1 = (TextMessage) reply;
                System.out.println("reply: " + reply1.getText() + " for request correlation id = " + reply1.getJMSCorrelationID());
            }

        } catch (NamingException e) {
            res.getWriter().println("Error while trying to send <" + text + "> message: " + e.getMessage());
        } catch (Exception e) {
            res.getWriter().println("Error while trying to send <" + text + "> message: " + e.getMessage());
        } finally {
            try {
                jmsSession.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

        res.getWriter().println("Message sent: " + text);
    }

    public Connection getConnection(Context ic) throws JMSException, NamingException {
        ConnectionFactory cf = (ConnectionFactory) ic.lookup("/ConnectionFactory");
        Connection connection = cf.createConnection();
        return connection;
    }

    public Session getJMSSession(Context ic) throws JMSException, NamingException {

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.start();
        return session;
    }

    public void publish(Context ic, Session session, String text, TypeMSG type) throws JMSException, NamingException {
        Queue queue = (Queue) ic.lookup("jms/queue/EventCachingQueue");
        MessageProducer publisher = session.createProducer(queue);
        TextMessage message = session.createTextMessage(text);
        message.setStringProperty("operation", type.name());
        message.setJMSCorrelationID(String.valueOf(UUID.randomUUID()));
        publisher.send(message);
    }

    @Override
    public void destroy() {
        try {
            if (connection!= null) {
                connection.close();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}