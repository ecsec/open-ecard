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
import org.openecard.addon.AbstractFactory
import org.openecard.addon.Context

/**
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
class SALProtocolProxy(
	protocolClass: String,
	classLoader: ClassLoader,
) : AbstractFactory<SALProtocol>(protocolClass, classLoader),
	SALProtocol {
	private var c: SALProtocol? = null

	override val internalData: MutableMap<String, Any>
		get() = c!!.internalData

	override fun hasNextStep(aFunction: FunctionType?): Boolean = c!!.hasNextStep(aFunction)

	override val isFinished: Boolean
		get() = c!!.isFinished

	override fun cardApplicationStartSession(aParam: CardApplicationStartSession): CardApplicationStartSessionResponse? =
		c!!.cardApplicationStartSession(aParam)

	override fun cardApplicationEndSession(aParam: CardApplicationEndSession): CardApplicationEndSessionResponse? =
		c!!.cardApplicationEndSession(aParam)

	override fun encipher(aParam: Encipher): EncipherResponse? = c!!.encipher(aParam)

	override fun decipher(aParam: Decipher): DecipherResponse? = c!!.decipher(aParam)

	override fun getRandom(aParam: GetRandom): GetRandomResponse? = c!!.getRandom(aParam)

	override fun hash(aParam: Hash): HashResponse? = c!!.hash(aParam)

	override fun sign(aParam: Sign): SignResponse? = c!!.sign(aParam)

	override fun verifySignature(aParam: VerifySignature): VerifySignatureResponse? = c!!.verifySignature(aParam)

	override fun verifyCertificate(aParam: VerifyCertificate): VerifyCertificateResponse? = c!!.verifyCertificate(aParam)

	override fun didCreate(aParam: DIDCreate): DIDCreateResponse? = c!!.didCreate(aParam)

	override fun didUpdate(aParam: DIDUpdate): DIDUpdateResponse? = c!!.didUpdate(aParam)

	override fun didDelete(aParam: DIDDelete): DIDDeleteResponse? = c!!.didDelete(aParam)

	override fun didAuthenticate(aParam: DIDAuthenticate): DIDAuthenticateResponse? = c!!.didAuthenticate(aParam)

	override fun needsSM(): Boolean = c!!.needsSM()

	override fun applySM(aCommandAPDU: ByteArray?): ByteArray? = c!!.applySM(aCommandAPDU)

	override fun removeSM(aResponseAPDU: ByteArray?): ByteArray? = c!!.removeSM(aResponseAPDU)

	override fun init(aCtx: Context) {
		c = loadInstance(aCtx, SALProtocol::class.java)
	}

	override fun destroy(force: Boolean) {
		c!!.destroy(force)
	}
}
