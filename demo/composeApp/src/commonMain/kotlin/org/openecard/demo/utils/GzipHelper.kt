package org.openecard.demo.utils

import okio.Buffer
import okio.GzipSource

@OptIn(ExperimentalUnsignedTypes::class)
internal fun gunzip(data: UByteArray) =
    Buffer()
        .also {
            it.writeAll(
                GzipSource(
                    Buffer().also { b -> b.write(data.toByteArray()) },
                ),
            )
        }.readByteArray()
