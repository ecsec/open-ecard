/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

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
