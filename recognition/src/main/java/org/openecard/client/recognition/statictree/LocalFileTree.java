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
