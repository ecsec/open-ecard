package org.openecard.utils.common

import kotlin.random.Random

var sessionRandomBytes: Int = 16

@OptIn(ExperimentalStdlibApi::class)
fun Random.generateSessionId(): String = this.nextBytes(sessionRandomBytes).toHexString()
