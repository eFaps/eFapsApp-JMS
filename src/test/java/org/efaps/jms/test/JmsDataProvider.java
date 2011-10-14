/*
 * Copyright 2003 - 2011 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.jms.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.xerces.dom.DocumentImpl;
import org.testng.annotations.DataProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class JmsDataProvider
{

    @DataProvider(name = "Contact")
    public static Iterator<Object[]> createXML(final Method _method)
        throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        final Document xmldoc = JmsDataProvider.getContact();

        if (_method.getName().contains("create")) {
            final Element action = xmldoc.createElement("create");
            final Element objectsElement = xmldoc.createElement("objects");
            action.appendChild(objectsElement);

            final Node root = xmldoc.getFirstChild();
            xmldoc.removeChild(root);
            objectsElement.appendChild(root);
            xmldoc.appendChild(action);
        }

        final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
        final LSSerializer writer = impl.createLSSerializer();
        writer.getDomConfig().setParameter("format-pretty-print", true);

        final String str = writer.writeToString(xmldoc);

        final List<Object[]> objects = new ArrayList<Object[]>();
        objects.add(new Object[] { str });
        return objects.iterator();
    }

    private static Document getContact()
    {
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
        return xmldoc;
    }

    private static Document getLogin(final String _userName,
                                     final String _password)
    {
        final Document xmldoc = new DocumentImpl();
        final Element login = xmldoc.createElement("login");
        xmldoc.appendChild(login);

        final Element username = xmldoc.createElement("username");
        username.setTextContent(_userName);
        login.appendChild(username);

        final Element password = xmldoc.createElement("password");
        password.setTextContent(_password);
        login.appendChild(password);

        final Element applicationkey = xmldoc.createElement("applicationkey");
        applicationkey.setTextContent("jmsTest");
        login.appendChild(applicationkey);
        return xmldoc;
    }

    @DataProvider(name = "login")
    public static Iterator<Object[]> createLoginXML()
        throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException

    {
        final Document xmldoc = JmsDataProvider.getLogin("Administrator", "Administrator");

        final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
        final LSSerializer writer = impl.createLSSerializer();
        writer.getDomConfig().setParameter("format-pretty-print", true);

        final String str = writer.writeToString(xmldoc);

        final List<Object[]> objects = new ArrayList<Object[]>();
        objects.add(new Object[] { str });
        return objects.iterator();
    }

}
