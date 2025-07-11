package org.openecard.sal.sc.acl

import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.missingAuthentications
import org.openecard.sal.sc.SmartcardDeviceConnection

fun CifAclOr.missingAuthentications(dev: SmartcardDeviceConnection): MissingAuthentications =
	this.missingAuthentications(
		dev.allAuthDids,
		dev.cardState.authenticatedDids
			.map { it.toStateReference() }
			.toSet(),
	)
