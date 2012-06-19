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

package org.openecard.client.gui.definition;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SignatureField implements InputInfoUnit, OutputInfoUnit {

    private String name;
    private String text;
    private byte[] value;

    /**
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * @return the text
     */
    public String getText() {
	return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
	this.text = text;
    }

    /**
     * @return the value
     */
    public byte[] getValue() {
	return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(byte[] value) {
	this.value = value;
    }


    @Override
    public InfoUnitElementType type() {
	return InfoUnitElementType.SIGNAUTRE_FIELD;
    }

}
