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

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.openecard.addon.AddonPropertiesException;
import org.openecard.common.I18n;
import org.openecard.gui.graphics.GraphicsUtil;
import org.openecard.gui.graphics.OecLogoBgTransparent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Base implementation of an add-on panel.
 * {@code AddonPanel}s are used to represent add-ons on the {@link ManagementDialog}. This implementation is complete,
 * however it should be subclassed to reflect the needs of the add-on or builtin item.<br>
 * {@code AddonPanel}s are either arranged as tabs, or as a single content panel, depending on the use case.
 *
 * @author Tobias Wich
 */
public class AddonPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(AddonPanel.class);

    private final I18n lang = I18n.getTranslation("addon");

    private static final int LOGO_WIDTH = 45;
    private static final int LOGO_HEIGHT = 45;

    private Image logo;
    private SettingsPanel settingsPanel;

    private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder();


    /**
     * Creates an AddonPanel with the given panels.
     * Each of the panel types may be left out, but at least one must be present in order for the dialog to make any
     * sense. The panels are arranged in a tab pane.
     *
     * @param actionPanel Optional action panel of the add-on.
     * @param settingsPanel Optional settings panel of the add-on.
     * @param aboutPanel Optional about page panel of the add-on.
     * @param name Name of the add-on as displayed in the head of the panel.
     * @param description Optional description of the add-on as displayed in the head of the panel.
     * @param logo Optional logo of the add-on as displayed on the {@link ManagementDialog}.
     *   If not present a default will be used.
     */
    public AddonPanel(@Nullable ActionPanel actionPanel, @Nullable SettingsPanel settingsPanel,
	    @Nullable AboutPanel aboutPanel, @Nonnull String name, @Nullable String description, @Nullable Image logo) {
	setLayout(new BorderLayout(0, 0));

	this.logo = logo;
	JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	add(tabbedPane);
	Dimension dim = new Dimension(100,100);

	if (actionPanel != null) {
	    JScrollPane actionScrollPane = new JScrollPane(actionPanel);
	    actionScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    actionScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	    actionScrollPane.setMinimumSize(dim);
	    actionScrollPane.setPreferredSize(dim);
	    actionScrollPane.setBorder(EMPTY_BORDER);
	    tabbedPane.addTab(lang.translationForKey("addon.panel.tab.function"), null, actionScrollPane, null);
	}
	if (settingsPanel != null) {
	    if (settingsPanel instanceof SettingsPanel) {
		this.settingsPanel = settingsPanel;
	    }
	    tabbedPane.addTab(lang.translationForKey("addon.panel.tab.settings"), null, settingsPanel, null);
	}
	if (aboutPanel != null) {
	    tabbedPane.addTab(lang.translationForKey("addon.panel.tab.about"), null, aboutPanel, null);
	}

	createHeader(name, description);
    }

    /**
     * Creates an AddonPanel with the given panel.
     * The panels is placed directly on this panel.
     *
     * @param singlePanel Panel to display. This panel may be of any type.
     * @param name Name of the add-on as displayed in the head of the panel.
     * @param description Optional description of the add-on as displayed in the head of the panel.
     * @param logo Optional logo of the add-on as displayed on the {@link ManagementDialog}.
     *   If not present a default will be used.
     */
    public AddonPanel(@Nonnull JPanel singlePanel, @Nonnull String name, @Nullable String description,
	    @Nullable Image logo) {
	setLayout(new BorderLayout(0, 0));
	this.logo = logo;
	createHeader(name, description);

	JComponent panel;
	if (!(singlePanel instanceof AboutPanel) && !(singlePanel instanceof SettingsPanel)) {
	    JScrollPane singleScrollPane = new JScrollPane(singlePanel);
	    singleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    singleScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	    Dimension dim = new Dimension(this.getWidth(), this.getHeight() - 75);
	    singleScrollPane.setMinimumSize(dim);
	    singleScrollPane.setPreferredSize(dim);
	    singleScrollPane.setBorder(EMPTY_BORDER);
	    panel = singleScrollPane;
	} else {
	    panel = singlePanel;
	}

	if (singlePanel instanceof SettingsPanel) {
	    this.settingsPanel = (SettingsPanel) singlePanel;
	}

	add(panel);
    }

    /**
     * Saves the properties of the settings panel if one is present.
     */
    public void saveProperties() {
	try {
	    if (settingsPanel != null) {
		settingsPanel.saveProperties();
	    }
	} catch (IOException ex) {
	    logger.error("Failed to save settings.", ex);
	} catch (SecurityException ex) {
	    logger.error("Missing permissions to save settings.", ex);
	} catch (AddonPropertiesException ex) {
	    logger.error("Failed to save addon settings.", ex);
	}
    }

    /**
     * Gets the logo of the add-on.
     * If no logo has been defined, a default logo is returned.
     *
     * @return The logo of the add-on.
     */
    @Nonnull
    public Image getLogo() {
	Image result;
	if (logo == null) {
	    result = getDefaultLogo();
	} else {
	    result = logo.getScaledInstance(LOGO_WIDTH, LOGO_HEIGHT, Image.SCALE_SMOOTH);
	}
	return result;
    }

    private void createHeader(@Nonnull String name, @Nullable String description) {
	Box panel = Box.createVerticalBox();
	add(panel, BorderLayout.NORTH);

	Box content = Box.createVerticalBox();
	content.setAlignmentX(Component.LEFT_ALIGNMENT);
	content.setBorder(new EmptyBorder(5, 20, 0, 0));
	content.setMinimumSize(new Dimension(50, 50));
	content.setPreferredSize(new Dimension(50, 50));
	panel.add(content);

	JLabel nameLabel = new JLabel(name);
	nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
	nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 19));
	nameLabel.setForeground(Color.getHSBColor(0, 0, 0.25f));
	content.add(nameLabel);

	if (description != null && ! description.isEmpty()) {
	    content.add(Box.createVerticalStrut(5));
	    JLabel descLabel = new JLabel(description);
	    descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
	    descLabel.setFont(descLabel.getFont().deriveFont(Font.PLAIN));
	    descLabel.setForeground(Color.getHSBColor(0, 0, 0.25f));
	    content.add(descLabel);
	}

	panel.add(Box.createVerticalStrut(4));

	JSeparator rule = new JSeparator(SwingConstants.HORIZONTAL);
	rule.setBorder(new EmptyBorder(10, 20, 10, 20));
	rule.setAlignmentX(Component.LEFT_ALIGNMENT);
	panel.add(rule);

	panel.add(Box.createVerticalStrut(5));
    }

    private Image getDefaultLogo() {
	Image original = GraphicsUtil.createImage(OecLogoBgTransparent.class, LOGO_WIDTH, LOGO_HEIGHT);
	BufferedImage result = new BufferedImage(LOGO_WIDTH, LOGO_HEIGHT, BufferedImage.TYPE_INT_ARGB);
	Graphics2D g = result.createGraphics();

	// draw original image with grey shade
	g.setComposite(AlphaComposite.SrcOver.derive(0.3f));
	g.drawImage(original, 0, 0, null);

	g.dispose();
	return result;
    }

}
