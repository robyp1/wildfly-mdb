package com.cadit.mdb;

import com.cadit.cache.CacheManager;
import com.cadit.data.CacheEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Message-Driven Bean implementation class for: ReadMessageMDB
 * memorizza il valore nella cache locale di questo server
 * oppure riceve la richiesta con una chiave e restituisce il valore trovato in cache o nel db se il sistema è stato appena avviato
 */
@MessageDriven(
        activationConfig = {
                @ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/queue/EventCachingQueue"),
                @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
        })
public class ReadMessageMDB implements MessageListener {

    @PersistenceContext(name = "cachePU")
    private EntityManager entityManager;//JTA

    private final Logger logger = LoggerFactory.getLogger(ReadMessageMDB.class);

    @Resource
    private MessageDrivenContext ctx; //ctx.getUserTansaction..

    @Resource(mappedName = "java:/ConnectionFactory")
    private QueueConnectionFactory queueFactory;

    @Resource(mappedName = "java:/jms/queue/ResponseEventCachingQueue")
    private Queue replyQueue;

    private QueueConnection connection;

    @PostConstruct
    public void init() {
        try {
            connection = queueFactory.createQueueConnection();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void onMessage(Message message) {
        QueueSession session = null;
        QueueSender sender = null;
        TextMessage textMessage = (TextMessage) message;
//        UserTransaction userTransaction = ctx.getUserTransaction();
        try {
            CacheManager cacheManager = CacheManager.getInstance();
            //message selector
            switch (TypeMSG.valueOf(message.getStringProperty("operation"))) {
                case SETTOCACHE:
                    logger.info("Message received: " + textMessage.getText());
                    String[] tuple = textMessage.getText().split("=");
                    CacheEntity cacheEntity = new CacheEntity(tuple[0], tuple[1]);
                    entityManager.persist(cacheEntity);
                    cacheManager.set(tuple[0], tuple[1]);
                    break;
                case GETFROMCACHE:
                    String key = textMessage.getText();
                    String value = cacheManager.get(key);
                    if (value == null) { //miss in cache
                        CacheEntity cacheEntryDb = entityManager.createQuery("select c from CacheEntity c", CacheEntity.class).getSingleResult();
                        if (cacheEntryDb != null) {
                            value = cacheEntryDb.getValue();
                            cacheManager.set(key,value);
                        }
                    }
                    //send reply response for get request received..
                    session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
                    TextMessage responseMessage = session.createTextMessage();
                    responseMessage.setText(key + "=" + value);
                    responseMessage.setJMSCorrelationID(message.getJMSCorrelationID());
                    sender = session.createSender(replyQueue);
                    sender.send(responseMessage);
                    break;
            }

        } catch (Exception e) {
            logger.error("Error while trying to consume messages: " + e.getMessage());
            ctx.setRollbackOnly();

        } finally {
            try {
                if (sender != null) {
                    sender.close();
                }
                if (session != null) {
                    session.close();
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    @PreDestroy
    public void preDispose(){
        try {
            if (connection != null) connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}