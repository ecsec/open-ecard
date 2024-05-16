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
 ***************************************************************************/

package org.openecard.addon.bind;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * Container with an ordered list of header entries.
 * In general all entries are ordered how they have been added to the container. However multi valued entries are
 * grouped at the first position one of the values got inserted.
 *
 * @author Tobias Wich
 */
public class Headers {

    private final LinkedHashMap<String, Deque<HeaderEntry>> headers;

    public Headers() {
	this.headers = new LinkedHashMap<>();
    }

    /**
     * Adds a header entry.
     *
     * @param name Name of the header.
     * @param value Value of the header.
     */
    public void addHeader(@Nonnull String name, @Nonnull String value) {
	addHeader(new HeaderEntry(name, value));
    }

    /**
     * Adds a header entry.
     *
     * @param h Header entry containing the name and value of the header.
     */
    public void addHeader(@Nonnull HeaderEntry h) {
	getHeaders(h.getName()).add(h);
    }

    /**
     * Sets a header entry and therby removing any existing headers mathing the name.
     *
     * @param name Name of the header.
     * @param value Value of the header.
     */
    public void setHeader(@Nonnull String name, @Nonnull String value) {
	setHeader(new HeaderEntry(name, value));
    }

    /**
     * Sets a header entry and therby removing any existing headers mathing the name.
     *
     * @param h Header entry containing the name and value of the header.
     */
    public void setHeader(@Nonnull HeaderEntry h) {
	Deque<HeaderEntry> hs = getHeaders(h.getName());
	hs.clear();
	hs.add(h);
    }

    /**
     * Removes all headers with the given name.
     *
     * @param name Name of the header.
     */
    public void removeHeader(@Nonnull String name) {
	getHeaders(name).clear();
    }

    /**
     * Gets all entries for a given header name.
     * If the header name is not present, an empty list will be returned.
     *
     * @param name Header name.
     * @return Header entries matching the given header name, or an empty collection if no such header exists.
     */
    @Nonnull
    public Deque<HeaderEntry> getHeaders(@Nonnull String name) {
	Deque<HeaderEntry> sameHeaders;
	synchronized (headers) {
	    sameHeaders = headers.get(name);
	    if (sameHeaders == null) {
		sameHeaders = new ConcurrentLinkedDeque<>();
		headers.put(name, sameHeaders);
	    }
	}
	return sameHeaders;
    }

    /**
     * Gets the first header entry matching the given header name.
     *
     * @param name Header name.
     * @return The first entry, or {@code null} if no such header exists.
     */
    @Nullable
    public HeaderEntry getFirstHeader(@Nonnull String name) {
	return getHeaders(name).peekFirst();
    }

    /**
     * Gets all header entry values for a given header name.
     *
     * @param name Header name.
     * @return Header entry values matching the given header name, or an empty collection if no such header exists.
     */
    @Nonnull
    public Deque<String> getHeaderValues(@Nonnull String name) {
	LinkedList<String> result = new LinkedList<>();
	for (HeaderEntry next : getHeaders(name)) {
	    result.add(next.getValue());
	}
	return result;
    }

    /**
     * Gets all header names in this collection.
     *
     * @return Collection with all headers, or an empty collection if no headers are set.
     */
    @Nonnull
    public Collection<String> getHeaderNames() {
	ArrayList<String> result = new ArrayList<>();
	// walk over entries to filter out empty headers
	for (Map.Entry<String,Deque<HeaderEntry>> next : headers.entrySet()) {
	    if (! next.getValue().isEmpty()) {
		result.add(next.getKey());
	    }
	}
	return result;
    }

}
