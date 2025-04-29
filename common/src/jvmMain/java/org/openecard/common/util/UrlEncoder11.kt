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
 */
package org.openecard.common.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*
import javax.annotation.Nonnull

/**
 * URL encoder class supporting standard URL encoding and URL encoding of query parameters.
 *
 * @author Tobias Wich
 */
class UrlEncoder
/**
 * Creates an instance for the UTF-8 character set.
 *
 * @see .UrlEncoder
 */ @JvmOverloads constructor(private val charset: Charset = Charset.forName("UTF-8")) {
    /**
     * Creates an instance for the given character set.
     *
     * @param charset The character set used to encode and decode strings.
     */

    /**
     * URL encodes the given string, so that it is usable in the query part of a URL.
     * This method is based on RFC 3986.
     *
     * @param content Data which will be encoded. `null` is permitted.
     * @return Encoded data or `null` if `null` has been supplied as parameter.
     */
    fun encodeQueryParam(content: String?): String? {
        if (content == null) {
            return null
        }
        return urlEncode(content, QUERY_SAFE, false, true)
    }

    /**
     * URL encodes the given string, so it is usable in an encoded form (`application/x-www-form-urlencoded`).
     *
     * @see URLEncoder
     *
     * @param content Data which will be encoded. `null` is permitted.
     * @return Encoded data or `null` if `null` has been supplied as parameter.
     */
    fun urlEncodeUrl(content: String?): String? {
        if (content == null) {
            return null
        }
        try {
            return URLEncoder.encode(content, charset.name())
        } catch (ex: UnsupportedEncodingException) {
            // can not happen as the charset is already verified in the constructor
            throw IllegalStateException("Charset became invalid out of a sudden.")
        }
    }

    /**
     * URL encodes the given string, so that it is usable in the fragment part of a URL.
     * This method is based on RFC 3986.
     *
     * @param content Data which will be encoded. `null` is permitted.
     * @return Encoded data or `null` if `null` has been supplied as parameter.
     */
    fun encodeFragment(content: String?): String? {
        if (content == null) {
            return null
        }
        return urlEncode(content, FRAGMENT_SAFE, false, true)
    }

    private fun urlEncode(@Nonnull content: String, safeChars: BitSet, wsPlus: Boolean, keepEscapes: Boolean): String {
        val contentBytes = content.toByteArray(charset)
        val `is` = ByteArrayInputStream(contentBytes)
        val os = ByteArrayOutputStream()

        var nextVal: Int
        while ((`is`.read().also { nextVal = it }) != -1) {
            val b = (nextVal and 0xFF).toByte()

            if (wsPlus && b == ' '.code.toByte()) {
                // space as special element
                os.write('+'.code)
            } else if (keepEscapes && b == '%'.code.toByte()) {
                // already encoded numbers
                `is`.mark(2)
                val c1 = `is`.read()
                val c2 = `is`.read()
                if (HEX[c1] && HEX[c2]) {
                    os.write('%'.code)
                    os.write(c1)
                    os.write(c2)
                } else {
                    // rewind stream and encode the last character
                    `is`.reset()
                    encodeChar(os, b)
                }
            } else if (safeChars[b.toInt()]) {
                // characters which should not be encoded
                os.write(b.toInt())
            } else {
                // characters which should be encoded
                encodeChar(os, b)
            }
        }

        return String(os.toByteArray(), charset)
    }

    companion object {
        private val ASCII: Charset = Charset.forName("ASCII")
        private const val RADIX = 16

        // character classes according to RFC 2396
        private val ALPHA = BitSet(256)
        private val DIGIT = BitSet(256)
        private val HEX = BitSet(256)
        private val UNRESERVED = BitSet(256)
        private val RESERVED = BitSet(256)
        private val SUB_DELIM = BitSet(256)
        private val GEN_DELIM = BitSet(256)
        private val PCHAR = BitSet(256)
        private val QUERY = BitSet(256)
        private val FRAGMENT = BitSet(256)

        private val QUERY_SAFE = BitSet(256)
        private val FRAGMENT_SAFE = BitSet(256)

        init {
            ALPHA['a'.code] = 'z'.code + 1
            ALPHA['A'.code] = 'Z'.code + 1
            DIGIT['0'.code] = '9'.code + 1
            HEX.or(DIGIT)
            HEX['a'.code] = 'f'.code
            HEX['A'.code] = 'F'.code

            setBits(SUB_DELIM, "!$&'()*+,;=")

            setBits(GEN_DELIM, ":/?#[]@")

            RESERVED.or(SUB_DELIM)
            RESERVED.or(GEN_DELIM)

            UNRESERVED.or(ALPHA)
            UNRESERVED.or(DIGIT)
            setBits(UNRESERVED, "-._~")

            PCHAR.or(UNRESERVED)
            PCHAR.or(SUB_DELIM)
            setBits(PCHAR, ":@")

            // and percent encoded, but this is handled in the encode function
            QUERY.or(PCHAR)
            setBits(QUERY, "/?")

            FRAGMENT.or(PCHAR)
            setBits(FRAGMENT, "/?")

            // calculate safe character sets
            QUERY_SAFE.or(QUERY)
            QUERY_SAFE.andNot(RESERVED)

            FRAGMENT_SAFE.or(FRAGMENT)
            FRAGMENT_SAFE.andNot(RESERVED)
        }


        private fun setBits(bs: BitSet, chars: String) {
            setBitsVal(bs, chars, true)
        }

        private fun unsetBits(bs: BitSet, chars: String) {
            setBitsVal(bs, chars, false)
        }

        private fun setBitsVal(bs: BitSet, chars: String, value: Boolean) {
            for (next in chars.toByteArray(ASCII)) {
                bs[next.toInt()] = value
            }
        }


        private fun encodeChar(os: ByteArrayOutputStream, b: Byte) {
            os.write('%'.code)
            val hex1 = Character.forDigit((b.toInt() shr 4) and 0xF, RADIX).uppercaseChar()
            val hex2 = Character.forDigit(b.toInt() and 0xF, RADIX).uppercaseChar()
            os.write(hex1.code)
            os.write(hex2.code)
        }
    }
}
