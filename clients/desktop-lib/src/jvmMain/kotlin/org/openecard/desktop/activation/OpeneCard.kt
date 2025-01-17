package org.openecard.desktop.activation

import org.openecard.common.ifd.scio.SCIOException
import org.openecard.ifd.scio.wrapper.IFDTerminalFactory
import org.openecard.mobile.activation.ActivationSource
import org.openecard.mobile.activation.CardLinkControllerFactory
import org.openecard.mobile.activation.ContextManager
import org.openecard.mobile.activation.EacControllerFactory
import org.openecard.mobile.activation.NFCCapabilities
import org.openecard.mobile.activation.NfcCapabilityResult
import org.openecard.mobile.activation.PinManagementControllerFactory
import org.openecard.mobile.activation.common.CommonActivationUtils
import org.openecard.mobile.activation.common.NFCDialogMsgSetter
import org.openecard.mobile.system.OpeneCardContextConfig
import org.openecard.ws.jaxb.JAXBMarshaller

/**
 *
 * @author Neil Crossley
 */
class OpeneCard internal constructor(
	private val contextManager: ContextManager,
	private val activationUtils: CommonActivationUtils
): ActivationSource {

	fun context(): ContextManager {
		return contextManager
	}

	override fun eacFactory(): EacControllerFactory {
		return activationUtils.eacFactory()
	}

	override fun cardLinkFactory(): CardLinkControllerFactory {
		return activationUtils.cardLinkFactory()
	}

	override fun pinManagementFactory(): PinManagementControllerFactory {
		return activationUtils.pinManagementFactory()
	}

	companion object {

		@JvmStatic
		fun createInstance(): OpeneCard {
			val ifdTerminalfactory = IFDTerminalFactory.configBackedInstance()
			val terminalFactory = ifdTerminalfactory.instance

			val config =
                OpeneCardContextConfig(ifdTerminalfactory, JAXBMarshaller::class.java.getCanonicalName())
			val activationUtils = CommonActivationUtils(config, object : NFCDialogMsgSetter {
                override fun setText(msg: String) {}

                override fun isSupported(): Boolean {
                    return false
                }
            })
			val contextManager = activationUtils.context(object: NFCCapabilities {
				override fun isAvailable(): Boolean {
					return try {
						terminalFactory.terminals().list().isNotEmpty()
					} catch (ex: SCIOException) {
						false
					}
				}

				override fun isEnabled(): Boolean {
					return true
				}

				override fun checkExtendedLength(): NfcCapabilityResult {
					return NfcCapabilityResult.QUERY_NOT_ALLOWED
				}

			})
			return OpeneCard(contextManager, activationUtils)
		}
	}

}
