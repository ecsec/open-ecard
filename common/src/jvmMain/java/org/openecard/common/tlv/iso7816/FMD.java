/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.common.tlv.iso7816;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.openecard.common.tlv.Parser;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.util.Pair;


/**
 * The class model an FMD as data type.
 *
 * @author Hans-Martin Haase
 */
public class FMD {

    private final TLV tlv;
    
    boolean content = true;
    private byte[] discretionaryData;
    private byte[] discretionaryDataTemplate;
    private List<ApplicationTemplate> applicationTemplates;
    private String applicationLabel;
    private byte[] fileReference;
    private String uniformResourceLocator;
    private List<Pair<byte[], byte[]>> references;
    private List<TLV> proprietaryInformation;
    
    /**
     * Creats an FMD object.
     *
     * @param tlv
     * @throws TLVException
     * @throws UnsupportedEncodingException
     */
    public FMD(TLV tlv) throws TLVException, UnsupportedEncodingException {
	this.tlv = tlv;
	if (tlv.getTagNumWithClass() != 0x64) {
	    throw new TLVException("Data doesn't represent an FCP.");
	}

	if (tlv.getValue().length == 0) {
	    content = false;
	} else {
	    TLV child = tlv.getChild();

	    if (child.getTagNumWithClass() == 0x61) {
		Parser p = new Parser(child);
		applicationTemplates = new LinkedList<>();
		while (p.match(0x61)) {
		    if (p.match(0x61)) {
			applicationTemplates.add(new ApplicationTemplate(p.next(0)));
		    }
		}
	    } else if (child.getTagNumWithClass() == 0x53) {
		discretionaryData = child.getValue();
	    } else if (child.getTagNumWithClass() == 0x73) {
		discretionaryDataTemplate = child.getValue();
	    } else if (child.getTagNumWithClass() == 0x5F50) {
		uniformResourceLocator = new String(child.getValue(), "ASCII-US");
	    } else if (child.getTagNumWithClass() == 0x50) {
		applicationLabel = new String(child.getValue());
	    } else if (child.getTagNumWithClass() == 0x51) {
		fileReference = child.getValue();
	    } else if (child.getTagNumWithClass() == 0xA2) {
		Parser p = new Parser(child);
		references = new ArrayList<>();
		while (p.match(0x88) || p.match(0x51)) {
		    byte[] shortRef = null;
		    byte[] fileRef = null;

		    if (p.match(0x88)) {
			shortRef = p.next(0).getValue();
		    }

		    if (p.match(0x51)) {
			fileRef = p.next(0).getValue();
		    }

		    Pair<byte[], byte[]> refPair = new Pair<>(shortRef, fileRef);
		    references.add(refPair);
		}
	    } else if (child.getTagNumWithClass() == 0x85) {
		Parser p = new Parser(child);
		proprietaryInformation = new ArrayList<>();
		while (p.match(0x85)) {
		    proprietaryInformation.add(p.next(0));
		}
	    }
	}
    }
    
    /**
     * Get the uniform resource locator which points to the part of the software required in the interface device to
     * communicate with the application in the card.
     * 
     * @return The URL contained in the FMD.
     */
    public String getURL() {
	return uniformResourceLocator;
    }
    
    /**
     * Get the discretionary data if available.
     * 
     * @return The Discretionary Data contained in the FMD.
     */
    public byte[] getDiscretionaryData() {
	return discretionaryData;
    }
    
    /**
     * Get the discretionary data template which may contain proprietary information.
     * 
     * @return A discretionary data template as byte array.
     */
    public byte[] getDiscretionaryDataTemplate() {
	return discretionaryDataTemplate;
    }
    
    /**
     * Get the name of the application.
     * 
     * @return A string containing the name of the application.
     */
    public String getApplicationLabel() {
	return applicationLabel;
    }
    
    /**
     * Get the file reference of the application or file.
     * 
     * @return A file reference to the current application.
     */
    public byte[] getFileReference() {
	return fileReference;
    }
    
    /**
     * Get a list of pairs with short file reference and file reference to files contained in the current application.
     * 
     * @return A list of pairs where the first component of the pair is the short reference and the second one the full 
     * file reference.
     */
    public List<Pair<byte[], byte[]>> getReferences() {
	return references;
    }
    
    
    /**
     * Get a list with proprietary information objects contained in the FMD if available.
     * 
     * @return List of TLV object which contain the proprietary information.
     */
    public List<TLV> getProprietaryInformation() {
	return proprietaryInformation;
    }
    
    /**
     * The method indicates whether the FMD has content or not.
     * 
     * @return True if the FMD contains content else false.
     */
    public boolean hasContent() {
	return content;
    }
    
    /**
     * Get, if available, the application templates in the FMD.
     * 
     * @return A list of ApplicationTemplates.
     */
    public List<ApplicationTemplate> getApplicationTemplates() {
	return applicationTemplates;
    }
}
