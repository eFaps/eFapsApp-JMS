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


package org.efaps.esjp.jms.msg.listener;

import java.io.StringReader;
import java.io.StringWriter;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.efaps.esjp.jms.actions.AbstractAction;
import org.efaps.esjp.jms.actions.Create;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ActionListener
    extends  AbstractContextListener
{

    /**
     * {@inheritDoc}
     */
    @Override
    public Object onSessionMessage(final Message _msg)
    {
        Object object = null;
        try {
            if (_msg instanceof TextMessage) {
                final TextMessage msg = (TextMessage) _msg;
                final String xml = msg.getText();
                System.out.println(xml);
                final JAXBContext jc = JAXBContext.newInstance(Create.class);
                final Unmarshaller unmarschaller = jc.createUnmarshaller();
                final Source source = new StreamSource(new StringReader(xml));
                object = unmarschaller.unmarshal(source);

                if (object instanceof AbstractAction) {
                    final AbstractAction action = (AbstractAction) object;
                    action.execute();
                }
            }
        } catch (final JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return object;
    }

    /**
     * {@inheritDoc}
     * @throws JMSException
     */
    @Override
    protected void respondSessionMessage(final TextMessage _msg,
                                         final Object _object)
        throws JMSException
    {
        try {
            final JAXBContext jc = JAXBContext.newInstance(Create.class);
            final Marshaller marschaller = jc.createMarshaller();
            marschaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            final StringWriter xml = new StringWriter();
            marschaller.marshal(_object, xml);
            _msg.setText(xml.toString());
        } catch (final JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
