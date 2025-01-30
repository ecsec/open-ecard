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

import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import javax.swing.AbstractSpinnerModel

/**
 *
 * @author Hans-Martin Haase
 */
class SpinnerMathNumberModel : AbstractSpinnerModel, Serializable {
    private var value: Number? = null
    private var stepSize: Number? = null
    private var maximum: Comparable<*>? = null
    private var minimum: Comparable<*>? = null

    constructor(value: BigInteger, minimum: BigInteger?, maximum: BigInteger?, stepSize: BigInteger) {
        spinnerMathNumberModelSetter(value, minimum, maximum, stepSize)
    }

    constructor(value: BigDecimal, minimum: BigDecimal?, maximum: BigDecimal?, stepSize: BigDecimal) {
        spinnerMathNumberModelSetter(value, minimum, maximum, stepSize)
    }

    private fun spinnerMathNumberModelSetter(
        value: Number,
        minimum: Comparable<*>?,
        maximum: Comparable<*>?,
        stepSize: Number
    ) {
        requireNotNull(value) { "NULL is not allowed as value." }
        this.value = value

        requireNotNull(stepSize) { "The value NULL for the stepSize field is invalid." }
        this.stepSize = stepSize
        this.maximum = maximum
        this.minimum = minimum
    }

    private fun incrementValue(dir: Int): Number? {
        var newValue: Number? = null
        when (dir) {
            -1 -> if (value is BigInteger) {
                val oldValue: BigInteger = value as BigInteger
                newValue = oldValue.subtract(stepSize as BigInteger?)
            } else {
                val oldValue: BigDecimal? = value as BigDecimal?
                newValue = oldValue!!.subtract(stepSize as BigDecimal?)
            }

            1 -> if (value is BigInteger) {
                val oldValue: BigInteger = value as BigInteger
                newValue = oldValue.add(stepSize as BigInteger?)
            } else {
                val oldValue: BigDecimal? = value as BigDecimal?
                newValue = oldValue!!.add(stepSize as BigDecimal?)
            }
        }

        return newValue
    }

    override fun getValue(): Any {
        return value!!
    }

    override fun setValue(value: Any) {
        requireNotNull(value) { "NULL is not allowed as value." }

        require(!(value !is BigInteger && value !is BigDecimal)) { "The argument must be of type BigInteger or BigDecimal" }

        if (this.value!!.javaClass != value.javaClass) {
            val msg: String =
                ("The current value is of type " + this.value!!.javaClass.getSimpleName() + " and the given value "
                        + "is of type " + value.javaClass.getSimpleName())
            throw IllegalArgumentException(msg)
        }

        if (this.value is BigInteger) {
            val convValue: BigInteger = value as BigInteger
            if (minimum != null && convValue.compareTo(minimum as BigInteger) == -1) {
                this.value = minimum as BigInteger
            } else if (maximum != null && convValue.compareTo(maximum as BigInteger) == 1) {
                this.value = maximum as BigInteger
            } else {
                this.value = convValue
            }
        } else {
            val convValue: BigDecimal = value as BigDecimal
            if (minimum != null && convValue.compareTo(minimum as BigDecimal) == -1) {
                this.value = minimum as BigDecimal
            } else if (maximum != null && convValue.compareTo(maximum as BigDecimal) == 1) {
                this.value = maximum as BigDecimal
            } else {
                this.value = value
            }
        }
        fireStateChanged()
    }

    override fun getNextValue(): Any {
        return incrementValue(1)!!
    }

    override fun getPreviousValue(): Any {
        return incrementValue(-1)!!
    }

    fun getMinimum(): Number? {
        if (minimum is BigInteger) {
            return minimum as BigInteger
        } else {
            return minimum as BigDecimal?
        }
    }

    fun getMaximum(): Number? {
        if (maximum is BigDecimal) {
            return maximum as BigDecimal
        } else {
            return maximum as BigInteger?
        }
    }
}
