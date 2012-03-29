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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.esjp.common.file.FileUtil;
import org.efaps.esjp.jms.actions.IAction;
import org.efaps.util.EFapsException;

/**
 * This class must be replaced for customization, therefore it is left empty.
 * Functional description can be found in the related "<code>_base</code>"
 * class.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("1af5fdd5-de2c-4aea-8c17-e0fb8d524c55")
@EFapsRevision("$Rev$")
public abstract class SessionActionListener_Base
    extends AbstractSessionListener
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
                AbstractSessionListener_Base.LOG.debug("unmarshalling: {} ", xml);
                final JAXBContext jc = JAXBContext.newInstance(getClasses());
                final Unmarshaller unmarschaller = jc.createUnmarshaller();
                final Source source = new StreamSource(new StringReader(xml));
                object = unmarschaller.unmarshal(source);
            } else if (_msg instanceof BytesMessage) {
                final FileUtil fileUtil = new FileUtil();
                final File file = fileUtil.getFile(_msg
                                .getStringProperty(AbstractSessionListener_Base.FILENAME_PROPNAME));
                final FileOutputStream fos = new FileOutputStream(file);
                final BufferedOutputStream outBuf = new BufferedOutputStream(fos);
                int i;
                while ((i = ((BytesMessage) _msg).readInt()) != -1) {
                    outBuf.write(i);
                }
                outBuf.close();
                fos.close();
            }
            if (object instanceof IAction) {
                final IAction action = (IAction) object;
                object = action.execute();
            }
        } catch (final JMSException e) {
            AbstractSessionListener_Base.LOG.error("JMSException", e);
        } catch (final JAXBException e) {
            AbstractSessionListener_Base.LOG.error("JAXBException", e);
        } catch (final EFapsException e) {
            AbstractSessionListener_Base.LOG.error("EFapsException", e);
        } catch (final FileNotFoundException e) {
            AbstractSessionListener_Base.LOG.error("FileNotFoundException", e);
        } catch (final IOException e) {
            AbstractSessionListener_Base.LOG.error("IOException", e);
        }
        return object;
    }

    /**
     * {@inheritDoc}
     *
     * @throws JMSException
     */
    @Override
    protected void respondSessionTextMessage(final TextMessage _msg,
                                             final Object _object)
        throws JMSException
    {
        try {
            final JAXBContext jc = getJAXBContext();
            final Marshaller marschaller = jc.createMarshaller();
            marschaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            final StringWriter xml = new StringWriter();
            marschaller.marshal(_object, xml);
            _msg.setText(xml.toString());
            AbstractSessionListener_Base.LOG.debug("setting text: {}", xml);
        } catch (final JAXBException e) {
            AbstractSessionListener_Base.LOG.error("JAXBException", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void respondSessionBytestMessage(final BytesMessage _msg,
                                               final Object _object)
        throws JMSException
    {
        if (_object instanceof File) {
            final File fileToPublish = (File) _object;

            InputStream in;
            try {
                in = new FileInputStream(fileToPublish);
                final BufferedInputStream inBuf = new BufferedInputStream(in);
                int i;
                while ((i = inBuf.read()) != -1) {
                    _msg.writeInt(i);
                }
                // adding an eof
                _msg.writeInt(-1);
            } catch (final FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }
}
