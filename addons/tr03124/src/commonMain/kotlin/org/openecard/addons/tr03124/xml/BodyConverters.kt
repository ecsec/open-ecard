package org.openecard.addons.tr03124.xml

fun StartPaos.toBody() = Body(startPaos = this)

fun DidAuthenticateResponse.toBody() = Body(didAuthenticateResponse = this)

fun TransmitResponse.toBody() = Body(transmitResponse = this)
