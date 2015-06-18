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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.text.NumberFormatter;


/**
 *
 * @author Hans-Martin Haase
 */
public class MathNumberEditorFormatter extends NumberFormatter {

    private final SpinnerMathNumberModel model;

    public MathNumberEditorFormatter(SpinnerMathNumberModel model, NumberFormat format) {
	this.model = model;
	setFormat(format);
	setOverwriteMode(false);
	setAllowsInvalid(true);
	setCommitsOnValidEdit(false);
	setValueClass(model.getValue().getClass());
    }

    /**
     * Converts the passed in value to the passed in class. This only
     * works if <code>valueClass</code> is one of <code>Integer</code>,
     * <code>Long</code>, <code>Float</code>, <code>Double</code>,
     * <code>Byte</code> or <code>Short</code> and <code>value</code>
     * is an instanceof <code>Number</code>.
     */
    private Object convertValueToValueClass(Object value, Class valueClass) {
        if (valueClass != null && (value instanceof Number)) {
            Number numberValue = (Number)value;
            if (valueClass == BigInteger.class) {
                return (BigInteger) numberValue;
            }
            else if (valueClass == BigDecimal.class) {
                return (BigDecimal) numberValue;
            }
        }
        return value;
    }

    /**
     * Invokes <code>parseObject</code> on <code>f</code>, returning
     * its value.
     */
    Object stringToValue(String text, Format f) throws ParseException {
        if (f == null) {
            return text;
        }
        Object value = f.parseObject(text);

        return convertValueToValueClass(value, getValueClass());
    }

}
