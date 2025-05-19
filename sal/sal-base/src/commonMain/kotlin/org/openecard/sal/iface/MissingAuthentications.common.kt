package org.openecard.sal.iface

import org.openecard.sal.iface.dids.AuthenticationDid

class MissingAuthentications(
	val decisions: BoolTreeOr<AuthenticationDid>,
)
