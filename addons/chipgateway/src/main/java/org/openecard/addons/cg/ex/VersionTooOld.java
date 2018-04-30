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

package org.openecard.addons.cg.ex;

import org.openecard.common.I18nKey;


/**
 *
 * @author Tobias Wich
 */
public class VersionTooOld extends RedirectionBaseError {

    private static final long serialVersionUID = 1L;

    public VersionTooOld(String errorUrl, String msg) {
	super(errorUrl, msg);
    }

    public VersionTooOld(String errorUrl, String msg, Throwable ex) {
	super(errorUrl, msg, ex);
    }

    public VersionTooOld(String errorUrl, Throwable ex) {
        super(errorUrl, ex);
    }

    public VersionTooOld(String errorUrl, I18nKey key, Object... params) {
	super(errorUrl, key, params);
    }

    public VersionTooOld(String errorUrl, I18nKey key, Throwable cause, Object... params) {
	super(errorUrl, key, cause, params);
    }

}
