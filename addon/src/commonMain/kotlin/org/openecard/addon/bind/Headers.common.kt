/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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
package org.openecard.addon.bind


/**
 * Simple immutable header entry.
 * It contains the name and the value of a header.
 *
 * @author Tobias Wich
 */
class HeaderEntry(val name: String, val value: String)


/**
 * Container with an ordered list of header entries.
 * In general all entries are ordered how they have been added to the container. However multi valued entries are
 * grouped at the first position one of the values got inserted.
 *
 * @author Tobias Wich
 */
class Headers {
	private val headers =
		LinkedHashMap<String, MutableList<HeaderEntry>>()

	/**
	 * Adds a header entry.
	 *
	 * @param name Name of the header.
	 * @param value Value of the header.
	 */
	fun addHeader(name: String, value: String) {
		addHeader(HeaderEntry(name, value))
	}

	/**
	 * Adds a header entry.
	 *
	 * @param h Header entry containing the name and value of the header.
	 */
	fun addHeader(h: HeaderEntry) {
		getHeaders(h.name).add(h)
	}

	/**
	 * Sets a header entry and therby removing any existing headers mathing the name.
	 *
	 * @param name Name of the header.
	 * @param value Value of the header.
	 */
	fun setHeader(name: String, value: String) {
		setHeader(HeaderEntry(name, value))
	}

	/**
	 * Sets a header entry and thereby removing any existing headers matching the name.
	 *
	 * @param h Header entry containing the name and value of the header.
	 */
	fun setHeader(h: HeaderEntry) {
		val hs = getHeaders(h.name)
		hs.clear()
		hs.add(h)
	}

	/**
	 * Removes all headers with the given name.
	 *
	 * @param name Name of the header.
	 */
	fun removeHeader(name: String) {
		getHeaders(name).clear()
	}

	/**
	 * Gets all entries for a given header name.
	 * If the header name is not present, an empty list will be returned.
	 *
	 * @param name Header name.
	 * @return Header entries matching the given header name, or an empty collection if no such header exists.
	 */
	fun getHeaders(name: String): MutableList<HeaderEntry> {
		synchronized(headers) {
			return headers[name] ?: run {
				val newSameHeaders = mutableListOf<HeaderEntry>()
				headers[name] = newSameHeaders
				newSameHeaders
			}
		}
	}

	/**
	 * Gets the first header entry matching the given header name.
	 *
	 * @param name Header name.
	 * @return The first entry, or `null` if no such header exists.
	 */
	fun getFirstHeader(name: String): HeaderEntry? {
		return getHeaders(name).firstOrNull()
	}

	/**
	 * Gets all header entry values for a given header name.
	 *
	 * @param name Header name.
	 * @return Header entry values matching the given header name, or an empty collection if no such header exists.
	 */
	fun getHeaderValues(name: String): List<String> {
		return getHeaders(name).map { it.value }
	}

	val headerNames: Collection<String>
		/**
		 * Gets all header names in this collection.
		 *
		 * @return Collection with all headers, or an empty collection if no headers are set.
		 */
		get() = headers.filterKeys { it.isNotEmpty() }.keys
}
