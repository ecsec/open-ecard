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

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.ParseException
import javax.swing.JFormattedTextField
import javax.swing.JSpinner
import javax.swing.JTextField
import javax.swing.text.DefaultFormatterFactory
import javax.swing.text.NumberFormatter
import kotlin.math.max

/**
 *
 * @author Hans-Martin Haase
 */
class MathNumberEditor(spinner: JSpinner, format: DecimalFormat?) : JSpinner.DefaultEditor(spinner) {
    /**
     * Construct a `JSpinner` editor that supports displaying and editing the value of a
     * `SpinnerNumberModel` with a `JFormattedTextField`.
     * This `NumberEditor` becomes both a `ChangeListener` on the spinner and a
     * `PropertyChangeListener` on the new `JFormattedTextField`.
     *
     * @param spinner The spinner whose model this editor will monitor.
     * @param format The initial pattern for the `DecimalFormat` object that's used to display and parse
     * the value of the text field.
     * @exception IllegalArgumentException Thrown if the spinners model is not an instance of
     * `SpinnerNumberModel`.
     *
     * @see .getTextField
     * @see javax.swing.SpinnerNumberModel
     *
     * @see java.text.DecimalFormat
     */
    init {
        require(spinner.getModel() is SpinnerMathNumberModel) { "model not a SpinnerMathNumberModel" }

        val model: SpinnerMathNumberModel = spinner.getModel() as SpinnerMathNumberModel
        val formatter: NumberFormatter = MathNumberEditorFormatter(
            model,
            format
        )
        val factory: DefaultFormatterFactory = DefaultFormatterFactory(
            formatter
        )
        val ftf: JFormattedTextField = getTextField()
        ftf.setEditable(true)
        ftf.setFormatterFactory(factory)
        ftf.setHorizontalAlignment(JTextField.RIGHT)

        /* TBD - initializing the column width of the text field
             * is imprecise and doing it here is tricky because
             * the developer may configure the formatter later.
             */
        try {
            val maxString: String = formatter.valueToString(model.getMinimum())
            val minString: String = formatter.valueToString(model.getMaximum())
            ftf.setColumns(
                max(
                    maxString.length.toDouble(),
                    minString.length.toDouble()
                ).toInt()
            )
        } catch (e: ParseException) {
            // TBD should throw a chained error here
        }
    }


    val format: DecimalFormat
        /**
         * Returns the `java.text.DecimalFormat` object the
         * `JFormattedTextField` uses to parse and format
         * numbers.
         *
         * @return the value of `getTextField().getFormatter().getFormat()`.
         * @see .getTextField
         *
         * @see java.text.DecimalFormat
         */
        get() {
            val format: DecimalFormat =
                ((getTextField().getFormatter()) as NumberFormatter).getFormat() as DecimalFormat
            format.setRoundingMode(RoundingMode.HALF_UP)
            return format
        }


    val model: SpinnerMathNumberModel
        /**
         * Return our spinner ancestor's `SpinnerMathNumberModel`.
         *
         * @return `getSpinner().getModel()`
         * @see .getSpinner
         *
         * @see .getTextField
         */
        get() {
            return (getSpinner().getModel()) as SpinnerMathNumberModel
        }
}
