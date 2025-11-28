/*
 * Copyright (C) 2025 ecsec GmbH.
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
 */

package org.openecard.richclient.tr03124.ui

import org.openecard.i18n.I18N
import org.openecard.sc.pace.cvc.ReadAccess
import org.openecard.sc.pace.cvc.SpecialFunction
import org.openecard.sc.pace.cvc.WriteAccess

fun ReadAccess.stringResource() =
	when (this) {
		ReadAccess.DG01 -> I18N.strings.eac_dg01
		ReadAccess.DG02 -> I18N.strings.eac_dg02
		ReadAccess.DG03 -> I18N.strings.eac_dg03
		ReadAccess.DG04 -> I18N.strings.eac_dg04
		ReadAccess.DG05 -> I18N.strings.eac_dg05
		ReadAccess.DG06 -> I18N.strings.eac_dg06
		ReadAccess.DG07 -> I18N.strings.eac_dg07
		ReadAccess.DG08 -> I18N.strings.eac_dg08
		ReadAccess.DG09 -> I18N.strings.eac_dg09
		ReadAccess.DG10 -> I18N.strings.eac_dg10
		ReadAccess.DG11 -> I18N.strings.eac_dg11
		ReadAccess.DG12 -> I18N.strings.eac_dg12
		ReadAccess.DG13 -> I18N.strings.eac_dg13
		ReadAccess.DG14 -> I18N.strings.eac_dg14
		ReadAccess.DG15 -> I18N.strings.eac_dg15
		ReadAccess.DG16 -> I18N.strings.eac_dg16
		ReadAccess.DG17 -> I18N.strings.eac_dg17
		ReadAccess.DG18 -> I18N.strings.eac_dg18
		ReadAccess.DG19 -> I18N.strings.eac_dg19
		ReadAccess.DG20 -> I18N.strings.eac_dg20
		ReadAccess.DG21 -> I18N.strings.eac_dg21
		ReadAccess.DG22 -> I18N.strings.eac_dg22
	}

fun WriteAccess.stringResource() =
	when (this) {
		WriteAccess.DG17 -> I18N.strings.eac_dg17
		WriteAccess.DG18 -> I18N.strings.eac_dg18
		WriteAccess.DG19 -> I18N.strings.eac_dg19
		WriteAccess.DG20 -> I18N.strings.eac_dg20
		WriteAccess.DG21 -> I18N.strings.eac_dg21
		WriteAccess.DG22 -> I18N.strings.eac_dg22
	}

fun SpecialFunction.stringResource() =
	when (this) {
		SpecialFunction.INSTALL_QUALIFIED_CERTIFICATE -> I18N.strings.eac_install_qualified_certificate
		SpecialFunction.INSTALL_CERTIFICATE -> I18N.strings.eac_install_certificate
		SpecialFunction.PIN_MANAGEMENT -> I18N.strings.eac_pin_management
		SpecialFunction.CAN_ALLOWED -> I18N.strings.eac_can_allowed
		SpecialFunction.PRIVILEGED_TERMINAL -> I18N.strings.eac_privileged_terminal
		SpecialFunction.RESTRICTED_IDENTIFICATION -> I18N.strings.eac_restricted_identification
		SpecialFunction.COMMUNITY_ID_VERIFICATION -> I18N.strings.eac_community_id_verification
		SpecialFunction.AGE_VERIFICATION -> I18N.strings.eac_age_verification
		SpecialFunction.PSEUDONYMOUS_SIGNATURE_AUTHENTICATION -> I18N.strings.eac_pseudonymous_signature_authentication
	}
