/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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
package org.openecard.binding.tctoken

import generated.TCTokenType
import org.openecard.common.util.StringUtils
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.util.*

/**
 * Implements a SAX handler to parse TCTokens.
 *
 * @author Moritz Horsch
 */
class TCTokenSAXHandler : DefaultHandler() {
    private var read = false
    private var sb: StringBuilder? = null
    private var tokens: MutableList<TCToken?>? = null
    private var token: TCToken? = null

    @Throws(SAXException::class)
    override fun startDocument() {
        tokens = ArrayList<TCToken?>()
        sb = StringBuilder(2048)
    }

    @Throws(SAXException::class)
    override fun endDocument() {
        token = null
        read = false
        sb!!.delete(0, sb!!.length)
    }

    @Throws(SAXException::class)
    override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes?) {
        // Consider only the TCTokens.
        if (qName.equals(TC_TOKEN_TYPE, ignoreCase = true)) {
            read = true
            token = TCToken()
        } else if (qName.equals(PATH_SECURITY_PARAMETERS, ignoreCase = true)) {
            token!!.setPathSecurityParameters(TCTokenType.PathSecurityParameters())
        }
    }

    @Throws(SAXException::class)
    override fun endElement(uri: String?, localName: String?, qName: String) {
        var value = sb.toString()
        sb!!.delete(0, sb!!.length)

        if (qName.equals(TC_TOKEN_TYPE, ignoreCase = true)) {
            tokens!!.add(token)
            token = TCToken()
            read = false
        } else if (qName.equals(SESSION_IDENTIFIER, ignoreCase = true)) {
            token!!.setSessionIdentifier(value)
        } else if (qName.equals(SERVER_ADDRESS, ignoreCase = true)) {
            if (!value.isEmpty()) {
                // correct malformed URL
                if (!value.startsWith("https://") && !value.startsWith("http://")) {
                    // protocol relative or completely missing scheme
                    if (value.startsWith("//")) {
                        value = "https:" + value
                    } else {
                        value = "https://" + value
                    }
                }
            }

            token!!.setServerAddress(value)
        } else if (qName.equals(REFRESH_ADDRESS, ignoreCase = true)) {
            token!!.setRefreshAddress(value)
        } else if (qName.equals(ERROR_ADDRESS, ignoreCase = true)) {
            token!!.setCommunicationErrorAddress(value)
        } else if (qName.equals(PATH_SECURITY_PROTOCOL, ignoreCase = true)) {
            token!!.setPathSecurityProtocol(value)
        } else if (qName.equals(BINDING, ignoreCase = true)) {
            token!!.setBinding(value)
        } else if (qName.equals(PSK, ignoreCase = true)) {
            try {
                // check that an even number of characters (2 per byte) is present
                if ((value.length % 2) == 0) {
                    val b = StringUtils.toByteArray(value.uppercase(Locale.getDefault()))
                    token!!.getPathSecurityParameters().setPSK(b)
                } else {
                    token!!.setInvalidPSK(true)
                }
            } catch (ex: NumberFormatException) {
                token!!.setInvalidPSK(true)
            }
        } else if (qName.equals(PATH_SECURITY_PARAMETERS, ignoreCase = true)) {
            // check if a PSK value is actually present, if not delete PSP element and force attached eID Server case
            val psp = token!!.getPathSecurityParameters()
            if (psp.getPSK() == null || psp.getPSK().size == 0) {
                token!!.setPathSecurityParameters(null)
            }
        } else if (qName.equals(ALLOWED_CARD_TYPE, ignoreCase = true)) {
            token!!.getAllowedCardType().add(value)
        }
    }

    @Throws(SAXException::class)
    override fun characters(ch: CharArray, start: Int, length: Int) {
        // Read only the TCToken.
        if (read) {
            for (i in start..<(start + length)) {
                // Ignore whitespaces and control characters like line breaks.
                if (!Character.isISOControl(ch[i]) && !Character.isWhitespace(ch[i])) {
                    sb!!.append(ch[i])
                }
            }
        }
    }

    val tCTokens: MutableList<TCToken?>
        /**
         * Returns the list of TCTokens.
         *
         * @return TCTokens
         */
        get() = tokens!!

    companion object {
        private const val TC_TOKEN_TYPE = "TCTokenType"
        private const val SERVER_ADDRESS = "ServerAddress"
        private const val SESSION_IDENTIFIER = "SessionIdentifier"
        private const val ERROR_ADDRESS = "CommunicationErrorAddress"
        private const val REFRESH_ADDRESS = "RefreshAddress"
        private const val PATH_SECURITY_PROTOCOL = "PathSecurity-Protocol"
        private const val BINDING = "Binding"
        private const val PATH_SECURITY_PARAMETERS = "PathSecurity-Parameters"
        private const val PSK = "PSK"
        private const val ALLOWED_CARD_TYPE = "AllowedCardType"
    }
}
