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

package org.openecard.client.sal.protocol.eac.gui;

import java.util.HashMap;


/**
 * Provides a container to store data for the GUI and user interaction.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class GUIContentMap {

    public enum ELEMENT {

	CERTIFICATE,
	CERTIFICATE_DESCRIPTION,
	REQUIRED_CHAT,
	OPTIONAL_CHAT,
	SELECTED_CHAT,
	PIN_ID,
	PIN;
    }

    private final HashMap<String, Object> map = new HashMap<String, Object>();

    /**
     * Creates a new GUI Content map.
     */
    public GUIContentMap() {
    }

    /**
     * Adds a new value to the GUI content map.
     *
     * @param id ID
     * @param value Value
     */
    public void add(ELEMENT id, Object value) {
	add(id.name(), value);
    }

    /**
     * Adds a new value to the GUI content map.
     *
     * @param id ID
     * @param value Value
     */
    public void add(String id, Object value) {
	map.put(id, value);
    }

    /**
     * Returns the corresponding value.
     *
     * @param id ID
     * @return Value
     */
    public Object get(ELEMENT id) {
	return get(id.name());
    }

    /**
     * Returns the corresponding value.
     *
     * @param id ID
     * @return Value
     */
    public Object get(String id) {
	return map.get(id);
    }

}
