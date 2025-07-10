package org.openecard.sal.iface

import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.sal.iface.dids.AuthenticationDid

class AclDidResolution(
	val authDid: AuthenticationDid,
	val requiredState: DidStateReference,
)
