/****************************************************************************
 * Copyright (C) 2017-2018 ecsec GmbH.
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

package org.openecard.mdlw.sal.config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 *
 * @author Tobias Wich
 * @author Mike Prechtl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MiddlewareSpecType", propOrder = {
    "middlewareName",
    "libSpec",
    "builtinPinDialog",
    "cardConfig"
})
public class MiddlewareSpecType {

    @XmlAttribute(name="required", required = false)
    private boolean required = false;

    @XmlAttribute(name="disabled", required = false)
    private boolean disabled = false;

    @XmlElement(name="MiddlewareName", required = true)
    private String middlewareName;

    @XmlElement(name="LibSpec", required = true)
    private List<LibSpecType> libSpec;

    @XmlElement(name="BuiltinPinDialog", required = false)
    private boolean builtinPinDialog = true;

    @XmlElement(name = "CardConfig", required = true)
    private CardConfigType cardConfig;


    public boolean isRequired() {
	return required;
    }

    public void setRequired(boolean required) {
	this.required = required;
    }

    public boolean isDisabled() {
	return disabled;
    }

    public void setDisabled(boolean disabled) {
	this.disabled = disabled;
    }

    public String getMiddlewareName() {
	return middlewareName;
    }

    public void setMiddlewareName(String middlewareSALName) {
	this.middlewareName = middlewareSALName;
    }

    public List<LibSpecType> getLibSpec() {
	if (libSpec == null) {
	    libSpec = new ArrayList<>();
	}
	return libSpec;
    }

    public boolean isBuiltinPinDialog() {
	return builtinPinDialog;
    }

    public void setBuiltinPinDialog(boolean builtinPinDialog) {
	this.builtinPinDialog = builtinPinDialog;
    }

    public CardConfigType getCardConfig() {
	return cardConfig;
    }

    public void setCardConfig(CardConfigType cardConfig) {
	this.cardConfig = cardConfig;
    }

}
