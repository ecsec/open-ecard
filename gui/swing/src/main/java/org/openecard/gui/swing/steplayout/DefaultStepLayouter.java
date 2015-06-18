/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.gui.swing.steplayout;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import org.openecard.gui.definition.InputInfoUnit;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Radiobox;
import org.openecard.gui.definition.TextField;
import org.openecard.gui.swing.ScrollPanel;
import org.openecard.gui.swing.components.AbstractInput;
import org.openecard.gui.swing.components.Checkbox;
import org.openecard.gui.swing.components.Hyperlink;
import org.openecard.gui.swing.components.ImageBox;
import org.openecard.gui.swing.components.Radiobutton;
import org.openecard.gui.swing.components.StepComponent;
import org.openecard.gui.swing.components.Text;
import org.openecard.gui.swing.components.ToggleText;


/**
 * Default layouter for the Swing GUI.
 * This layouter provides a decent look and feel for most user consent tasks. If you need a specialised version,
 * create another layouter and register it for the respective URI.
 *
 * @author Tobias Wich
 * @author Florian Feldmann
 */
public class DefaultStepLayouter extends StepLayouter {

    private final ArrayList<StepComponent> components;
    private final JPanel rootPanel;

    protected DefaultStepLayouter(List<InputInfoUnit> infoUnits, String stepName) {
	components = new ArrayList<StepComponent>(infoUnits.size());
	rootPanel = new JPanel(new BorderLayout());

	// Add a panel containing step title and separator
	JPanel pageStart = new JPanel(new BorderLayout());
	JLabel title = new JLabel("<html><h3>" + stepName + "</h3></html>");
	// add a space of 20 on top and 3 below to match with the logo separator
	title.setBorder(new EmptyBorder(20, 0, 3, 0));
	pageStart.add(title, BorderLayout.PAGE_START);
	JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
	pageStart.add(sep, BorderLayout.CENTER);
	// add a space of 15 before the actual step content
	pageStart.setBorder(new EmptyBorder(0, 0, 15, 0));
	rootPanel.add(pageStart, BorderLayout.PAGE_START);

	final ScrollPanel contentPanel = new ScrollPanel();
	contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
	contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

	// Create content
	for (InputInfoUnit next : infoUnits) {
	    StepComponent nextComponent = null;

	    switch (next.type()) {
		case CHECK_BOX:
		    nextComponent = new Checkbox((org.openecard.gui.definition.Checkbox) next);
		    break;
		case HYPERLINK:
		    nextComponent = new Hyperlink((org.openecard.gui.definition.Hyperlink) next);
		    break;
		case IMAGE_BOX:
		    nextComponent = new ImageBox((org.openecard.gui.definition.ImageBox) next);
		    break;
		case PASSWORD_FIELD:
		    nextComponent = new AbstractInput((PasswordField) next);
		    break;
		case RADIO_BOX:
		    nextComponent = new Radiobutton((Radiobox) next);
		    break;
		case SIGNAUTRE_FIELD:
		    throw new UnsupportedOperationException("Not implemented yet.");
		case TEXT:
		    nextComponent = new Text((org.openecard.gui.definition.Text) next);
		    break;
		case TEXT_FIELD:
		    nextComponent = new AbstractInput((TextField) next);
		    break;
		case TOGGLE_TEXT:
		    nextComponent = new ToggleText((org.openecard.gui.definition.ToggleText) next);
		    break;
	    }
	    if (nextComponent != null) {
		components.add(nextComponent);
		contentPanel.add(nextComponent.getComponent());
		contentPanel.add(Box.createRigidArea(new Dimension(0, 6)));
	    }
	}

	JScrollPane scrollPane = new JScrollPane(contentPanel);
	scrollPane.setBorder(BorderFactory.createEmptyBorder());
	scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	rootPanel.add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public List<StepComponent> getComponents() {
	return components;
    }

    @Override
    public Container getPanel() {
	return rootPanel;
    }

}
