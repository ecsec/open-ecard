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
import java.io.File;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openecard.common.I18n;
import org.openecard.gui.swing.common.GUIDefaults;
import org.openecard.richclient.gui.manage.Settings;


/**
 * Implements a dynamic gui element which is able to remove and add a file entry.
 *
 * @author Hans-Martin Haase
 */
public class FileListEntryItem extends JPanel {

    /**
     * Icon for the minus button which indicates the removal of an entry.
     */
    private final static Icon openedIndicator = GUIDefaults.getImage("ToggleText.selectedIcon");

    /**
     * Icon for the plus button which indicates the addition of a new empty row.
     */
    private final static Icon closedIndicator = GUIDefaults.getImage("ToggleText.icon");


    /**
     * I18n object which is necessary to retrieve translations.
     */
    private final I18n lang = I18n.getTranslation("addon");

    /**
     * A list which contains all text entries .
     */
    private final ArrayList<String> entries = new ArrayList<>();

    /**
     * A list of JButtons.
     * The order of the buttons corresponds to the order in the {@code entries} list.
     */
    private final ArrayList<JButton> entryPlusBox = new ArrayList<>();

    /**
     * A list of JTextFields.
     * The order of text fields corresponds to the order in the {@code entries} list.
     */
    private final ArrayList<JTextField> textEntries = new ArrayList<>();

    /**
     * A semicolon separated list of accepted file types.
     */
    private final String fileType;

    /**
     * The name of the property which relates to this configuration entry.
     */
    private final String property;

    /**
     * A Settings object which is used to retrieve and set the current value of the configuration entry.
     */
    private final Settings properties;


    /**
     * The current Y coordinate in the used GridBagLayout.
     */
    private int currentYCoordinate = 0;

    /**
     * Creates a new FileListEntryItem object.
     *
     * @param fileType A semicolon separated list of accepted file types.
     * @param property The name of the property which relates to this configuration entry.
     * @param properties A Settings object which is used to retrieve and set the current value of the configuration entry.
     */
    public FileListEntryItem(String fileType, String property, Settings properties) {
	setLayout(new GridBagLayout());
	this.fileType = fileType;
	this.properties = properties;
	this.property = property;
	String props = properties.getProperty(property);
	if (props != null && ! props.equals("")) {
	    for (String entry : props.split(";")) {
		entries.add(entry);
		addRow(entry);
	    }
	} else {
	    addEmptyEntry();
	}
    }

    /**
     * Adds an new empty entry to the list of entries.
     */
    private void addEmptyEntry() {
	addRow(null);
    }

    /**
     * Rebuilds the complete list of entries.
     *
     * This method have to be called if the remove button was pressed. The GridBagLayout is not able to remove rows in
     * a dynamic way only dynamic adding of rows is possible. So we have to clear the whole panel and rebuild the list
     * of entries.
     */
    private void rebuild() {
	removeAll();
	textEntries.clear();
	entryPlusBox.clear();
	if (! entries.isEmpty()) {
	    for (String entry : entries) {
		addRow(entry);
	    }
	} else {
	    addEmptyEntry();
	}
	revalidate();
	repaint();
    }

    /**
     * Adds an additional row to the layout.
     *
     * The added row contains an TextField with the given {@code entry}. If {@code entry} is NULL an empty TextField is
     * add which is used to add a completely new list entry.
     *
     * @param entry The entry to add to the list.
     */
    private void addRow(String entry) {
	JTextField textField = new JTextField() {
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
	textField.setText(entry);
	textField.setToolTipText(textField.getText());
	textField.getDocument().addDocumentListener(new DocumentChangeListener(textField));
	textEntries.add(textField);

	JButton browseButton = new JButton(lang.translationForKey("addon.settings.browse"));
	browseButton.addActionListener(new OpenFileBrowserListener(fileType, textField));

	// hide plus button of the previous line
	if (!entryPlusBox.isEmpty()) {
	    JButton prevPlus = entryPlusBox.get(entryPlusBox.size() - 1);
	    prevPlus.setVisible(false);
	}

	JButton plus = new JButton();
	plus.setIcon(closedIndicator);
	plus.addActionListener(new AddActionListener(plus));
	plus.setOpaque(true);
	plus.setFocusPainted(false);
	plus.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
	plus.setHorizontalAlignment(SwingConstants.LEFT);
	plus.setMargin(new Insets(0, 0, 0, 0));
	plus.setBounds(0, 0, 0, 0);
	plus.setContentAreaFilled(false);
	plus.setHorizontalTextPosition(SwingConstants.TRAILING);
	entryPlusBox.add(plus);

	JButton minus = new JButton();
	minus.setIcon(openedIndicator);
	minus.addActionListener(new RemoveActionListener(minus));
	// necessary to determine which entry to remove from the list.
	minus.setName(entry);
	minus.setOpaque(true);
	minus.setFocusPainted(false);
	minus.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
	minus.setHorizontalAlignment(SwingConstants.LEFT);
	minus.setMargin(new Insets(0, 0, 0, 0));
	minus.setBounds(0, 0, 0, 0);
	minus.setContentAreaFilled(false);
	minus.setHorizontalTextPosition(SwingConstants.TRAILING);

	GridBagConstraints textConstraint = new GridBagConstraints();
	textConstraint.anchor = GridBagConstraints.WEST;
	textConstraint.fill = GridBagConstraints.HORIZONTAL;
	textConstraint.gridx = 0;
	textConstraint.gridy = currentYCoordinate;
	textConstraint.weightx = 4;

	GridBagConstraints plusConstraint = new GridBagConstraints();
	plusConstraint.anchor = GridBagConstraints.WEST;
	plusConstraint.fill = GridBagConstraints.NONE;
	plusConstraint.gridx = 3;
	plusConstraint.gridy = currentYCoordinate;
	plusConstraint.gridwidth = GridBagConstraints.REMAINDER;
	plusConstraint.insets = new Insets(0, 5, 0, 5);

	GridBagConstraints minusConstraint = new GridBagConstraints();
	minusConstraint.anchor = GridBagConstraints.WEST;
	minusConstraint.fill = GridBagConstraints.NONE;
	minusConstraint.gridx = 2;
	minusConstraint.gridy = currentYCoordinate;
	minusConstraint.insets = new Insets(0, 5, 0, 5);

	GridBagConstraints browseConstrain = new GridBagConstraints();
	browseConstrain.anchor = GridBagConstraints.WEST;
	browseConstrain.fill = GridBagConstraints.NONE;
	browseConstrain.gridx = 1;
	browseConstrain.gridy = currentYCoordinate;
	browseConstrain.insets = new Insets(5, 5, 0, 5);

	add(textField, textConstraint);
	add(browseButton, browseConstrain);
	add(plus, plusConstraint);
	add(minus, minusConstraint);
	currentYCoordinate++;
    }

    /**
     *	An ActionListener implementation which triggers the addition of a new empty list entry.
     */
    private class AddActionListener implements ActionListener {

	/**
	 * The button which is associated with this listener.
	 */
	private final JButton button;

	/**
	 * Creates a new AddActionListener object.
	 *
	 * @param addButton The button which is associated with this listener.
	 */
	private AddActionListener(JButton addButton) {
	    button = addButton;
	}

	/**
	 * Make the clicked plus button invisible an invoke the creation of a new empty entry.
	 *
	 * @param e
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
	    if (! textEntries.get(textEntries.size() - 1).getText().equals("")) {
		button.setVisible(false);
		addEmptyEntry();
	    }
	}
    }

    /**
     * An ActionListener implementation which triggers the removal of a entry in the list.
     */
    private class RemoveActionListener implements ActionListener {

	/**
	 * The button which is associated with this Listener.
	 */
	private final JButton button;

	/**
	 * Creates a new RemoveActionListener object.
	 *
	 * @param removeButton The button which is associated with this Listener.
	 */
	private RemoveActionListener(JButton removeButton) {
	    this.button = removeButton;
	}

	/**
	 * Triggers the removal of the selected entry.
	 *
	 * @param e
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
	    String name = button.getName();
	    int indexOfEntry = entries.indexOf(name);
	    if (indexOfEntry >= 0) {
		entryPlusBox.remove(indexOfEntry);
		JTextField field = textEntries.get(indexOfEntry);
		field.setText("");
		textEntries.remove(indexOfEntry);
		entries.remove(name);
	    }
	    rebuild();
	}
    }

    /**
     * Implements a DocumentListener which checks the correctness of the entered content.
     */
    private class DocumentChangeListener implements DocumentListener {

	/**
	 * The TextField which is associated with this Listener.
	 */
	private final JTextField textField;

	private String currentText;

	/**
	 * Creates a new DocumentChangeListener object.
	 *
	 * @param field The {@link JTextField which is associated with this Listener.
	 */
	private DocumentChangeListener(JTextField field) {
	    textField = field;
	    currentText = field.getText();
	}

	/**
	 *
	 * @param e
	 */
	@Override
	public void insertUpdate(DocumentEvent e) {
	    concatContentsAndSetProperty();
	    int entryIndex = entries.indexOf(currentText);
	    // update existing entry
	    if (entryIndex >= 0) {
		entryPlusBox.get(entryIndex).setName(textField.getText());
		entries.set(entryIndex, textField.getText());
	    } else {
		entries.add(textField.getText());
	    }
	    currentText = textField.getText();
	}

	/**
	 *
	 * @param e
	 */
	@Override
	public void removeUpdate(DocumentEvent e) {
	    concatContentsAndSetProperty();
	}

	/**
	 * Not implemented in this implementation so an changed update event is ignored.
	 *
	 * @param e
	 */
	@Override
	public void changedUpdate(DocumentEvent e) {
	    // ignore
	}

	/**
	 * Concatenates the content of all filled text fields.
	 *
	 * The different contents a separated by a semicolon and the resulting string is set as the new value of the
	 * {@code property} property.
	 */
	private void concatContentsAndSetProperty() {
	    StringBuilder sb = new StringBuilder();
	    for (JTextField text : textEntries) {
		File file = new File(text.getText());
		if (file.exists() && file.isFile()) {
		    sb.append(text.getText());
		    sb.append(";");
		}
	    }

	    properties.setProperty(property, sb.toString());
	}

    }
}
