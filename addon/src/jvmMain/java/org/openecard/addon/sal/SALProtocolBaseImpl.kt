/****************************************************************************
 * Copyright (C) 2013-2014 ecsec GmbH.
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
import iso.std.iso_iec._24727.tech.schema.RequestType
import iso.std.iso_iec._24727.tech.schema.ResponseType
import iso.std.iso_iec._24727.tech.schema.Sign
import iso.std.iso_iec._24727.tech.schema.SignResponse
import iso.std.iso_iec._24727.tech.schema.VerifyCertificate
import iso.std.iso_iec._24727.tech.schema.VerifyCertificateResponse
import iso.std.iso_iec._24727.tech.schema.VerifySignature
import iso.std.iso_iec._24727.tech.schema.VerifySignatureResponse
import org.openecard.common.ECardConstants
import org.openecard.common.WSHelper.makeResponse
import org.openecard.common.WSHelper.makeResultError
import java.util.EnumMap
import kotlin.jvm.java

/**
 * Basic implementation of a SAL protocol.
 * Some protocols may need to override this implementation in order to control Secure Messaging or
 * provide a customized protocol flow.
 *
 * @author Tobias Wich
 */
abstract class SALProtocolBaseImpl protected constructor() : SALProtocol {
	override val isFinished: Boolean
		get() = !hasNextStep()

	override val internalData: MutableMap<String, Any> = mutableMapOf()

	/** List of ProtocolSteps, which are per default executed in order.  */
	protected val steps: MutableList<ProtocolStep<*, *>?> = mutableListOf()
	protected val statelessSteps: MutableMap<FunctionType, ProtocolStep<*, *>?> =
		EnumMap<FunctionType, ProtocolStep<*, *>?>(FunctionType::class.java)

	/** Index marking current step in the step list.  */
	protected var curStep: Int = 0

	private fun hasNextStep(): Boolean = steps.size > curStep

	private fun hasNextProcessStep(functionName: FunctionType?): Boolean =
		if (hasNextStep()) {
			steps[curStep]!!.functionType == functionName
		} else {
			false
		}

	private fun hasStatelessStep(functionName: FunctionType?): Boolean = statelessSteps.containsKey(functionName)

	override fun hasNextStep(aFunction: FunctionType?): Boolean {
		if (hasStatelessStep(aFunction)) {
			return true
		}
		// check for a step in the process order
		return hasNextProcessStep(aFunction)
	}

	protected fun addOrderStep(step: ProtocolStep<*, *>): ProtocolStep<*, *> {
		steps.add(step)
		return step
	}

	/**
	 * Adds the given step to the stateless steps of this protocol.
	 *
	 * @param step The protocol step to add to the map.
	 * @return The previously associated step, or null if there was no previous association.
	 */
	protected fun addStatelessStep(step: ProtocolStep<*, *>): ProtocolStep<*, *>? =
		statelessSteps.put(step.functionType, step)

	/**
	 * Get next step and advance counter.
	 * @return next step or null if none exists.
	 */
	private fun <Req : RequestType> next(functionName: FunctionType): ProtocolStep<Req, *>? {
		// process order step takes precedence over stateless steps
		if (hasNextProcessStep(functionName)) {
			val step = steps[curStep]
			curStep++
			return step as ProtocolStep<Req, *>?
		} else {
			return statelessSteps[functionName] as ProtocolStep<Req, *>? // returns null if nothing found
		}
	}

	override fun cardApplicationStartSession(aParam: CardApplicationStartSession): CardApplicationStartSessionResponse? {
		val s = next<CardApplicationStartSession>(FunctionType.CardApplicationStartSession)
		val c = CardApplicationStartSessionResponse::class.java
		return perform(c, s, aParam, internalData) as CardApplicationStartSessionResponse?
	}

	override fun cardApplicationEndSession(param: CardApplicationEndSession): CardApplicationEndSessionResponse? {
		val s = next<CardApplicationEndSession>(FunctionType.CardApplicationEndSession)
		val c = CardApplicationEndSessionResponse::class.java
		return perform(c, s, param, internalData) as CardApplicationEndSessionResponse?
	}

	override fun encipher(param: Encipher): EncipherResponse? {
		val s = next<Encipher>(FunctionType.Encipher)
		return perform(EncipherResponse::class.java, s, param, internalData) as EncipherResponse?
	}

	override fun decipher(param: Decipher): DecipherResponse? {
		val s = next<Decipher>(FunctionType.Decipher)
		return perform(DecipherResponse::class.java, s, param, internalData) as DecipherResponse?
	}

	override fun getRandom(param: GetRandom): GetRandomResponse? {
		val s = next<GetRandom>(FunctionType.GetRandom)
		return perform(GetRandomResponse::class.java, s, param, internalData) as GetRandomResponse?
	}

	override fun hash(param: Hash): HashResponse? {
		val s = next<Hash>(FunctionType.Hash)
		return perform(HashResponse::class.java, s, param, internalData) as HashResponse?
	}

	override fun sign(aParam: Sign): SignResponse? {
		val s = next<Sign>(FunctionType.Sign)
		return perform(SignResponse::class.java, s, aParam, internalData) as SignResponse?
	}

	override fun verifySignature(param: VerifySignature): VerifySignatureResponse? {
		val s = next<VerifySignature>(FunctionType.VerifySignature)
		return perform(
			VerifySignatureResponse::class.java,
			s,
			param,
			internalData,
		) as VerifySignatureResponse?
	}

	override fun verifyCertificate(param: VerifyCertificate): VerifyCertificateResponse? {
		val s = next<VerifyCertificate>(FunctionType.VerifyCertificate)
		return perform(
			VerifyCertificateResponse::class.java,
			s,
			param,
			internalData,
		) as VerifyCertificateResponse?
	}

	override fun didCreate(aParam: DIDCreate): DIDCreateResponse? {
		val s = next<DIDCreate>(FunctionType.DIDCreate)
		return perform(DIDCreateResponse::class.java, s, aParam, internalData) as DIDCreateResponse?
	}

	override fun didUpdate(param: DIDUpdate): DIDUpdateResponse? {
		val s = next<DIDUpdate>(FunctionType.DIDUpdate)
		return perform(DIDUpdateResponse::class.java, s, param, internalData) as DIDUpdateResponse?
	}

	override fun didDelete(param: DIDDelete): DIDDeleteResponse? {
		val s = next<DIDDelete>(FunctionType.DIDDelete)
		return perform(DIDDeleteResponse::class.java, s, param, internalData) as DIDDeleteResponse?
	}

	override fun didAuthenticate(param: DIDAuthenticate): DIDAuthenticateResponse? {
		val s = next<DIDAuthenticate>(FunctionType.DIDAuthenticate)
		return perform(
			DIDAuthenticateResponse::class.java,
			s,
			param,
			internalData,
		) as DIDAuthenticateResponse?
	}

	/**
	 * Secure Messaging functions
	 * Overwrite in subclass when needed
	 */
	override fun needsSM(): Boolean = false

	override fun applySM(aCommandAPDU: ByteArray?): ByteArray? = aCommandAPDU

	override fun removeSM(responseAPDU: ByteArray?): ByteArray? = responseAPDU

    /*	companion object {
            private fun <Req : RequestType?> perform(
                responseClass: Class<out ResponseType?>,
                step: ProtocolStep<*, *>?,
                request: Req?,
                internalData: MutableMap<String?, Any?>?,
            ):*/

	private fun <Req : RequestType> perform(
		responseClass: Class<out ResponseType>,
		step: ProtocolStep<Req, *>?,
		request: Req,
		internalData: MutableMap<String, Any>,
	): ResponseType? {
		// return not implemented result first
		if (step == null) {
			val msg = "There is no applicable protocol step at this point in the protocol flow."
			val r = makeResultError(ECardConstants.Minor.SAL.INAPPROPRIATE_PROTOCOL_FOR_ACTION, msg)
			return makeResponse(responseClass, r)
		} else {
			return step.perform(request, internalData)
		}
	}
}
