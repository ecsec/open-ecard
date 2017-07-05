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

import iso.std.iso_iec._24727.tech.schema.CardTypeType;
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
import javax.xml.datatype.XMLGregorianCalendar;
import oasis.names.tc.dss._1_0.core.schema.InternationalStringType;


/**
 * CardConfig which is used to identify a Card. The Card Infos are set in the corresponding CardInfo-Template.
 *
 * @author Tobias Wich
 * @author Mike Prechtl
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
        "objectIdentifier",
	"cardTypeName",
        "version",
        "status",
        "date",
	"atr",
	"mask",
        "cardImageName"
    })
    public static class CardSpec {

	@XmlElement(name="MiddlewareName")
	private String middlewareName;

        @XmlElement(name = "ObjectIdentifier", namespace = "urn:iso:std:iso-iec:24727:tech:schema", required = true)
        @XmlSchemaType(name = "anyURI")
        private String objectIdentifier;

        @XmlElement(name = "CardTypeName", namespace = "urn:iso:std:iso-iec:24727:tech:schema")
        private List<InternationalStringType> cardTypeName;

        @XmlElement(name = "Version", namespace = "urn:iso:std:iso-iec:24727:tech:schema")
        private CardTypeType.Version version;

        @XmlElement(name = "Status", namespace = "urn:iso:std:iso-iec:24727:tech:schema")
        private String status;

        @XmlElement(name = "Date", namespace = "urn:iso:std:iso-iec:24727:tech:schema")
        @XmlSchemaType(name = "date")
        private XMLGregorianCalendar date;

	@XmlElement(name = "ATR", type = String.class)
	@XmlJavaTypeAdapter(HexBinaryAdapter.class)
	@XmlSchemaType(name = "hexBinary")
	private byte[] atr;

	@XmlElement(name = "Mask", type = String.class)
	@XmlJavaTypeAdapter(HexBinaryAdapter.class)
	@XmlSchemaType(name = "hexBinary")
	private byte[] mask;

        @XmlElement(name = "CardImage")
        private String cardImageName;

	public void setMiddlewareName(String middlewareName) {
	    this.middlewareName = middlewareName;
	}

	public String getMiddlewareName() {
	    return middlewareName;
	}

        public String getObjectIdentifier() {
            return objectIdentifier;
        }

        public void setObjectIdentifier(String objectIdentifier) {
            this.objectIdentifier = objectIdentifier;
        }

        public List<InternationalStringType> getCardTypeName() {
            return cardTypeName;
        }

        public void setCardTypeName(List<InternationalStringType> cardTypeName) {
            this.cardTypeName = cardTypeName;
        }

        public CardTypeType.Version getVersion() {
            return version;
        }

        public void setVersion(CardTypeType.Version version) {
            this.version = version;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public XMLGregorianCalendar getDate() {
            return date;
        }

        public void setDate(XMLGregorianCalendar date) {
            this.date = date;
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

        public String getCardImageName() {
            return cardImageName;
        }

        public void setCardImageName(String imageName) {
            this.cardImageName = imageName;
        }

    }

}
