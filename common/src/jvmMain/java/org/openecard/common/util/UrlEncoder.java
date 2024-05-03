/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.BitSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * URL encoder class supporting standard URL encoding and URL encoding of query parameters.
 *
 * @author Tobias Wich
 */
public class UrlEncoder {

    private static final Charset ASCII = Charset.forName("ASCII");
    private static final int RADIX = 16;

    // character classes according to RFC 2396
    private static final BitSet ALPHA = new BitSet(256);
    private static final BitSet DIGIT = new BitSet(256);
    private static final BitSet HEX = new BitSet(256);
    private static final BitSet UNRESERVED = new BitSet(256);
    private static final BitSet RESERVED = new BitSet(256);
    private static final BitSet SUB_DELIM = new BitSet(256);
    private static final BitSet GEN_DELIM = new BitSet(256);
    private static final BitSet PCHAR = new BitSet(256);
    private static final BitSet QUERY = new BitSet(256);
    private static final BitSet FRAGMENT = new BitSet(256);

    private static final BitSet QUERY_SAFE = new BitSet(256);
    private static final BitSet FRAGMENT_SAFE = new BitSet(256);

    private final Charset charset;

    static {
	ALPHA.set('a', 'z' + 1);
	ALPHA.set('A', 'Z' + 1);
	DIGIT.set('0', '9' + 1);
	HEX.or(DIGIT);
	HEX.set('a', 'f');
	HEX.set('A', 'F');

	setBits(SUB_DELIM, "!$&'()*+,;=");

	setBits(GEN_DELIM, ":/?#[]@");

	RESERVED.or(SUB_DELIM);
	RESERVED.or(GEN_DELIM);

	UNRESERVED.or(ALPHA);
	UNRESERVED.or(DIGIT);
	setBits(UNRESERVED, "-._~");

	PCHAR.or(UNRESERVED);
	PCHAR.or(SUB_DELIM);
	setBits(PCHAR, ":@");
	// and percent encoded, but this is handled in the encode function

	QUERY.or(PCHAR);
	setBits(QUERY, "/?");

	FRAGMENT.or(PCHAR);
	setBits(FRAGMENT, "/?");

	// calculate safe character sets
	QUERY_SAFE.or(QUERY);
	QUERY_SAFE.andNot(RESERVED);

	FRAGMENT_SAFE.or(FRAGMENT);
	FRAGMENT_SAFE.andNot(RESERVED);
    }


    /**
     * Creates an instance for the UTF-8 character set.
     *
     * @see #UrlEncoder(java.nio.charset.Charset)
     */
    public UrlEncoder() {
	this(Charset.forName("UTF-8"));
    }

    /**
     * Creates an instance for the given character set.
     *
     * @param charset The character set used to encode and decode strings.
     */
    public UrlEncoder(Charset charset) {
	this.charset = charset;
    }


    private static void setBits(BitSet bs, String chars) {
	setBitsVal(bs, chars, true);
    }

    private static void unsetBits(BitSet bs, String chars) {
	setBitsVal(bs, chars, false);
    }

    private static void setBitsVal(BitSet bs, String chars, boolean value) {
	for (byte next : chars.getBytes(ASCII)) {
	    bs.set(next, value);
	}
    }


    /**
     * URL encodes the given string, so that it is usable in the query part of a URL.
     * This method is based on RFC 3986.
     *
     * @param content Data which will be encoded. {@code null} is permitted.
     * @return Encoded data or {@code null} if {@code null} has been supplied as parameter.
     */
    public String encodeQueryParam(@Nullable String content) {
        if (content == null) {
            return null;
        }
        return urlEncode(content, QUERY_SAFE, false, true);
    }

    /**
     * URL encodes the given string, so it is usable in an encoded form ({@code application/x-www-form-urlencoded}).
     *
     * @see URLEncoder
     * @param content Data which will be encoded. {@code null} is permitted.
     * @return Encoded data or {@code null} if {@code null} has been supplied as parameter.
     */
    public String urlEncodeUrl(@Nullable String content) {
        if (content == null) {
            return null;
        }
	try {
	    return URLEncoder.encode(content, charset.name());
	} catch (UnsupportedEncodingException ex) {
	    // can not happen as the charset is already verified in the constructor
	    throw new IllegalStateException("Charset became invalid out of a sudden.");
	}
    }

    /**
     * URL encodes the given string, so that it is usable in the fragment part of a URL.
     * This method is based on RFC 3986.
     *
     * @param content Data which will be encoded. {@code null} is permitted.
     * @return Encoded data or {@code null} if {@code null} has been supplied as parameter.
     */
    public String encodeFragment(@Nullable String content) {
        if (content == null) {
            return null;
        }
        return urlEncode(content, FRAGMENT_SAFE, false, true);
    }

    private String urlEncode(@Nonnull String content, BitSet safeChars, boolean wsPlus, boolean keepEscapes) {
	byte[] contentBytes = content.getBytes(charset);
	ByteArrayInputStream is = new ByteArrayInputStream(contentBytes);
	ByteArrayOutputStream os = new ByteArrayOutputStream();

	int nextVal;
        while ((nextVal = is.read()) != -1) {
            byte b = (byte) (nextVal & 0xFF);

	    if (wsPlus && b == ' ') {
		// space as special element
		os.write('+');
	    } else if (keepEscapes && b == '%') {
		// already encoded numbers
		is.mark(2);
		int c1 = is.read();
		int c2 = is.read();
		if (HEX.get(c1) && HEX.get(c2)) {
		    os.write('%');
		    os.write(c1);
		    os.write(c2);
		} else {
		    // rewind stream and encode the last character
		    is.reset();
		    encodeChar(os, b);
		}
	    } else if (safeChars.get(b)) {
		// characters which should not be encoded
                os.write(b);
            } else {
		// characters which should be encoded
		encodeChar(os, b);
            }
        }

        return new String(os.toByteArray(), charset);
    }

    private static void encodeChar(ByteArrayOutputStream os, byte b) {
	os.write('%');
	char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, RADIX));
	char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, RADIX));
	os.write(hex1);
	os.write(hex2);
    }

}
