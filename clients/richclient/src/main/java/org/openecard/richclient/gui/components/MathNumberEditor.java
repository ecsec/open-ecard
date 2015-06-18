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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;


/**
 *
 * @author Hans-Martin Haase
 */
public class MathNumberEditor extends DefaultEditor {

        /**
         * Construct a <code>JSpinner</code> editor that supports displaying and editing the value of a
	 * <code>SpinnerNumberModel</code> with a <code>JFormattedTextField</code>.
	 * This <code>NumberEditor</code> becomes both a <code>ChangeListener</code> on the spinner and a
	 * <code>PropertyChangeListener</code> on the new <code>JFormattedTextField</code>.
         *
         * @param spinner The spinner whose model this editor will monitor.
         * @param format The initial pattern for the <code>DecimalFormat</code> object that's used to display and parse
	 *   the value of the text field.
         * @exception IllegalArgumentException Thrown if the spinners model is not an instance of
	 *   <code>SpinnerNumberModel</code>.
         *
         * @see #getTextField()
         * @see javax.swing.SpinnerNumberModel
         * @see java.text.DecimalFormat
         */
        public MathNumberEditor(JSpinner spinner, DecimalFormat format) {
            super(spinner);
            if (!(spinner.getModel() instanceof SpinnerMathNumberModel)) {
                throw new IllegalArgumentException(
                          "model not a SpinnerMathNumberModel");
            }

            SpinnerMathNumberModel model = (SpinnerMathNumberModel)spinner.getModel();
            NumberFormatter formatter = new MathNumberEditorFormatter(model,
                                                                  format);
            DefaultFormatterFactory factory = new DefaultFormatterFactory(
                                                  formatter);
            JFormattedTextField ftf = getTextField();
            ftf.setEditable(true);
            ftf.setFormatterFactory(factory);
            ftf.setHorizontalAlignment(JTextField.RIGHT);

            /* TBD - initializing the column width of the text field
             * is imprecise and doing it here is tricky because
             * the developer may configure the formatter later.
             */
            try {
                String maxString = formatter.valueToString(model.getMinimum());
                String minString = formatter.valueToString(model.getMaximum());
                ftf.setColumns(Math.max(maxString.length(),
                                        minString.length()));
            }
            catch (ParseException e) {
                // TBD should throw a chained error here
            }

        }


        /**
         * Returns the <code>java.text.DecimalFormat</code> object the
         * <code>JFormattedTextField</code> uses to parse and format
         * numbers.
         *
         * @return the value of <code>getTextField().getFormatter().getFormat()</code>.
         * @see #getTextField
         * @see java.text.DecimalFormat
         */
        public DecimalFormat getFormat() {
	    DecimalFormat format = (DecimalFormat)((NumberFormatter)(getTextField().getFormatter())).getFormat();
	    format.setRoundingMode(RoundingMode.HALF_UP);
            return format;
        }


        /**
         * Return our spinner ancestor's <code>SpinnerMathNumberModel</code>.
         *
         * @return <code>getSpinner().getModel()</code>
         * @see #getSpinner
         * @see #getTextField
         */
        public SpinnerMathNumberModel getModel() {
            return (SpinnerMathNumberModel)(getSpinner().getModel());
        }

}
