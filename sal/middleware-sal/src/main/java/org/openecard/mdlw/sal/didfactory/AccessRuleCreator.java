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

package org.openecard.mdlw.sal.didfactory;

import iso.std.iso_iec._24727.tech.schema.AccessRuleType;
import iso.std.iso_iec._24727.tech.schema.ActionNameType;
import iso.std.iso_iec._24727.tech.schema.SecurityConditionType;

public class AccessRuleCreator {

    public static String serviceName;
    public static ActionNameType actionNameType;
    public static Boolean securityConditionAlways;
    public static Boolean securityConditionNever;

    public void setServiceName(String sN) {
        this.serviceName = sN;
    }

    public void setActionNameType(ActionNameType aN) {
        this.actionNameType = aN;
    }

    public void setSecurityConditionAlways(Boolean sC) {
        this.securityConditionAlways = sC;
    }

    public void setSecurityConditionNever(Boolean sC) {
        this.securityConditionNever = sC;
    }

    public AccessRuleType create() {
        AccessRuleType art = new AccessRuleType();

        art.setCardApplicationServiceName(serviceName);

        art.setAction(actionNameType);

        SecurityConditionType security = new SecurityConditionType();
        if (securityConditionAlways != null) {
            security.setAlways(securityConditionAlways);
        }

        if (securityConditionNever != null) {
            security.setNever(securityConditionNever);
        }

        art.setSecurityCondition(security);

        return art;
    }
}
