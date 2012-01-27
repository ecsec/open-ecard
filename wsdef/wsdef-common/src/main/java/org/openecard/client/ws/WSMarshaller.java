/*
 * Copyright 2012 Tobias Wich ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.ws;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.transform.TransformerException;
import org.openecard.client.ws.soap.SOAPException;
import org.openecard.client.ws.soap.SOAPMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface WSMarshaller {

    public Document str2doc(String docStr) throws SAXException;
    public Document str2doc(InputStream docStr) throws SAXException, IOException;
    public String doc2str(Node doc) throws TransformerException;

    public Object unmarshal(Node n) throws MarshallingTypeException, WSMarshallerException;
    public Document marshal(Object o) throws MarshallingTypeException;

    public SOAPMessage doc2soap(Document envDoc) throws SOAPException;
    public SOAPMessage add2soap(Document content) throws SOAPException;

}
