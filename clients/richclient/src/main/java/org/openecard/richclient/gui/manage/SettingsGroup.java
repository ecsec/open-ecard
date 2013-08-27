/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
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


/**
 * Aggregator class for settings entries.
 * The entries form a group with an optional caption.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public abstract class SettingsGroup extends JPanel {

    private static final long serialVersionUID = 1L;

    protected final Properties properties;
    private final JPanel container;
    private final HashMap<Component, JLabel> fieldLabels;
    private int itemIdx;

    /**
     * Creates an instance bound to a set of properties.
     *
     * @param title Optional title to display as group caption.
     * @param properties Properties bound to the entries in this group.
     */
    public SettingsGroup(@Nullable String title, @Nonnull Properties properties) {
	this.properties = properties;
	this.fieldLabels = new HashMap<Component, JLabel>();

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
     */
    protected abstract void saveProperties() throws IOException, SecurityException;

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

    protected void addListInputItem(@Nonnull String name, @Nullable String description, final @Nonnull String property) {
	JLabel label = addLabel(name, description);

	String values = properties.getProperty(property);
	String[] entries;
	if (values == null) {
	    entries = new String[0];
	} else {
	    entries = values.split(";");
	}
	Vector<Vector<String>> rowData = new Vector<Vector<String>>();
	Vector<String> columnData = new Vector<String>();
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
	    Vector<String> row = new Vector<String>();
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
	return;
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
		properties.setProperty(property, Boolean.valueOf(input.isSelected()).toString());
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
     * @return The selection element which has been created and added to the entry.
     */
    protected void addMultiSelectionItem(@Nonnull String name, @Nullable String description,
	    final @Nonnull String property, @Nonnull String... values) {
	addLabel(name, description);
	// TODO:
	itemIdx++;
    }

    private JLabel addLabel(@Nonnull String name, @Nullable String description) {
	JLabel label = new JLabel(name);
	label.setToolTipText(description);
	label.setFont(label.getFont().deriveFont(Font.PLAIN));
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.insets = new Insets(5, 10, 0, 5);
	constraints.gridx = 0;
	constraints.gridy = itemIdx;
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

}
