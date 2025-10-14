package org.openecard.sc.pcsc

import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
internal actual val isWindows: Boolean = Platform.osFamily == OsFamily.WINDOWS
