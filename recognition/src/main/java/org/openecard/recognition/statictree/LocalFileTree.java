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

package org.openecard.recognition.statictree;

import iso.std.iso_iec._24727.tech.schema.GetRecognitionTree;
import iso.std.iso_iec._24727.tech.schema.GetRecognitionTreeResponse;
import java.io.IOException;
import java.io.InputStream;
import org.openecard.common.util.FileUtils;
import org.openecard.ws.WSMarshaller;
import org.openecard.ws.WSMarshallerException;
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
	InputStream in = FileUtils.resolveResourceAsStream(LocalFileTree.class, "/StaticTree.xml");
	response = (GetRecognitionTreeResponse) marshaller.unmarshal(marshaller.str2doc(in));
    }


    @Override
    public GetRecognitionTreeResponse getRecognitionTree(GetRecognitionTree parameters) {
	return response;
    }

}
