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

package org.openecard.client.connector.common;

import org.openecard.client.common.I18n;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class ConnectorConstants {

    public static final String CHARSET = "UTF-8";
    private static I18n lang = I18n.getTranslation("connector");

    public enum ConnectorError {

	NOT_FOUND, BAD_REQUEST, TC_TOKEN_NOT_AVAILABLE, TC_TOKEN_REFUSED, INTERNAL_ERROR;

	@Override
	public String toString() {
	    return lang.translationForKey(this.name());
	}
    }

}
