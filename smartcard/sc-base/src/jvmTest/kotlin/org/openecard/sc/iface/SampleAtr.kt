package org.openecard.sc.iface

import org.openecard.utils.serialization.PrintableUByteArray
import org.openecard.utils.serialization.toPrintable
import java.util.Scanner
import java.util.stream.Stream

data class SampleAtr(
	val atr: PrintableUByteArray,
	val description: String,
) {
	companion object {
		@OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
		fun loadSamples(): List<SampleAtr> {
			val s = Scanner(SampleAtr::class.java.getResourceAsStream("/smartcard_list_20250521.txt")!!)

			val samples =
				s.findAll("""^(\p{XDigit}{2} .*?)\n((\t.+?\n)+)\n""".toRegex(RegexOption.MULTILINE).toPattern()).flatMap {
					val atr = it.group(1)
					val desc = it.group(2)

					try {
						val atrBytes = atr.replace(" ", "").hexToUByteArray()
						val cleanedDesc = desc.trim().replace("\t", "")
						Stream.of(SampleAtr(atrBytes.toPrintable(), cleanedDesc))
					} catch (ex: Exception) {
						Stream.of()
					}
				}
			return samples.toList()
		}
	}
}
