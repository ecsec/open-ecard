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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openecard.addon.manifest.ScalarListEntryType;
import org.openecard.gui.swing.common.GUIDefaults;
import org.openecard.richclient.gui.manage.Settings;


/**
 * This class models a graphical representation of a ScalarListEntry from the add-on manifest specification.
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class ScalarListItem extends JPanel {

    /**
     * Icon for the minus button which indicates the removal of an entry.
     */
    private final static Icon openedIndicator = GUIDefaults.getImage("ToggleText.selectedIcon");

    /**
     * Icon for the plus button which indicates the addition of a new empty row.
     */
    private final static Icon closedIndicator = GUIDefaults.getImage("ToggleText.icon");


    /**
     * An ArrayList which contains all JTextFields used on the GUI.
     */
    private final ArrayList<JTextField> textFieldList;

    /**
     * An ArrayList containing all JSpinners used on the GUI.
     */
    private final ArrayList<JSpinner> spinnerList;

    /**
     * The type of the ScalarListEntry.
     */
    private final ScalarListEntryType type;

    /**
     * Setting object which contains the value of the property {@link ScalarListItem#property}.
     */
    private final Settings properties;

    /**
     * The property which is managed by this ScalarListItem.
     */
    private final String property;


    /**
     * The current number of rows in this ScalarListItem.
     */
    private int currentRow = 0;

    /**
     * A reference to the last plus button.
     */
    private JButton lastPlus;


    /**
     * Creates a new ScalarListItem from the given {@code property} and {@code type}.
     *
     * @param property The property which is represented by this ScalarListEntry.
     * @param properties Settings object which manages the setting of changed property values in the configuration file.
     * @param type The type of the ScalarListEntry which is managed by this ScalarListItem.
     */
    public ScalarListItem(@Nonnull String property, @Nonnull Settings properties, @Nonnull ScalarListEntryType type) {
	if (type.equals(ScalarListEntryType.BIGDECIMAL)) {
	    this.type = ScalarListEntryType.BIGDECIMAL;
	} else if (type.equals(ScalarListEntryType.BIGINTEGER)) {
	    this.type = ScalarListEntryType.BIGINTEGER;
	} else {
	    this.type = ScalarListEntryType.STRING;
	}

	// basic variable and set layout
	this.properties = properties;
	this.property = property;
	setLayout(new GridBagLayout());

	// initialize the managed list dependent on the type.
	if (type.equals(ScalarListEntryType.BIGDECIMAL) || type.equals(ScalarListEntryType.BIGINTEGER)) {
	    spinnerList = new ArrayList<>();
	    textFieldList = null;
	} else {
	    textFieldList = new ArrayList<>();
	    spinnerList = null;
	}

	// fill the GUI with stored entries
	String entriesList = properties.getProperty(property);
	if (entriesList != null) {
	    String[] singleEntries = entriesList.split(";");
	    for (String entry : singleEntries) {
		addRow(entry);
	    }
	} else {
	    addEmptyRow();
	}
    }

    /**
     * Add an empty row to the layout.
     */
    private void addEmptyRow() {
	addRow(null);
    }

    /**
     * Add an entry with the value {@code entry} to the layout.
     * If {@code entry} is NULL an empty entry is created.
     *
     * @param entry Value to set in the spinner or text field.
     */
    private void addRow(String entry) {

	JComponent item = null;
	switch (type) {
	    case BIGDECIMAL:
	    case BIGINTEGER:
		JSpinner spinner = createNumberEntry(entry);
		spinnerList.add(spinner);
		item = spinner;
		break;
	    case STRING:
		JTextField field = createStringEntry(entry);
		textFieldList.add(field);
		item = field;
		break;
	}

	addComponent(item);

    }

    /**
     * Adds a JSpinner or JTextField to the layout.
     *
     * @param item The element to place on the panel.
     */
    private void addComponent(JComponent item) {
	// create a new plus button and hide the previous one
	JButton plusButton = createButton(true, item);
	if (lastPlus != null) {
	    lastPlus.setVisible(false);
	}
	lastPlus = plusButton;

	// create minus button
	JButton minusButton = createButton(false, item);

	// GridBagConstraints for the item to add
	GridBagConstraints componentConstraints = new GridBagConstraints();
	componentConstraints.anchor = GridBagConstraints.WEST;
	componentConstraints.fill = GridBagConstraints.HORIZONTAL;
	componentConstraints.gridx = 0;
	componentConstraints.gridy = currentRow;
	componentConstraints.weightx = 1.0;
	add(item, componentConstraints);

	// Constraint for the plus button
	GridBagConstraints plusConstraints = new GridBagConstraints();
	plusConstraints.anchor = GridBagConstraints.WEST;
	plusConstraints.fill = GridBagConstraints.NONE;
	plusConstraints.gridx = 2;
	plusConstraints.gridy = currentRow;
	add(plusButton, plusConstraints);

	// constraint for the minus button
	GridBagConstraints minusConstraints = new GridBagConstraints();
	minusConstraints.anchor = GridBagConstraints.WEST;
	minusConstraints.fill = GridBagConstraints.NONE;
	minusConstraints.gridx = 1;
	minusConstraints.gridy = currentRow;
	minusConstraints.insets = new Insets(0, 10, 0, 10);
	add(minusButton, minusConstraints);

	// increase the currentRow to place the next entry in the next row
	currentRow++;
    }

    /**
     * Create a plus or minus button dependent on the given {@code isAdd} variable.
     *
     * @param isAdd boolean variable which indicates whether to create a plus or minus button. A plus button is created
     * if the variable is {@code true} else a minus button is created.
     * @param item The item which is associated with this button.
     * @return A {@link JButton} dependent on the input parameters.
     */
    private JButton createButton(boolean isAdd, JComponent item) {
	JButton button = new JButton();
	if (isAdd) {
	    button.setIcon(closedIndicator);
	    button.addActionListener(new AddRowListener(button, item));
	} else {
	    button.setIcon(openedIndicator);
	    button.addActionListener(new RemoveRowListener(item));
	}

	button.setOpaque(true);
	button.setFocusPainted(false);
	button.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
	button.setHorizontalAlignment(SwingConstants.LEFT);
	button.setMargin(new Insets(0, 0, 0, 0));
	button.setBounds(0, 0, 0, 0);
	button.setContentAreaFilled(false);
	button.setHorizontalTextPosition(SwingConstants.TRAILING);
	return button;
    }

    /**
     * Creates a JSpinner which is able to handle BigInteger or BigDecimal entries.
     *
     * @param value The initial value of the spinner.
     * @return A JSpinner which displays the value {@code value}.
     */
    private JSpinner createNumberEntry(String value) {
	JSpinner spinner = new JSpinner() {

	    @Override
	    public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		dim.width = 100;
		return dim;
	    }
	};
	if (type.equals(ScalarListEntryType.BIGDECIMAL)) {
	    BigDecimal val;
	    if (value == null || value.equals("")) {
		val = new BigDecimal("0");
	    } else {
		val = new BigDecimal(value);
	    }
	    spinner.setModel(new SpinnerMathNumberModel(val, null, null, new BigDecimal("0.1")));
	} else {
	    BigInteger val;
	    if (value == null || value.equals("")) {
		val = new BigInteger("0");
	    } else {
		val = new BigInteger(value, 10);
	    }
	    spinner.setModel(new SpinnerMathNumberModel(val, null, null, new BigInteger("1")));
	}
	spinner.setEditor(new MathNumberEditor(spinner, (DecimalFormat) DecimalFormat.getInstance(spinner.getLocale())));
	spinner.addChangeListener(new SpinnerValueChangedListener());
	return spinner;
    }

    /**
     * Creates a JTextField from the given {@code value}.
     *
     * @param value A text to display in the {@link JTextField}.
     * @return A {@link JTextField} which displayes the value.
     */
    private JTextField createStringEntry(String value) {
	final JTextField input = new JTextField(value) {

	    @Override
	    public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		dim.width = 100;
		return dim;
	    }
	};
	// add listener for value changes
	input.getDocument().addDocumentListener(new DocumentJoinListener());
	return input;
    }

    /**
     * Creates a JCheckBox which represents a boolean type ScalarListEntry.
     *
     * @param value Indicator whether the JCheckBox is checked or not.
     * @return A {@link JCheckBox} depending on the {@code value}.
     */
    private JCheckBox createBooleanEntry(String value) {
	final JCheckBox box = new JCheckBox();

	if (value != null && value.equalsIgnoreCase("true")) {
	    box.setSelected(true);
	} else {
	    box.setSelected(false);
	}

	box.addItemListener(new ItemListener() {

	    @Override
	    public void itemStateChanged(ItemEvent e) {
		properties.setProperty(property, Boolean.toString(box.isSelected()));
	    }
	});

	return box;
    }

    /**
     * Rebuilds the complete panel for the case a row have to be removed.
     * Necessary because the GridBagLayout does not allow to remove a row directly.
     */
    private void rebuild() {
	removeAll();
	currentRow = 0;
	if (textFieldList == null) {
	    spinnerList.clear();
	} else {
	    textFieldList.clear();
	}
	String props = properties.getProperty(property);
	if (props != null && ! props.equals("")) {
	    String[] props2 = props.split(";");
	    for (String entry : props2) {
		addRow(entry);
	    }
	} else {
	    addEmptyRow();
	}

	revalidate();
	repaint();
    }

    /**
     * DocumentListener implementation which manages value changes in a JTextField of a ScalarListItem.
     */
    private class DocumentJoinListener implements DocumentListener {

	@Override
	public void insertUpdate(DocumentEvent e) {
	    properties.setProperty(property, joinContent());
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
	    properties.setProperty(property, joinContent());
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	    // ignore
	}

	/**
	 * Joins the content of all JTextField on the GUI.
	 *
	 * @return A String of semicolon separated strings gathered from all TextField on the GUI.
	 */
	private String joinContent() {
	    StringBuilder sb = new StringBuilder();
	    for (JTextField field : textFieldList) {
		if (field.getText() != null && ! field.getText().equals("")) {
		    sb.append(field.getText());
		    sb.append(";");
		}
	    }

	    return sb.toString();
	}

    }

    /**
     * ChangeListener implementation which manages the merging and joining of the JSpinner on a ScalarListItem.
     */
    private class SpinnerValueChangedListener implements ChangeListener {

	@Override
	public void stateChanged(ChangeEvent e) {
	    StringBuilder sb = new StringBuilder();
	    for (JSpinner spinner : spinnerList) {
		if (type.equals(ScalarListEntryType.BIGDECIMAL)) {
		    BigDecimal dec = (BigDecimal) spinner.getModel().getValue();
		    sb.append(dec.toPlainString());
		} else {
		    BigInteger inte = (BigInteger) spinner.getModel().getValue();
		    sb.append(inte.toString(10));
		}
		sb.append(";");
	    }

	    properties.setProperty(property, sb.toString());
	}

    }

    /**
     * ActionListener implementation which dynamically adds a new JTextField or JSpinner to the layout.
     */
    private class AddRowListener implements ActionListener {

	private final JButton button;
	private final JComponent field;

	/**
	 * Creates a new AddRowListener dependent on the given components.
	 *
	 * @param button The button which is associated with this Listener.
	 * @param field
	 */
	private AddRowListener(JButton button, JComponent field) {
	    this.button = button;
	    this.field = field;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    if (textFieldList != null &&  ! ((JTextField) field).getText().equals("")) {
		addEmptyRow();
		button.setVisible(false);
	    } else if (spinnerList != null && properties.getProperty(property) != null) {
		addEmptyRow();
		button.setVisible(false);
	    }
	}

    }

    /**
     * ActionListener implementation which removes a row from the layout.
     */
    private class RemoveRowListener implements ActionListener {

	private final JComponent item;

	/**
	 * Creates a  new RemoveRowListener dependent on the input {@code item}.
	 *
	 * @param item A JComponent which shall be removed. This a have to be a JSpinner or a JTextField.
	 */
	private RemoveRowListener(JComponent item) {
	    this.item = item;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    if (properties.getProperty(property) != null) {
		if (textFieldList == null) {
		    if (type.equals(ScalarListEntryType.BIGDECIMAL)) {
			String currentVal = ((BigDecimal) ((JSpinner) item).getModel().getValue()).toPlainString() + ";";
			String props = properties.getProperty(property);
			props = props.replaceFirst(currentVal, "");
			properties.setProperty(property, props);
		    } else {
			String currentVal = ((BigInteger) ((JSpinner) item).getModel().getValue()).toString(10) + ";";
			String props = properties.getProperty(property);
			props = props.replaceFirst(currentVal, "");
			properties.setProperty(property, props);
		    }
		    spinnerList.remove((JSpinner) item);
		} else {
		    ((JTextField) item).setText("");
		    textFieldList.remove((JTextField) item);
		}
	    }
	    rebuild();
	}

    }
}
