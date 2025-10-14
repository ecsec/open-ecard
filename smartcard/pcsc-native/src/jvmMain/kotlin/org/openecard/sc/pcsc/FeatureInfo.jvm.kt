package org.openecard.sc.pcsc

import com.sun.jna.Platform

internal actual val isWindows: Boolean = Platform.isWindows()
