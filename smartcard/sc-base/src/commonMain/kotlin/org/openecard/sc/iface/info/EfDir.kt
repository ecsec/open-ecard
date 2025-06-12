package org.openecard.sc.iface.info

import org.openecard.sc.tlv.Tlv

class EfDir(
	private val dos: List<Tlv>,
) {
	val applications by lazy {
		dos.mapNotNull { ApplicationTemplate.fromDataObject(it) }
	}
}
