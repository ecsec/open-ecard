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
package org.openecard.richclient.gui.components

import java.math.BigDecimal
import java.math.BigInteger
import java.text.Format
import java.text.NumberFormat
import java.text.ParseException
import javax.swing.text.NumberFormatter

/**
 *
 * @author Hans-Martin Haase
 */
class MathNumberEditorFormatter(private val model: SpinnerMathNumberModel, format: NumberFormat?) : NumberFormatter() {
    init {
        setFormat(format)
        setOverwriteMode(false)
        setAllowsInvalid(true)
        setCommitsOnValidEdit(false)
        setValueClass(model.getValue().javaClass)
    }

    /**
     * Converts the passed in value to the passed in class. This only
     * works if `valueClass` is one of `Integer`,
     * `Long`, `Float`, `Double`,
     * `Byte` or `Short` and `value`
     * is an instanceof `Number`.
     */
    private fun convertValueToValueClass(value: Any, valueClass: Class<*>?): Any {
        if (valueClass != null && (value is Number)) {
            val numberValue: Number = value
            if (valueClass == BigInteger::class.java) {
                return numberValue as BigInteger
            } else if (valueClass == BigDecimal::class.java) {
                return numberValue as BigDecimal
            }
        }
        return value
    }

    /**
     * Invokes `parseObject` on `f`, returning
     * its value.
     */
    @Throws(ParseException::class)
    fun stringToValue(text: String?, f: Format?): Any? {
        if (f == null) {
            return text
        }
        val value: Any = f.parseObject(text)

        return convertValueToValueClass(value, getValueClass())
    }
}
