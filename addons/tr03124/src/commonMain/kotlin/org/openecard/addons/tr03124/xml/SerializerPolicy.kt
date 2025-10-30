package org.openecard.addons.tr03124.xml

import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.serialization.XML

val eacXml =
	XML {
		defaultPolicy {
			indentString = "  "
			ignoreUnknownChildren()
		}
	}

val tcTokenXml =
	XML {
		xmlDeclMode = XmlDeclMode.None
		defaultPolicy {
			verifyElementOrder = false
			indentString = "  "
			ignoreUnknownChildren()
		}
	}
