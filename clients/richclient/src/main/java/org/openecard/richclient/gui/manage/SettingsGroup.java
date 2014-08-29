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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openecard.addon.AddonPropertiesException;
import org.openecard.addon.manifest.ScalarEntryType;
import org.openecard.addon.manifest.ScalarListEntryType;
import org.openecard.common.I18n;
import org.openecard.richclient.gui.components.CheckboxListItem;
import org.openecard.richclient.gui.components.FileListEntryItem;
import org.openecard.richclient.gui.components.MathNumberEditor;
import org.openecard.richclient.gui.components.OpenFileBrowserListener;
import org.openecard.richclient.gui.components.ScalarListItem;
import org.openecard.richclient.gui.components.SpinnerMathNumberModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Aggregator class for settings entries.
 * The entries form a group with an optional caption.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class SettingsGroup extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SettingsGroup.class);

    protected final Settings properties;
    private final I18n lang = I18n.getTranslation("addon");
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
	final JTextField input = new JTextField(value) {
	    @Override
	    public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		dim.width = 100;
		return dim;
	    }
	};
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

    protected ScalarListItem addScalarListItem(@Nonnull String name, @Nullable String desc, final @Nonnull String property,
	    @Nonnull ScalarListEntryType entry) {
	JLabel label = addListLabel(name, desc);

	ScalarListItem item = new ScalarListItem(property, properties, entry);
	fieldLabels.put(item, label);
	addComponent(item);
	itemIdx++;

	return item;
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
	if (value == null || value.equals("")) {
	    properties.setProperty(property, Boolean.FALSE.toString());
	}
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
    protected JComboBox<String> addSelectionItem(@Nonnull String name, @Nullable String description,
	    final @Nonnull String property, @Nonnull String... values) {
	JLabel label = addLabel(name, description);
	JPanel test = new JPanel();
	final JComboBox<String> comboBox = new JComboBox<>(values);

	if (Arrays.asList(values).contains("")) {
	    comboBox.setSelectedItem(properties.getProperty(property));
	} else {
	    // in this case the empty string is not allowed so an option have to be set. We take the first element in the
	    // array.
	    String prop = properties.getProperty(property);
	    if (prop == null || prop.equals("")) {
		comboBox.setSelectedItem(values[0]);
	    } else {
		comboBox.setSelectedItem(prop);
	    }
	}

	comboBox.addItemListener(new ItemListener() {
	    @Override
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    properties.setProperty(property, (String) e.getItem());
		}
	    }
	});

	addComponent(comboBox);
	fieldLabels.put(test, label);
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
	JLabel optionName = addListLabel(name, description);
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
	    c.fill = GridBagConstraints.NONE;
	    c.anchor = GridBagConstraints.NORTHWEST;

	    if (property2 != null) {
		String[] multProps = property2.split(";");
		List<String> selectedOpts = Arrays.asList(multProps);
		if (selectedOpts.contains(value)) {
		    checkboxPane.add(new CheckboxListItem(value, true, property, properties), c);
		} else {
		    checkboxPane.add(new CheckboxListItem(value, false, property, properties), c);
		}
	    } else {
		checkboxPane.add(new CheckboxListItem(value, false, property, properties), c);
	    }

	    if (col == 2) {
		GridBagConstraints c2 = new GridBagConstraints();
		c2.fill = GridBagConstraints.BOTH;
		c2.weightx = 1.0;
		c2.gridx = 3;
		c2.gridy = row;
		checkboxPane.add(new JLabel(), c2);
	    }

	    col++;

	}

	GridBagConstraints c3 = new GridBagConstraints();
	c3.anchor = GridBagConstraints.NORTHWEST;
	c3.fill = GridBagConstraints.HORIZONTAL;
	c3.gridwidth = GridBagConstraints.REMAINDER;
	c3.weightx = 1.0;
	contentPane.add(checkboxPane, c3);

	addComponent(contentPane);
	itemIdx++;
	fieldLabels.put(contentPane, optionName);
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

    private JLabel addListLabel(@Nonnull String name, @Nullable String description) {
	JLabel label = new JLabel(name);
	label.setToolTipText(description);
	label.setFont(label.getFont().deriveFont(Font.PLAIN));
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.insets = new Insets(5, 10, 0, 5);
	constraints.gridx = 0;
	constraints.gridy = itemIdx;
	constraints.anchor = GridBagConstraints.NORTHWEST;
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

    protected JSpinner addScalarEntryTypNumber(@Nonnull String name, @Nullable String description,
	    final @Nonnull String property, @Nonnull String type) {
	JLabel label = addLabel(name, description);
	String value = properties.getProperty(property);
	SpinnerMathNumberModel model;

	if (type.equals(ScalarEntryType.BIGDECIMAL.name())) {
	    if (value == null || value.equals("")) {
		model = new SpinnerMathNumberModel(new BigDecimal("0.0"), null, null, new BigDecimal("0.1"));
	    } else {
		BigDecimal convertedValue = new BigDecimal(value);
		model = new SpinnerMathNumberModel(convertedValue, null, null, new BigDecimal("0.1"));
	    }
	} else if (type.equals(ScalarEntryType.BIGINTEGER.name())) {
	    if (value == null || value.equals("")) {
		model = new SpinnerMathNumberModel(BigInteger.ZERO, null, null, BigInteger.ONE);
	    } else {
		BigInteger convertedValue = new BigInteger(value);
		model = new SpinnerMathNumberModel(convertedValue, null, null, BigInteger.ONE);
	    }
	} else {
	    logger.error("Type STRING and BOOLEAN are not allowed for the use of this function.");
	    return null;
	}

	final JSpinner spinner = new JSpinner(model) {
	    @Override
	    public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		dim.width = 100;
		return dim;
	    }
	};
	spinner.addChangeListener(new ChangeListener() {

	    @Override
	    public void stateChanged(ChangeEvent e) {
		properties.setProperty(property, spinner.getModel().getValue().toString());

	    }
	});
	spinner.setEnabled(true);
	spinner.setEditor(new MathNumberEditor(spinner, (DecimalFormat) DecimalFormat.getInstance(spinner.getLocale())));
	addComponent(spinner);
	fieldLabels.put(spinner, label);
	itemIdx++;
	return spinner;
    }

    protected JPanel addFileEntry(@Nonnull String name, @Nullable String description, final @Nonnull String property,
	    @Nonnull String fileType, boolean requiredBeforeAction) {
	JPanel filePanel = new JPanel(new GridBagLayout());
	JLabel label = addLabel(name, description);

	String currentValue = properties.getProperty(property);

	final JTextField filePathField = new JTextField(){

	    // the following is necessary because most path are longer than the field and GridBagLayout does not care
	    // about MaximumSize. The following seems good for the default size of the configuration window but is does
	    // not scale good if the size of the window becomes bigger.
	    @Override
	    public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		dim.width = 100;
		return dim;
	    }
	};

	if (currentValue != null) {
	    filePathField.setText(currentValue);
	    filePathField.setToolTipText(currentValue);
	}

	filePathField.getDocument().addDocumentListener(new DocumentListener() {
	    @Override
	    public void insertUpdate(DocumentEvent e) {
		File file = new File(filePathField.getText());
		if (file.exists() && file.isFile()) {
		    properties.setProperty(property, filePathField.getText());
		}
	    }
	    @Override
	    public void removeUpdate(DocumentEvent e) {
		File file = new File(filePathField.getText());
		if ((file.exists() && file.isFile()) || filePathField.getText().equals("")) {
		    properties.setProperty(property, filePathField.getText());
		}
	    }
	    @Override
	    public void changedUpdate(DocumentEvent e) {
		// ignore
	    }
	});

	GridBagConstraints fieldConstraint = new GridBagConstraints();
	fieldConstraint.anchor = GridBagConstraints.WEST;
	fieldConstraint.fill = GridBagConstraints.HORIZONTAL;
	fieldConstraint.gridx = 0;
	fieldConstraint.gridy = 0;
	fieldConstraint.weightx = 2;
	filePanel.add(filePathField, fieldConstraint);

	JButton browseButton = new JButton(lang.translationForKey("addon.settings.browse"));
	browseButton.addActionListener(new OpenFileBrowserListener(fileType, filePathField));
	GridBagConstraints buttonConstraint = new GridBagConstraints();
	buttonConstraint.anchor = GridBagConstraints.EAST;
	buttonConstraint.fill = GridBagConstraints.NONE;
	buttonConstraint.gridx = 1;
	buttonConstraint.gridy = 0;
	buttonConstraint.weightx = 0;
	buttonConstraint.gridwidth = GridBagConstraints.REMAINDER;
	buttonConstraint.insets = new Insets(0, 5, 0, 0);
	filePanel.add(browseButton, buttonConstraint);

	addComponent(filePanel);
	itemIdx++;
	fieldLabels.put(filePanel, label);
	return filePanel;
    }

    public JPanel addFileListEntry(@Nonnull String name, @Nonnull String description, @Nonnull String property,
	    @Nonnull String fileType, boolean isRequired) {
	JLabel label = addListLabel(name, description);

	FileListEntryItem item = new FileListEntryItem(fileType, property, properties);

	GridBagConstraints constraints2 = new GridBagConstraints();
	constraints2.anchor = GridBagConstraints.NORTHWEST;
	constraints2.insets = new Insets(5, 3, 0, 0);
	constraints2.fill = GridBagConstraints.HORIZONTAL;
	constraints2.gridx = 2;
	constraints2.gridy = itemIdx;
	container.add(item, constraints2);

	itemIdx++;
	fieldLabels.put(item, label);

	return item;
    }

}
