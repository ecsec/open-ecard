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
package org.openecard.common.apdu.common

import iso.std.iso_iec._24727.tech.schema.CardCallTemplateType
import org.openecard.common.apdu.common.APDUTemplateException
import org.openecard.common.util.*
import java.util.regex.Pattern
import javax.annotation.Nonnull

/**
 * Command APDU template.
 * The template is defined by [CardCallTemplateType]. It's underlying type in the xsd document contains a detailed
 * description how template values are evaluated.
 *
 * @author Tobias Wich
 */
class CardCommandTemplate
/**
 * Creates the template class based on the given data type class.
 *
 * @param apduTemplate Data type with values needed to evaluate the template to an APDU.
 */(@param:Nonnull private val template: CardCallTemplateType) {
    /**
     * Evaluate the template defintion wrapped up in this class against the given context.
     * The available values in the context object are dependent on the usage scenario. For example when used in a
     * signature context, there will be variables available for the algorithms and similar information.
     *
     * @param context Context containing functions and values.
     * @return A CardCommandAPDU which can be serialized and used in Transmit and the like.
     * @throws APDUTemplateException Thrown in case no APDU can be derived from the template.
     */
    @Nonnull
    @Throws(APDUTemplateException::class)
    fun evaluate(@Nonnull context: Map<String, Any>): CardCommandAPDU {
        val head = evalTemplate(template.headerTemplate, context)
        val data = evalTemplate(template.dataTemplate, context)
        val length = template.expectedLength

        // a few sanity checks
        if (head.size != 4) {
            throw APDUTemplateException("The computed command APDU header is not valid.")
        }

        val apdu = CardCommandAPDU()
        apdu.cla = head[0]
        apdu.ins = head[1]
        apdu.p1 = head[2]
        apdu.p2 = head[3]
        if (data.size > 0) {
            apdu.setData(data)
        }
        if (length != null) {
            apdu.le = length.toInt()
        }

        return apdu
    }

    @Nonnull
    @Throws(APDUTemplateException::class)
    private fun evalTemplate(s: String?, @Nonnull context: Map<String, Any>): ByteArray {
        var s = s ?: return ByteArray(0)

        val m = EXPRESSION.matcher(s)
        while (m.find()) {
            // get matching group and cut off the curlies
            var expr = m.group()
            expr = expr.substring(1, expr.length - 1)

            // get tokens and produce
            val tokens = getTokens(expr)
            val firstObj = getFirstObject(tokens, context)

            // evaluate value or function template
            val result: String?
            if (tokens.size == 1) {
                result = evalObject(firstObj)
            } else {
                val params = getParameters(tokens, context)
                result = evalObject(firstObj, params)
            }

            // cut expression and replace with result
            s = m.reset().replaceFirst(result)
            m.reset(s)
        }

        val resultBytes = StringUtils.toByteArray(s, true)
        return resultBytes
    }

    @Throws(APDUTemplateException::class)
    private fun evalObject(@Nonnull o: Any, @Nonnull params: Array<Any?> = arrayOfNulls(0)): String? {
        if (o is ByteArray) {
            val result = ByteUtils.toHexString(o)
            return result
        } else if (o is APDUTemplateFunction) {
            val result = o.call(*params)!!
            return result
        } else {
            // this also includes the String class where it is the identity function
            return o.toString()
        }
    }

    @Nonnull
    private fun getTokens(@Nonnull expr: String): Array<String> {
        val groups = expr.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return groups
    }

    @Nonnull
    @Throws(APDUTemplateException::class)
    private fun getFirstObject(@Nonnull tokens: Array<String>, @Nonnull ctx: Map<String, Any>): Any {
        val firstObj = ctx[tokens[0]]
        if (firstObj == null) {
            throw NullPointerException()
        } else if (tokens.size > 1 && firstObj !is APDUTemplateFunction) {
            // TODO: error not a function object
            throw APDUTemplateException("Multiple element template but no function named.")
        } else {
            return firstObj
        }
    }

    @Nonnull
    @Throws(APDUTemplateException::class)
    private fun getParameters(@Nonnull tokens: Array<String>, @Nonnull context: Map<String, Any>): Array<Any?> {
        val result = ArrayList<Any?>(tokens.size - 1)
        // get each token from the context object
        for (i in 1..<tokens.size) {
            val next = tokens[i]
            if (next.startsWith("0x")) {
                val value = next.substring(2)
                result.add(value)
            } else {
                val o = context[next]
                if (o is APDUTemplateFunction) {
                    throw APDUTemplateException("Function used in parameter list.")
                } else {
                    result.add(o)
                }
            }
        }
        return result.toTypedArray()
    }

    companion object {
        private val EXPRESSION: Pattern = Pattern.compile("\\{.*?\\}")
    }
}
