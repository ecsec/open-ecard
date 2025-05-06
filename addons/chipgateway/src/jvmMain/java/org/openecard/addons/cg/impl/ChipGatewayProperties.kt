/****************************************************************************
 * Copyright (C) 2016-2025 ecsec GmbH.
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
package org.openecard.addons.cg.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.Properties

/**
 *
 * @author Tobias Wich
 */
class ChipGatewayProperties {
	private val props: Properties

	private constructor() {
		this.props = Properties()
	}

	private constructor(propsResource: String) {
		val `is` = javaClass.getResourceAsStream(propsResource)
		this.props = Properties()
		this.props.load(`is`)
	}

	companion object {
		private val LOG: Logger = LoggerFactory.getLogger(ChipGatewayProperties::class.java)
		private var inst: ChipGatewayProperties? = null

		@get:Synchronized
		private val instance: ChipGatewayProperties
			get() {
				if (inst == null) {
					try {
						inst =
							ChipGatewayProperties("/chipgateway/cg_policy.properties")
					} catch (ex: IOException) {
						LOG.error(
							"The bundled properties file could not be loaded.",
							ex,
						)
						inst = ChipGatewayProperties()
					}
				}
				return inst!!
			}

		val isRemotePinAllowed: Boolean
			get() {
				val pinAllowedStr: String? =
					instance.props.getProperty("remote-pin-allowed", "false")
				return pinAllowedStr.toBoolean()
			}

		val isValidateServerCert: Boolean
			get() {
				val validateStr: String? =
					instance.props.getProperty("validate-server-cert", "true")
				return validateStr.toBoolean()
			}

		val isValidateChallengeResponse: Boolean
			get() {
				val validateStr: String? =
					instance.props.getProperty("validate-challenge-response", "true")
				return validateStr.toBoolean()
			}

		val isRevocationCheck: Boolean
			get() {
				val revocationStr: String? =
					instance.props.getProperty("revocation-check", "true")
				return revocationStr.toBoolean()
			}

		val isUseSubjectWhitelist: Boolean
			get() {
				val whitelistStr: String? =
					instance.props.getProperty("use-subject-whitelist", "true")
				return whitelistStr.toBoolean()
			}

		val isUseApiEndpointWhitelist: Boolean
			get() {
				val whitelistStr: String? =
					instance.props.getProperty("use-api-endpoint-whitelist", "true")
				return whitelistStr.toBoolean()
			}

		val isDeveloperTrustStore: Boolean
			get() {
				val devTrustStoreStr: String? =
					instance.props.getProperty("developer-truststore", "false")
				return devTrustStoreStr.toBoolean()
			}

		val isUseUpdateDomainWhitelist: Boolean
			get() {
				val devTrustStoreStr: String? =
					instance.props.getProperty("use-update-domain-whitelist", "true")
				return devTrustStoreStr.toBoolean()
			}

		val isHideUpdateDialog: Boolean
			get() {
				val hideUpdateDialog: String? =
					instance.props.getProperty("hide-update-dialog", "false")
				return hideUpdateDialog.toBoolean()
			}
	}
}
