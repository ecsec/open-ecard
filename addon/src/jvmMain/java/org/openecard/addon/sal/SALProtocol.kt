/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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
package org.openecard.addon.sal

import iso.std.iso_iec._24727.tech.schema.CardApplicationEndSession
import iso.std.iso_iec._24727.tech.schema.CardApplicationEndSessionResponse
import iso.std.iso_iec._24727.tech.schema.CardApplicationStartSession
import iso.std.iso_iec._24727.tech.schema.CardApplicationStartSessionResponse
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse
import iso.std.iso_iec._24727.tech.schema.DIDCreate
import iso.std.iso_iec._24727.tech.schema.DIDCreateResponse
import iso.std.iso_iec._24727.tech.schema.DIDDelete
import iso.std.iso_iec._24727.tech.schema.DIDDeleteResponse
import iso.std.iso_iec._24727.tech.schema.DIDUpdate
import iso.std.iso_iec._24727.tech.schema.DIDUpdateResponse
import iso.std.iso_iec._24727.tech.schema.Decipher
import iso.std.iso_iec._24727.tech.schema.DecipherResponse
import iso.std.iso_iec._24727.tech.schema.Encipher
import iso.std.iso_iec._24727.tech.schema.EncipherResponse
import iso.std.iso_iec._24727.tech.schema.GetRandom
import iso.std.iso_iec._24727.tech.schema.GetRandomResponse
import iso.std.iso_iec._24727.tech.schema.Hash
import iso.std.iso_iec._24727.tech.schema.HashResponse
import iso.std.iso_iec._24727.tech.schema.Sign
import iso.std.iso_iec._24727.tech.schema.SignResponse
import iso.std.iso_iec._24727.tech.schema.VerifyCertificate
import iso.std.iso_iec._24727.tech.schema.VerifyCertificateResponse
import iso.std.iso_iec._24727.tech.schema.VerifySignature
import iso.std.iso_iec._24727.tech.schema.VerifySignatureResponse
import org.openecard.addon.LifecycleTrait

/**
 *
 * @author Tobias Wich
 */
interface SALProtocol : LifecycleTrait {
	val internalData: MutableMap<String, Any>

	fun hasNextStep(aFunction: FunctionType?): Boolean

	val isFinished: Boolean

	fun cardApplicationStartSession(aParam: CardApplicationStartSession): CardApplicationStartSessionResponse?

	fun cardApplicationEndSession(aParam: CardApplicationEndSession): CardApplicationEndSessionResponse?

	fun encipher(aParam: Encipher): EncipherResponse?

	fun decipher(aParam: Decipher): DecipherResponse?

	fun getRandom(aParam: GetRandom): GetRandomResponse?

	fun hash(aParam: Hash): HashResponse?

	fun sign(aParam: Sign): SignResponse?

	fun verifySignature(aParam: VerifySignature): VerifySignatureResponse?

	fun verifyCertificate(aParam: VerifyCertificate): VerifyCertificateResponse?

	fun didCreate(aParam: DIDCreate): DIDCreateResponse?

	fun didUpdate(aParam: DIDUpdate): DIDUpdateResponse?

	fun didDelete(aParam: DIDDelete): DIDDeleteResponse?

	fun didAuthenticate(aParam: DIDAuthenticate): DIDAuthenticateResponse?

	fun needsSM(): Boolean

	fun applySM(aCommandAPDU: ByteArray?): ByteArray?

	fun removeSM(aResponseAPDU: ByteArray?): ByteArray?
}
