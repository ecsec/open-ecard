package org.openecard.sc.pace.crypto

import org.openecard.sc.pace.asn1.StandardizedDomainParameters
import org.openecard.sc.pace.crypto.whyoleg.crypto
import org.openecard.sc.pace.crypto.whyoleg.ecdhAgreement

actual fun ecdhAgreement(curve: StandardizedDomainParameters.Curve): KeyAgreement = crypto.ecdhAgreement(curve)
