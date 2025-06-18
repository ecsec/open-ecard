package org.openecard.cif.dsl.api.acl

@Target(
	allowedTargets = [
		AnnotationTarget.TYPE,
		AnnotationTarget.CLASS,
		AnnotationTarget.FUNCTION,
		AnnotationTarget.VALUE_PARAMETER,
	],
)
@DslMarker
annotation class AslTreeMarker
