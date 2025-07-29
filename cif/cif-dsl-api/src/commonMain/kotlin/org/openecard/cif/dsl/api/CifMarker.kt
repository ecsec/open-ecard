package org.openecard.cif.dsl.api

@Target(allowedTargets = [AnnotationTarget.TYPE, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION])
@DslMarker
annotation class CifMarker

@CifMarker
interface CifScope
