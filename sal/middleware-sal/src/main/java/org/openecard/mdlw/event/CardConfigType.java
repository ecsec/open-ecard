/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.mdlw.event;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 *
 * @author Tobias Wich
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CardConfigType", propOrder = {
    "cardSpecs"
})
@XmlRootElement(name = "CardConfig")
public class CardConfigType {

    @XmlElement(name = "CardSpec")
    private ArrayList<CardSpec> cardSpecs;

    public List<CardSpec> getCardSpecs() {
	return cardSpecs;
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "CardSpecType", propOrder = {
	"middlewareName",
	"type",
	"atr",
	"mask"
    })
    public static class CardSpec {

	@XmlElement(name="MiddlewareName")
	private String middlewareName;

	@XmlElement(name="Type")
	private String type;

	@XmlElement(name = "ATR", type = String.class)
	@XmlJavaTypeAdapter(HexBinaryAdapter.class)
	@XmlSchemaType(name = "hexBinary")
	private byte[] atr;

	@XmlElement(name = "Mask", type = String.class)
	@XmlJavaTypeAdapter(HexBinaryAdapter.class)
	@XmlSchemaType(name = "hexBinary")
	private byte[] mask;

	public void setMiddlewareName(String middlewareName) {
	    this.middlewareName = middlewareName;
	}

	public String getMiddlewareName() {
	    return middlewareName;
	}

	public void setType(String type) {
	    this.type = type;
	}

	public String getType() {
	    return type;
	}

	public void setAtr(byte[] atr) {
	    this.atr = atr;
	}

	public byte[] getAtr() {
	    return atr;
	}

	public void setMask(byte[] mask) {
	    this.mask = mask;
	}

	public byte[] getMask() {
	    return mask;
	}

    }

}
