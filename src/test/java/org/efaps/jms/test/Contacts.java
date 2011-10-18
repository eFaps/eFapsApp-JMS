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

import org.efaps.esjp.jms.actions.Create;
import org.efaps.esjp.jms.actions.Print;
import org.efaps.esjp.jms.contacts.Contact;
import org.efaps.esjp.jms.msg.listener.AbstractContextListener;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

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
        final String xml = ((TextMessage) respondMsg).getText();
        messageConsumer.close();

        final JAXBContext jc = JAXBContext.newInstance(Create.class, Print.class);
        final Unmarshaller unmarschaller = jc.createUnmarshaller();
        final Source source = new StreamSource(new StringReader(xml));
        final Create create = (Create) unmarschaller.unmarshal(source);

        final Contact contact = (Contact) create.getObjects().get(0);
        final Document contactDoc = JmsDataProvider.getContact(contact.getOid());
        final Document printDoc = JmsDataProvider.getActionDoc("print", contactDoc);

        final String msgText = JmsDataProvider.serialize(printDoc);

        final TextMessage msg2 = getSession().createTextMessage();
        msg2.setJMSReplyTo(getRespondQueue());
        msg2.setStringProperty(AbstractContextListener.SESSIONKEY_PROPNAME, getSessionKey());
        msg2.setText(msgText);
        producer.send(msg2);

        final MessageConsumer messageConsumer2 = getSession().createConsumer(getRespondQueue());
        final Message respondMsg2 = messageConsumer2.receive();
        final String xml2 = ((TextMessage) respondMsg2).getText();
        System.out.println(xml2);
        messageConsumer2.close();
        final Source source2 = new StreamSource(new StringReader(xml2));
        final Unmarshaller unmarschaller2 = jc.createUnmarshaller();
        final Print print = (Print) unmarschaller2.unmarshal(source2);
        final Contact contact2 = (Contact) print.getObjects().get(0);
        Assert.assertEquals(contact.getOid(), contact2.getOid());
        Assert.assertEquals(contact.getName().getValue(), contact2.getName().getValue());

    }
}
