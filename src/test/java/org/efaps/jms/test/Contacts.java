package org.efaps.jms.test;

import java.io.StringReader;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.efaps.esjp.jms.contacts.Contact;
import org.efaps.esjp.jms.msg.listener.ActionListener;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Contacts
    extends AbstractJmsTest
{

    @Test(dataProvider = "Contact", dataProviderClass = JmsDataProvider.class)
    public void f(final String _xml)
        throws JAXBException
    {
        final JAXBContext jc = JAXBContext.newInstance(Contact.class);
        final Unmarshaller unmarschaller = jc.createUnmarshaller();
        final Source source = new StreamSource(new StringReader(_xml));
        final Contact contact = (Contact) unmarschaller.unmarshal(source);
        Assert.assertNotNull(contact);
    }

    @Test(dataProvider = "Contact", dataProviderClass = JmsDataProvider.class)
    public void create(final String _xml)
        throws Exception
    {
        final Queue queue = (Queue) getJmsServer().lookup(getQueueName());
        final MessageProducer producer = getSession().createProducer(queue);
        final TextMessage msg = getSession().createTextMessage();
        msg.setText(_xml);
        producer.send(msg);

        final MessageConsumer messageConsumer = getSession().createConsumer(queue);
        messageConsumer.setMessageListener(new ActionListener());
    }
}
