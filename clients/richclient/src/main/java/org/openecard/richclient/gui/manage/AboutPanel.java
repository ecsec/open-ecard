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

package org.openecard.richclient.gui.manage;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.common.I18n;


/**
 * Implements a about panel which contains basic information about the installed addon.
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class AboutPanel extends JPanel {

    private static final String LANGUAGE_CODE = System.getProperty("user.language");
    private final I18n lang = I18n.getTranslation("addon");
    private final AddonSpecification addonSpec;
    private final GridBagLayout layout = new GridBagLayout();
    private final String license;
    private final String about;
    private JEditorPane display;

    /**
     * Creates an new AboutPanel instance.
     *
     * @param addonSpecification The addon manifest content which is the information source.
     */
    public AboutPanel(AddonSpecification addonSpecification) {
	this.setLayout(layout);
	this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	addonSpec = addonSpecification;
	license = addonSpecification.getLicenseText(LANGUAGE_CODE);
	about = addonSpecification.getAbout(LANGUAGE_CODE);
	setupHead();
	setupBody();
	setupFooter();
    }

    /**
     * Setup the header which contains basic information about license and the version.
     */
    private void setupHead() {
	JPanel basePane = new JPanel(new GridBagLayout());
	basePane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
	basePane.setLayout(new GridLayout(2,1));
	JLabel versionLabel = new JLabel(lang.translationForKey("addon.about.version") + ":");
	JLabel licenseLabel = new JLabel(lang.translationForKey("addon.about.license.type") + ":");

	GridBagConstraints lc = new GridBagConstraints();
	lc.anchor = GridBagConstraints.WEST;
	lc.gridx = 0;
	lc.gridy = 0;
	lc.weightx = 1.0;
	lc.weighty = 1.0;
	basePane.add(versionLabel, lc);

	GridBagConstraints lc2 = new GridBagConstraints();
	lc2.anchor = GridBagConstraints.WEST;
	lc2.fill = GridBagConstraints.HORIZONTAL;
	lc2.gridwidth = GridBagConstraints.REMAINDER;
	lc2.gridx = 1;
	lc2.gridy = 0;
	lc2.weightx = 1.0;
	lc2.weighty = 1.0;
	basePane.add(new JLabel(addonSpec.getVersion()));

	GridBagConstraints lc3 = new GridBagConstraints();
	lc3.anchor = GridBagConstraints.WEST;
	lc3.gridx = 1;
	lc3.gridy = 2;
	lc3.weightx = 1.0;
	lc3.weighty = 1.0;
	basePane.add(licenseLabel, lc3);

	GridBagConstraints lc4 = new GridBagConstraints();
	lc4.anchor = GridBagConstraints.WEST;
	lc4.fill = GridBagConstraints.HORIZONTAL;
	lc4.gridwidth = GridBagConstraints.REMAINDER;
	lc4.gridx = 1;
	lc4.gridy = 0;
	lc4.weightx = 1.0;
	lc4.weighty = 1.0;
	basePane.add(new JLabel(addonSpec.getLicense()));


	GridBagConstraints c = new GridBagConstraints();
	c.fill = GridBagConstraints.HORIZONTAL;
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.weightx = 1.0;
	c.weighty = 1.0;
	c.anchor = GridBagConstraints.NORTH;
	layout.setConstraints(basePane, c);
	add(basePane);
    }

    /**
     * Setup the body which contains the License Text and About text.
     */
    private void setupBody() {
	display = new JEditorPane();
	display.setContentType("text/html");
	display.setText(about);
	display.setEditable(false);

	if (about.equals("")) {
	    display.setText("No about text available.");
	}

	JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.LEADING));
	buttonPane.setBorder(BorderFactory.createEmptyBorder());

	if (! license.equals("") && ! about.equals("")) {
	    JRadioButton aButton = new JRadioButton(lang.translationForKey("addon.about.about"));
	    aButton.addItemListener(action);
	    aButton.setSelected(true);
	    JRadioButton lButton = new JRadioButton(lang.translationForKey("addon.about.license"));

	    ButtonGroup btnGrp = new ButtonGroup();
	    btnGrp.add(aButton);
	    btnGrp.add(lButton);

	    buttonPane.add(aButton);
	    buttonPane.add(lButton);
	} else if (! license.equals("") && about.equals("")) {
	    JLabel licenseLabel2 = new JLabel(lang.translationForKey("addon.about.license"));
	    buttonPane.add(licenseLabel2);
	    display.setText(license);
	} else if (license.equals("") && ! about.equals("")) {
	    JLabel aboutLabel = new JLabel(lang.translationForKey("addon.about.about"));
	    buttonPane.add(aboutLabel);
	}

	GridBagLayout scrollLayout = new GridBagLayout();
	JPanel panel = new JPanel(scrollLayout);
	JScrollPane aboutScroll = new JScrollPane(display);
	aboutScroll.setBorder(BorderFactory.createEmptyBorder());
	aboutScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	aboutScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	aboutScroll.setMinimumSize(new Dimension(150, 100));
	aboutScroll.getVerticalScrollBar().setBlockIncrement(16);
	aboutScroll.getVerticalScrollBar().setUnitIncrement(16);
	display.setMinimumSize(new Dimension(150, 100));

	GridBagConstraints c = new GridBagConstraints();
	c.fill = GridBagConstraints.BOTH;
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.gridheight = 2;
	c.weightx = 1.0;
	c.weighty = 10.0;
	c.anchor = GridBagConstraints.NORTH;
	layout.setConstraints(panel, c);

	GridBagConstraints c2 = new GridBagConstraints();
	c2.anchor = GridBagConstraints.WEST;
	c2.fill = GridBagConstraints.NONE;
	c2.gridwidth  = GridBagConstraints.REMAINDER;
	c2.gridheight = 1;
	c2.weightx = 1.0;
	c2.weighty = 1.0;
	scrollLayout.setConstraints(buttonPane, c2);
	panel.add(buttonPane);

	GridBagConstraints c3 = new GridBagConstraints();
	c3.anchor = GridBagConstraints.CENTER;
	c3.fill = GridBagConstraints.BOTH;
	c3.gridwidth = GridBagConstraints.REMAINDER;
	c3.gridheight = 2;
	c3.weightx = 1;
	c3.weighty = 5;
	scrollLayout.setConstraints(aboutScroll, c3);
	panel.add(aboutScroll);

	add(panel);
    }

    /**
     * Setup the footer which contains only the uninstall button.
     */
    private void setupFooter() {
	JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
	JButton uninstallButton = new JButton(lang.translationForKey("addon.about.uninstall"));
	panel.add(uninstallButton);

	GridBagConstraints c = new GridBagConstraints();
	c.fill = GridBagConstraints.HORIZONTAL;
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.weightx = 1.0;
	c.weighty = 1.0;
	c.anchor = GridBagConstraints.SOUTH;
	layout.setConstraints(panel, c);
	add(panel);
    }

    /**
     * ItemListener implementation which switches the text contained in the JEditorPane.
     */
    private final ItemListener action = new ItemListener() {

	@Override
	public void itemStateChanged(ItemEvent e) {
	    if (e.getStateChange() == ItemEvent.DESELECTED) {
		display.setText(license);
	    } else if (e.getStateChange() == ItemEvent.SELECTED) {
		display.setText(about);
	    }
	    display.setCaretPosition(0);
	}
    };

}
