/****************************************************************************
 * Copyright (C) 2013-2014 ecsec GmbH.
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

package org.openecard.richclient.gui.manage;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import org.openecard.addon.AddonPropertiesException;
import org.openecard.richclient.gui.manage.SettingsFactory.Settings;


/**
 * Aggregator class for settings entries.
 * The entries form a group with an optional caption.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class SettingsGroup extends JPanel {

    private static final long serialVersionUID = 1L;

    protected final Settings properties;
    private final JPanel container;
    private final HashMap<Component, JLabel> fieldLabels;
    private int itemIdx;

    /**
     * Creates an instance bound to a set of properties.
     *
     * @param title Optional title to display as group caption.
     * @param settings Settings object which wraps a Properties object or an AddonProperties object.
     */
    public SettingsGroup(@Nullable String title, @Nonnull Settings settings) {
	this.properties = settings;
	this.fieldLabels = new HashMap<>();

	Border frameBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
	if (title != null) {
	    TitledBorder titleBorder = BorderFactory.createTitledBorder(frameBorder, title);
	    titleBorder.setTitleJustification(TitledBorder.LEADING);
	    titleBorder.setTitlePosition(TitledBorder.TOP);
	    titleBorder.setTitleFont(new JLabel().getFont().deriveFont(Font.BOLD));
	    frameBorder = titleBorder;
	}
	setBorder(frameBorder);
	setLayout(new BorderLayout());

	// configure tuple container
	container = new JPanel();
	add(container, BorderLayout.NORTH);
	GridBagLayout layout = new GridBagLayout();
	layout.columnWidths = new int[]{0, 10, 0, 0};
	layout.rowHeights = new int[]{0, 0};
	layout.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
	layout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
	container.setLayout(layout);
    }

    /**
     * Saves the bound properties.
     * This method is abstract, so that the overriding class can decide how and where to save the properties.
     *
     * @throws IOException Thrown in case the properties could not be written to the output device.
     * @throws SecurityException Thrown in case the permission to save the properties is missing.
     * @throws org.openecard.addon.AddonPropertiesException
     */
    protected void saveProperties() throws IOException, SecurityException, AddonPropertiesException {
	properties.store();
    }

    /**
     * Enables or disables entries in the group.
     * The entry is identified by its input field which is returned in the add functions. When disabled the entry
     * disappears.
     *
     * @param element Input element identifying the entry.
     * @param enabled True whe element should be enabled, false otherwise.
     * @see #addInputItem(java.lang.String, java.lang.String, java.lang.String)
     * @see #addBoolItem(java.lang.String, java.lang.String, java.lang.String)
     * @see #addSelectionItem(java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
     * @see #addMultiSelectionItem(java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
     */
    protected void setEnabledComponent(Component element, boolean enabled) {
	JLabel label = fieldLabels.get(element);
	label.setVisible(enabled);
	element.setVisible(enabled);
    }


    /**
     * Adds an input field to the group.
     * The specified property is bound to the input and updates when the value changes.
     *
     * @param name Name displayed on the label besides the input element.
     * @param description Optional tooltip description visible when hovering the label.
     * @param property Property entry this element is bound to.
     * @return The input element which has been created and added to the entry.
     */
    protected JTextField addInputItem(@Nonnull String name, @Nullable String description,
	    final @Nonnull String property) {
	JLabel label = addLabel(name, description);

	String value = properties.getProperty(property);
	value = value == null ? "" : value;
	final JTextField input = new JTextField(value);
	fieldLabels.put(input, label);
	// add listener for value changes
	input.getDocument().addDocumentListener(new DocumentListener() {
	    @Override
	    public void insertUpdate(DocumentEvent e) {
		properties.setProperty(property, input.getText());
	    }
	    @Override
	    public void removeUpdate(DocumentEvent e) {
		properties.setProperty(property, input.getText());
	    }
	    @Override
	    public void changedUpdate(DocumentEvent e) {
		// ignore
	    }
	});
	addComponent(input);
	itemIdx++;

	return input;
    }

    @Deprecated
    private void addListInputItem(@Nonnull String name, @Nullable String description, final @Nonnull String property) {
	JLabel label = addLabel(name, description);

	String values = properties.getProperty(property);
	String[] entries;
	if (values == null) {
	    entries = new String[0];
	} else {
	    entries = values.split(";");
	}
	Vector<Vector<String>> rowData = new Vector<>();
	Vector<String> columnData = new Vector<>();
	columnData.add("Provider ID");
	columnData.add("URL");
	final DefaultTableModel model = new DefaultTableModel() {

	    @Override
	    public void setValueAt(Object aValue, int row, int column) {
		super.setValueAt(aValue, row, column);
		if (! aValue.toString().trim().isEmpty()) {
		    if (shouldAddRow(row, column)) {
			addRow(new Object[] {});
		    }
		}
	    }

	    private boolean shouldAddRow(int lastEditedRow, int lastEditedColumn) {
		// true if we are in the last row
		return lastEditedRow == getRowCount() - 1;
	    }

	};

	model.addTableModelListener(new TableModelListener() {
	    @Override
	    public void tableChanged(TableModelEvent e) {
		StringBuilder sb = new StringBuilder();
		for (int rowNumber = 0; rowNumber < model.getRowCount(); rowNumber++) {
		    for (int columnNumber = 0; columnNumber < model.getColumnCount(); columnNumber++) {
			Object valueAt = model.getValueAt(rowNumber, columnNumber);
			if (valueAt != null && !valueAt.toString().trim().isEmpty()) {
			    sb.append(valueAt.toString());
			    if (columnNumber == model.getColumnCount() - 1) {
				sb.append(";");
			    } else {
				sb.append(",");
			    }
			}
		    }
		}
		properties.setProperty(property, sb.toString());
	    }

	});
	for (String entry : entries) {
	    if (entry.split(",").length < 2) {
		continue;
	    }
	    String key = entry.split(",")[0];
	    String value = entry.split(",")[1];
	    Vector<String> row = new Vector<>();
	    row.add(key);
	    row.add(value);
	    rowData.add(row);
	}
	JTable jTable = new JTable(model);
	model.setDataVector(rowData, columnData);
	model.addRow(new Object[] {});
	fieldLabels.put(jTable, label);
	addComponent(jTable);
	itemIdx++;
    }

    protected JTable addScalarListItem(@Nonnull String name, @Nullable String desc, final @Nonnull String property) {
	JLabel label = addLabel(name, desc);

	String value = properties.getProperty(property);
	ArrayList<String> entries = new ArrayList<>(10);
	if (value != null) {
	    String[] arrayEntries = value.split("\n");
	    Collections.addAll(entries, arrayEntries);

	    // remove leading and trailing ws and remove empty entries
	    ListIterator<String> it = entries.listIterator();
	    while (it.hasNext()) {
		String next = it.next();
		next = next.trim();
		if (next.isEmpty()) {
		    it.remove();
		} else {
		    it.set(next);
		}
	    }
	}

	final JTable input = new JTable(entries.size() + 1, 2);
	// fill in the values from entries
	for (int i = 0; i < entries.size(); i++) {
	    input.getModel().setValueAt(entries.get(i), i, 0);
	    JButton removeButton = new JButton("x");
	    input.getModel().setValueAt(removeButton, i, 1);
	}
	fieldLabels.put(input, label);

	// TODO: add listener

	addComponent(input);
	itemIdx++;

	return input;
    }

    /**
     * Adds a check box to the group.
     * The specified property is bound to the input and updates when the value changes.
     *
     * @param name Name displayed on the label besides the input element.
     * @param description Optional tooltip description visible when hovering the label.
     * @param property Property entry this element is bound to.
     * @return The check box element which has been created and added to the entry.
     */
    protected JCheckBox addBoolItem(@Nonnull String name, @Nullable String description,
	    final @Nonnull String property) {
	JLabel label = addLabel(name, description);

	String value = properties.getProperty(property);
	Boolean boolValue = Boolean.parseBoolean(value);
	final JCheckBox input = new JCheckBox();
	input.setSelected(boolValue);
	fieldLabels.put(input, label);
	// add listener for value changes
	input.addItemListener(new ItemListener() {
	    @Override
	    public void itemStateChanged(ItemEvent e) {
		properties.setProperty(property, Boolean.toString(input.isSelected()));
	    }
	});
	addComponent(input);
	itemIdx++;

	return input;
    }

    /**
     * Adds a selection field to the group.
     * The specified property is bound to the input and updates when the value changes.
     *
     * @param name Name displayed on the label besides the input element.
     * @param description Optional tooltip description visible when hovering the label.
     * @param property Property entry this element is bound to.
     * @param values
     * @return The selection element which has been created and added to the entry.
     */
    protected JComboBox addSelectionItem(@Nonnull String name, @Nullable String description,
	    final @Nonnull String property, @Nonnull String... values) {
	addLabel(name, description);
	final JComboBox comboBox = new JComboBox();
	comboBox.setModel(new DefaultComboBoxModel(values));
	comboBox.setSelectedItem(properties.getProperty(property));
	comboBox.addItemListener(new ItemListener() {
	    @Override
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    properties.setProperty(property, (String) e.getItem());
		}
	    }
	});
	addComponent(comboBox);
	itemIdx++;

	return comboBox;
    }

    /**
     * Adds a selection field capable of selecting multiple values to the group.
     * The specified property is bound to the input and updates when the value changes.
     *
     * @param name Name displayed on the label besides the input element.
     * @param description Optional tooltip description visible when hovering the label.
     * @param property Property entry this element is bound to.
     * @param values Selectable values.
     * @return The selection element which has been created and added to the entry.
     */
    protected JPanel addMultiSelectionItem(@Nonnull String name, @Nullable String description,
	    final @Nonnull String property, @Nonnull List<String> values) {
	JLabel optionName = new JLabel(name);
	optionName.setFont(optionName.getFont().deriveFont(Font.PLAIN));
	optionName.setToolTipText(description);
	JPanel contentPane = new JPanel(new GridBagLayout());
	JPanel checkboxPane = new JPanel(new GridBagLayout());

	int row = 0;
	int col = 0;
	String property2 = properties.getProperty(property);
	for (String value : values) {
	    GridBagConstraints c = new GridBagConstraints();
	    if (col != 0) {
		if (col % 3 == 0) {
		    col = 0;
		    row = row + 1;
		}
	    }
	    c.gridx = col;
	    c.gridy = row;
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.anchor = GridBagConstraints.NORTHWEST;

	    if (property2 != null) {
		String[] multProps = property2.split(";");
		List<String> selectedOpts = Arrays.asList(multProps);
		if (selectedOpts.contains(value)) {
		    checkboxPane.add(new CheckboxListItem(value, true, property), c);
		} else {
		    checkboxPane.add(new CheckboxListItem(value, false, property), c);
		}
	    } else {
		checkboxPane.add(new CheckboxListItem(value, false, property), c);
	    }
	    col++;
	}

	GridBagConstraints c2 = new GridBagConstraints();
	c2.anchor = GridBagConstraints.NORTHWEST;
	c2.fill = GridBagConstraints.HORIZONTAL;
	c2.gridx = 0;
	c2.gridy = 0;
	c2.weightx = 1.0;
	c2.weighty = 1.0;
	contentPane.add(optionName, c2);

	GridBagConstraints c3 = new GridBagConstraints();
	c3.anchor = GridBagConstraints.NORTHWEST;
	c3.fill = GridBagConstraints.HORIZONTAL;
	c3.gridwidth = GridBagConstraints.REMAINDER;
	c3.gridheight = 2;
	c3.gridx = 1;
	c3.gridy = 0;
	c3.weightx = 4.0;
	c3.weighty = 5.0;
	contentPane.add(checkboxPane, c3);

	// add content panel to the group
	GridBagConstraints c = new GridBagConstraints();
	c.insets = new Insets(5, 3, 0, 5);
	c.fill = GridBagConstraints.HORIZONTAL;
	c.gridx = 2;
	c.gridy = itemIdx;
	container.add(contentPane, c);
	//addComponent(contentPane);
	itemIdx++;
	return contentPane;
    }

    private JLabel addLabel(@Nonnull String name, @Nullable String description) {
	JLabel label = new JLabel(name);
	label.setToolTipText(description);
	label.setFont(label.getFont().deriveFont(Font.PLAIN));
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.insets = new Insets(5, 10, 0, 5);
	constraints.gridx = 0;
	constraints.gridy = itemIdx;
	constraints.anchor = GridBagConstraints.WEST;
	container.add(label, constraints);
	return label;
    }

    private void addComponent(@Nonnull Component component) {
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.insets = new Insets(5, 3, 0, 5);
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.gridx = 2;
	constraints.gridy = itemIdx;
	container.add(component, constraints);
    }

    private class CheckboxListItem extends JCheckBox {

	private final String itemLabel;
	private final String propName;

	private CheckboxListItem(String name, boolean selected, String propertyName) {
	    this.setSelected(selected);
	    this.setBackground(container.getBackground());
	    setText(name);
	    itemLabel = name;
	    propName = propertyName;
	    construct();
	}

	private void construct() {
	    addItemListener(new ItemListener() {
		@Override
		public void itemStateChanged(ItemEvent e) {
		    String propValue = properties.getProperty(propName);
		    if (e.getStateChange() == ItemEvent.SELECTED) {
			if (propValue == null) {
			    properties.setProperty(propName, itemLabel);
			} else {
			    // property value is not null so some other options are selected so append the now selected
			    // option
			    properties.setProperty(propName, propValue.concat(";" + itemLabel));
			}
		    } else if (e.getStateChange() == ItemEvent.DESELECTED) {
			if (propValue.equals(itemLabel)) {
			    // just the current was selected so set an empty string
			    properties.setProperty(propName, "");
			} else {
			    // element somewhere between all others
			    if (propValue.contains(";" + itemLabel + ";")) {
				propValue = propValue.replace(";" + itemLabel + ";", ";");
				properties.setProperty(propName, propValue);
			    } else if (propValue.contains(";" + itemLabel)) {
				// last element
				propValue = propValue.replace(";" + itemLabel, ";");
				properties.setProperty(propName, propValue);
			    } else {
				// first element
				propValue = propValue.replace(itemLabel + ";", "");
				properties.setProperty(propName, propValue);
			    }
			}
		    }
		}

	    });
	}
    }

}
