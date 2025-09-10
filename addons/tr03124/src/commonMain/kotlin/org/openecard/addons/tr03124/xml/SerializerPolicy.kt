package org.openecard.addons.tr03124.xml

import nl.adaptivity.xmlutil.serialization.XML

val eacXml =
	XML {
		defaultPolicy {
			indentString = "  "
			ignoreUnknownChildren()
		}
	}
