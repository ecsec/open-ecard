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

package org.openecard.sal.protocol.eac.anytype;

import org.openecard.common.I18n;


/**
 *
 * @author Hans-Martin Haase
 */
public class ElementParsingException extends Exception {

    private static final I18n lang = I18n.getTranslation("tr03112");

    private final String msg;

    public ElementParsingException(String msg) {
	super(msg);
	this.msg = msg;
    }

    @Override
    public String getMessage() {
	return lang.getOriginalMessage(msg);
    }

    @Override
    public String getLocalizedMessage() {
	return lang.translationForKey(msg);
    }
}
