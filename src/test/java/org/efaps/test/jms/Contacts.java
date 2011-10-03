package org.efaps.test.jms;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.xerces.dom.DocumentImpl;
import org.efaps.esjp.jms.contacts.Contact;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class Contacts
{

    @Test
    public void f()
        throws JAXBException, ClassCastException, ClassNotFoundException, InstantiationException,
        IllegalAccessException
    {
        final JAXBContext jc = JAXBContext.newInstance(Contact.class);
        final Unmarshaller unmarschaller = jc.createUnmarshaller();

        final Document xmldoc = new DocumentImpl();
        final Element root = xmldoc.createElement("contact");
        xmldoc.appendChild(root);

        final Element name = xmldoc.createElement("name");
        root.appendChild(name);
        name.setTextContent("test_testName");

        final Element classifications = xmldoc.createElement("classifications");
        root.appendChild(classifications);
        final Element organisation = xmldoc.createElement("organisation");
        classifications.appendChild(organisation);
        final Element taxNumber = xmldoc.createElement("taxnumber");
        organisation.appendChild(taxNumber);
        taxNumber.setTextContent("test_123456789");


        final Element client = xmldoc.createElement("client");
        classifications.appendChild(client);
        final Element billingAdressStreet = xmldoc.createElement("billingadressstreet");
        client.appendChild(billingAdressStreet);
        billingAdressStreet.setTextContent("test_billingAdressStreet");
        final Element billingAdressCity = xmldoc.createElement("billingadresscity");
        client.appendChild(billingAdressCity);
        billingAdressCity.setTextContent("test_billingAdressCity");


        final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();

        final DOMImplementationLS impl =
                        (DOMImplementationLS) registry.getDOMImplementation("LS");

        final LSSerializer writer = impl.createLSSerializer();
        writer.getDomConfig().setParameter("format-pretty-print", true);

        final String str = writer.writeToString(xmldoc);

        final Source source = new StreamSource(new StringReader(str));

        final Contact contact = (Contact) unmarschaller.unmarshal(source);
        Assert.assertNotNull(contact);
    }
}
