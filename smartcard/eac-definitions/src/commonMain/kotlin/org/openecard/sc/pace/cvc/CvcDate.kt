package org.openecard.sc.pace.cvc

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.openecard.sc.tlv.Tag
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.TlvPrimitive
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class CvcDate(
	/**
	 * Number of the year of this date.
	 */
	val year: Int,
	/**
	 * Number of the month of this date in the range 1 to 12.
	 */
	val month: Int,
	/**
	 * Number of the day of this date in the range 1 to 31.
	 */
	val day: Int,
) : Comparable<CvcDate> {
	override fun compareTo(other: CvcDate): Int {
		val y = this.year.compareTo(other.year)
		if (y != 0) return y
		val m = this.month.compareTo(other.month)
		if (m != 0) return m
		return this.day.compareTo(other.day)
	}

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		@Throws(IllegalArgumentException::class)
		fun Tlv.toCvcDate(tag: Tag): CvcDate {
			require(this.tag == tag) { "The tag of the TLV ($tag) is not the expected tag." }
			return when (this) {
				is TlvPrimitive -> {
					val date = this.value
					val year = 2000u + (date[0] * 10u) + date[1]
					val month = (date[2] * 10u) + date[3]
					val day = (date[4] * 10u) + date[5]
					CvcDate(year.toInt(), month.toInt(), day.toInt())
				}

				else -> {
					throw IllegalArgumentException("CVC Date TLV is not primitive")
				}
			}
		}

		@OptIn(ExperimentalTime::class)
		fun Instant.toCvcDate(): CvcDate {
			val now = this.toLocalDateTime(TimeZone.UTC)
			return CvcDate(now.year, now.month.number, now.day)
		}

		@OptIn(ExperimentalTime::class)
		fun Instant.isBetween(
			start: CvcDate,
			endExclusive: CvcDate,
		): Boolean {
			val now = this.toCvcDate()
			return (start <= now) && (now <= endExclusive)
		}

		fun CvcDate.toLocalDate(): LocalDate = LocalDate(year, month, day)
	}
}
