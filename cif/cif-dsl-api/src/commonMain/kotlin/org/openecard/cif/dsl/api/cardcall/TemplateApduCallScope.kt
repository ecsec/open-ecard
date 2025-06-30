package org.openecard.cif.dsl.api.cardcall

interface TemplateApduCallScope {
	var header: String
	var data: String?
	var expectedLength: ULong?
}
