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

package org.openecard.client.recognition.statictree;

import iso.std.iso_iec._24727.tech.schema.GetRecognitionTree;
import iso.std.iso_iec._24727.tech.schema.GetRecognitionTreeResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.openecard.client.ws.WSMarshaller;
import org.openecard.client.ws.WSMarshallerException;
import org.xml.sax.SAXException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class LocalFileTree implements org.openecard.ws.GetRecognitionTree {

    private final WSMarshaller marshaller;
    private final GetRecognitionTreeResponse response;

    public LocalFileTree(WSMarshaller marshaller) throws WSMarshallerException, SAXException, IOException {
	this.marshaller = marshaller;
	// load file
	InputStream in = LocalFileTree.class.getResourceAsStream("StaticTree.xml");
	if (in == null) {
	    in = LocalFileTree.class.getResourceAsStream("/StaticTree.xml");
	}
	if (in == null) {
	    throw new FileNotFoundException("File StaticTree.xml which is needed for local tree repository can not be found.");
	}
	response = (GetRecognitionTreeResponse) marshaller.unmarshal(marshaller.str2doc(in));
    }


    @Override
    public GetRecognitionTreeResponse getRecognitionTree(GetRecognitionTree parameters) {
	return response;
    }

}
