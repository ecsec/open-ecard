package org.openecard.demo.util

import org.openecard.sc.pace.cvc.ReadAccess
import org.openecard.sc.pace.cvc.SpecialFunction
import org.openecard.sc.pace.cvc.WriteAccess

fun ReadAccess.toLabel() =
	when (this) {
		ReadAccess.DG01 -> "Document type"
		ReadAccess.DG02 -> "Issuing country"
		ReadAccess.DG03 -> "Valid until"
		ReadAccess.DG04 -> "Given name(s)"
		ReadAccess.DG05 -> "Family name"
		ReadAccess.DG06 -> "Religious/artistic name"
		ReadAccess.DG07 -> "Doctoral degree"
		ReadAccess.DG08 -> "Date of birth"
		ReadAccess.DG09 -> "Place of birth"
		ReadAccess.DG10 -> "Nationality"
		ReadAccess.DG11 -> "Sex"
		ReadAccess.DG12 -> "Optional Data"
		ReadAccess.DG13 -> "Birth name"
		ReadAccess.DG14 -> "Written Signature"
		ReadAccess.DG15 -> "Date of issuance"
		ReadAccess.DG16 -> "RFU"
		ReadAccess.DG17 -> "Address"
		ReadAccess.DG18 -> "Community ID"
		ReadAccess.DG19 -> "Auxiliary Conditions"
		ReadAccess.DG20 -> "Auxiliary Conditions II"
		ReadAccess.DG21 -> "Phone Number"
		ReadAccess.DG22 -> "Email Address"
	}

fun WriteAccess.toLabel() =
	when (this) {
		WriteAccess.DG17 -> "Address"
		WriteAccess.DG18 -> "Community ID"
		WriteAccess.DG19 -> "Auxiliary Conditions"
		WriteAccess.DG20 -> "Auxiliary Conditions II"
		WriteAccess.DG21 -> "Phone Number"
		WriteAccess.DG22 -> "Email Address"
	}

fun SpecialFunction.toLabel() =
	when (this) {
		SpecialFunction.INSTALL_QUALIFIED_CERTIFICATE -> "Install signature certificate"
		SpecialFunction.INSTALL_CERTIFICATE -> "Install non-qualified signature certificate"
		SpecialFunction.PIN_MANAGEMENT -> "PIN Management"
		SpecialFunction.CAN_ALLOWED -> "On-Site Verification"
		SpecialFunction.PRIVILEGED_TERMINAL -> "Privileged Terminal"
		SpecialFunction.RESTRICTED_IDENTIFICATION -> "Restricted Identification"
		SpecialFunction.COMMUNITY_ID_VERIFICATION -> "Address verification"
		SpecialFunction.AGE_VERIFICATION -> "Age verification (≥ %d)"
		SpecialFunction.PSEUDONYMOUS_SIGNATURE_AUTHENTICATION -> "Pseudonymous Signature authentication"
	}
