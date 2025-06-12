package org.openecard.sc.iface.info

import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.TlvConstructed

/**
 * Application template (8.2.1.3).
 *
 * Referenced by tag '61', this interindustry template may be present in EF.ATR (see 8.2.1.1), in EF.DIR (see 8.2.1.1)
 * and in the management data of any DF (see 5.3.3).
 */
class ApplicationTemplate
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val aid: ApplicationIdentifier,
		val label: String?,
		val fileReference: List<FileReference>,
		val commandApdu: UByteArray?,
		val discretionaryData: List<Tlv>,
		val url: String?,
		val applicationDos: List<ApplicationTemplate>,
	) {
		companion object {
			@OptIn(ExperimentalUnsignedTypes::class)
			fun fromDataObject(tlv: Tlv): ApplicationTemplate? {
				val atDo = matchAtDo(tlv)
				if (atDo != null) {
					val children = atDo.childList()
					val aid = ApplicationIdentifier.fromDataObjects(children).first()
					val label =
						atDo
							.findChildTags(StandardTags.applicationLabel)
							.firstOrNull()
							?.contentAsBytesBer
							?.toByteArray()
							?.decodeToString()
					val fileRef = children.mapNotNull { FileReference.fromDataObject(it) }
					val command = atDo.findChildTags(StandardTags.commandApdu).firstOrNull()?.contentAsBytesBer
					// discretionary data
					val discretionary =
						children.filter {
							it.tag in
								setOf(
									StandardTags.discretionaryDataObjectInterIndustry,
									StandardTags.discretionaryDataObjectPropretiery,
								)
						}
					val url =
						atDo
							.findChildTags(StandardTags.url)
							.firstOrNull()
							?.contentAsBytesBer
							?.toByteArray()
							?.decodeToString()
					// nested Application DOs
					val appDos =
						atDo
							.findChildTags(
								StandardTags.applicationTemplate,
							).mapNotNull { ApplicationTemplate.fromDataObject(it) }

					return ApplicationTemplate(aid, label, fileRef, command, discretionary, url, appDos)
				} else {
					return null
				}
			}

			private fun matchAtDo(tlv: Tlv): TlvConstructed? =
				if (tlv.tag == StandardTags.applicationTemplate) {
					// return do when there is an AID present
					tlv.asConstructed?.takeIf { it.findChildTags(StandardTags.applicationIdentifier).isNotEmpty() }
				} else {
					null
				}
		}
	}
