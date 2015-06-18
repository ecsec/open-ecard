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

package org.openecard.richclient.gui.components;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.swing.AbstractSpinnerModel;


/**
 *
 * @author Hans-Martin Haase
 */
public class SpinnerMathNumberModel extends AbstractSpinnerModel implements Serializable {

    private Number value;
    private Number stepSize;
    private Comparable maximum;
    private Comparable minimum;

    public SpinnerMathNumberModel(BigInteger value, BigInteger minimum, BigInteger maximum, BigInteger stepSize) {
	spinnerMathNumberModelSetter(value, minimum, maximum, stepSize);
    }

    public SpinnerMathNumberModel(BigDecimal value, BigDecimal minimum, BigDecimal maximum, BigDecimal stepSize) {
	spinnerMathNumberModelSetter(value, minimum, maximum, stepSize);
    }

    private void spinnerMathNumberModelSetter(Number value, Comparable minimum, Comparable maximum, Number stepSize) {
	if (value == null) {
	    throw new IllegalArgumentException("NULL is not allowed as value.");
	}
	this.value = value;

	if (stepSize == null) {
	    throw new IllegalArgumentException("The value NULL for the stepSize field is invalid.");
	}
	this.stepSize = stepSize;
	this.maximum = maximum;
	this.minimum = minimum;
    }

    private Number incrementValue(int dir) {
	Number newValue = null;
	switch(dir) {
	    case -1:
		if (value instanceof BigInteger) {
		    BigInteger oldValue = (BigInteger) value;
		    newValue = oldValue.subtract((BigInteger) stepSize);
		} else {
		    BigDecimal oldValue = (BigDecimal) value;
		    newValue = oldValue.subtract((BigDecimal) stepSize);
		}
		break;
	    case 1:
		if (value instanceof BigInteger) {
		    BigInteger oldValue = (BigInteger) value;
		    newValue = oldValue.add((BigInteger) stepSize);
		} else {
		    BigDecimal oldValue = (BigDecimal) value;
		    newValue = oldValue.add((BigDecimal) stepSize);
		}
		break;
	}

	return newValue;
    }

    @Override
    public Object getValue() {
	return value;
    }

    @Override
    public void setValue(Object value) {
	if (value == null) {
	    throw new IllegalArgumentException("NULL is not allowed as value.");
	}

	if (! (value instanceof BigInteger) && ! (value instanceof BigDecimal)) {
	    throw new IllegalArgumentException("The argument must be of type BigInteger or BigDecimal");
	}

	if (! this.value.getClass().equals(value.getClass())) {
	    String msg = "The current value is of type " + this.value.getClass().getSimpleName() + " and the given value "
		    + "is of type " + value.getClass().getSimpleName();
	    throw new IllegalArgumentException(msg);
	}

	if (this.value instanceof BigInteger) {
	    BigInteger convValue = (BigInteger) value;
	    if (minimum != null && convValue.compareTo((BigInteger) minimum) == -1) {
		this.value = (BigInteger) minimum;
	    } else if (maximum != null && convValue.compareTo((BigInteger) maximum) == 1) {
		this.value = (BigInteger) maximum;
	    } else {
		this.value = convValue;
	    }
	} else {
	    BigDecimal convValue = (BigDecimal) value;
	    if (minimum != null && convValue.compareTo((BigDecimal) minimum) == -1) {
		this.value = (BigDecimal) minimum;
	    } else if (maximum != null && convValue.compareTo((BigDecimal) maximum) == 1) {
		this.value = (BigDecimal) maximum;
	    } else {
		this.value = (BigDecimal) value;
	    }
	}
	fireStateChanged();
    }

    @Override
    public Object getNextValue() {
	return incrementValue(1);
    }

    @Override
    public Object getPreviousValue() {
	return incrementValue(-1);
    }

    public Number getMinimum() {
	if (minimum instanceof BigInteger) {
	    return (BigInteger) minimum;
	} else {
	    return (BigDecimal) minimum;
	}
    }

    public Number getMaximum() {
	if (maximum instanceof BigDecimal) {
	    return (BigDecimal) maximum;
	} else {
	    return (BigInteger) maximum;
	}
    }
}
