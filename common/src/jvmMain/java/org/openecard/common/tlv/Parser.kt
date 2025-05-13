/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 */
package org.openecard.common.tlv

/**
 * Parser class to help evaluate the internas of TLV structures.
 * This implementation works on the children of a given TLV object. If the parser is needed to evaluate deep structures,
 * then new instances of it have to be created for the respective substructures.
 *
 * @author Tobias Wich
 */
class Parser(
	private val tlv: TLV?,
) {
	private var next: TLV? = null

	init {
		reset()
	}

	/**
	 * Reset the parser to the original object given in the constructor.
	 */
	fun reset() {
		this.next = this.tlv
	}

	/**
	 * Matches the tag of the next element against any of the given tags.
	 *
	 * @param tags List of `Tag` objects which yield a positive result when found.
	 * @return `true` when the next object has one of the given tags, `false` otherwise.
	 */
	fun match(vararg tags: Tag): Boolean = match(next, convertTags(*tags))

	/**
	 * Matches the tag of the next element against any of the given tags.
	 *
	 * @param tagsWithClass List of tags which yield a positive result when found.
	 * @return `true` when the next object has one of the given tags, `false` otherwise.
	 */
	fun match(vararg tagsWithClass: Long): Boolean = match(next, tagsWithClass)

	/**
	 * Matches the tag of the ith element against any of the given tags.
	 *
	 * @param i Number stating how far to look ahead. 0 means to look at the current element.
	 * @param tagsWithClass List of tags which yield a positive result when found.
	 * @return `true` when the ith object has one of the given tags, `false` otherwise.
	 */
	fun matchLA(
		i: Int,
		vararg tagsWithClass: Long,
	): Boolean {
		val lookahead = lookAhead(i)
		return match(lookahead, tagsWithClass)
	}

	/**
	 * Matches the tag of the ith element against any of the given tags.
	 *
	 * @param i Number stating how far to look ahead. 0 means to look at the current element.
	 * @param tags List of `Tag` objects which yield a positive result when found.
	 * @return `true` when the ith object has one of the given tags, `false` otherwise.
	 */
	fun matchLA(
		i: Int,
		vararg tags: Tag,
	): Boolean {
		val lookahead = lookAhead(i)
		return match(lookahead, convertTags(*tags))
	}

	private fun match(
		tlv: TLV?,
		tagsWithClass: LongArray,
	): Boolean {
		if (tlv != null) {
			val tagNumWithClass = tlv.tagNumWithClass
			for (nextTag in tagsWithClass) {
				if (tagNumWithClass == nextTag) {
					return true
				}
			}
		}
		return false
	}

	private fun convertTags(vararg tags: Tag): LongArray {
		val tagList = LongArray(tags.size)
		for (i in tags.indices) {
			tagList[i] = tags[i].tagNumWithClass
		}
		return tagList
	}

	/**
	 * Get TLV for index i and advance parser to the next sibling of the ith element.
	 *
	 * @param i 0 for the current element, a positive number for any element further in the structure.
	 * @return The next element for the given index, or `null` if the index is greater than the number of
	 * available elements.
	 */
	fun next(i: Int): TLV? {
		var nextTLV = next
		
		if (nextTLV == null || i < 0) {
			return null
		}

		var count = i
		while (count != 0) {
			if (nextTLV!!.hasNext()) {
				nextTLV = nextTLV.next
				count--
			} else {
				return null
			}
		}

		// set new next element
		next = nextTLV!!.next
		// make copy and remove forward link
		nextTLV = TLV(nextTLV)
		nextTLV.next = null
		return nextTLV
	}

	private fun lookAhead(i: Int): TLV? {
		if (next == null || i < 0) {
			return null
		}

		var count = i
		var nextTLV = next
		while (count != 0) {
			if (nextTLV!!.hasNext()) {
				nextTLV = nextTLV.next
				count--
			} else {
				return null
			}
		}

		// make copy and remove forward link
		nextTLV = TLV(nextTLV!!)
		nextTLV.next = null
		return nextTLV
	}
}
