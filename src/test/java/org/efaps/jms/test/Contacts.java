package org.efaps.jms.test;

import java.io.StringReader;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.efaps.esjp.jms.contacts.Contact;
import org.efaps.esjp.jms.msg.listener.AbstractContextListener;
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

    @Test(dataProvider = "Contact", dataProviderClass = JmsDataProvider.class, dependsOnMethods = { "login" })
    public void create(final String _xml)
        throws Exception
    {
        System.out.println(getSessionKey());
        // server sends request
        final MessageProducer producer = getRequestProducer();
        final TextMessage msg = getSession().createTextMessage();
        msg.setJMSReplyTo(getRespondQueue());
        msg.setStringProperty(AbstractContextListener.SESSIONKEY_PROPNAME, getSessionKey());
        msg.setText(_xml);
        producer.send(msg);

        final MessageConsumer messageConsumer = getSession().createConsumer(getRespondQueue());
        final Message respondMsg = messageConsumer.receive();
        System.out.println(((TextMessage) respondMsg).getText());
        messageConsumer.close();
    }
}
