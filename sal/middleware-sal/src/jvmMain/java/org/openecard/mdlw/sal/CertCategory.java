/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.mdlw.sal;

import javax.annotation.Nonnull;

/**
 *
 * @author Tobias Wich
 */
public enum CertCategory {

    CK_CERTIFICATE_CATEGORY_UNSPECIFIED(0x00000000L),
    CK_CERTIFICATE_CATEGORY_TOKEN_USER(0x00000001L),
    CK_CERTIFICATE_CATEGORY_AUTHORITY(0x00000002L),
    CK_CERTIFICATE_CATEGORY_OTHER_ENTITY(0x80000000L);

    private final long num;

    private CertCategory(long num) {
	this.num = num;
    }

    @Nonnull
    public static CertCategory forCategoryType(long num) {
	for (CertCategory next : values()) {
	    if (next.num == num) {
		return next;
	    }
	}
	return CK_CERTIFICATE_CATEGORY_UNSPECIFIED;
    }

}
