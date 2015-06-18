/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

package org.openecard.gui.swing.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.openecard.common.I18n;
import org.openecard.common.util.FileUtils;
import org.openecard.gui.definition.Document;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.swing.common.GUIDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 */
public class ToggleText implements StepComponent {

    private static final Logger logger = LoggerFactory.getLogger(ToggleText.class);
    private static final I18n lang = I18n.getTranslation("swing");

    private final static String TOGGLETEXT = "ToggleText";
    private final static String TOGGLETEXT_FOREGROUND = TOGGLETEXT + ".foreground";
    private final static String TOGGLETEXT_BACKGROUND = TOGGLETEXT + ".background";
    private final static String TOGGLETEXT_INDICATOR_FOREGROUND = TOGGLETEXT + "Indicator.foreground";
    private final static Icon openedIndicator = GUIDefaults.getImage("ToggleText.selectedIcon");
    private final static Icon closedIndicator = GUIDefaults.getImage("ToggleText.icon");

    private JPanel rootPanel;
    private JButton button;
    private JComponent text;
    private File tmpDir;

    /**
     * Creates a new ToggleText.
     *
     * @param toggleText
     */
    public ToggleText(org.openecard.gui.definition.ToggleText toggleText) {
	this(toggleText.getTitle(), toggleText.getDocument(), toggleText.isCollapsed());
    }

    /**
     * Creates a new ToggleText.
     *
     * @param buttonText Text of the button
     * @param contentText Text of the content
     */
    public ToggleText(String buttonText, String contentText) {
	this(buttonText, new Document("text/plain", contentText.getBytes(Charset.forName("UTF-8"))), false);
    }

    /**
     * Creates a new ToggleText instance.
     *
     * @param buttonText Text of the button
     * @param contentText The test to display if collapsed = false
     * @param collapsed Indicates whether the {@code contentText} is displayed or not.
     */
    public ToggleText(String buttonText, String contentText, boolean collapsed) {
	this(buttonText, new Document("text/plain", contentText.getBytes(Charset.forName("UTF-8"))), collapsed);
    }

    /**
     * Creates a new ToggleText.
     *
     * @param buttonText Text of the button
     * @param content {@link Document} representing the content of the instance.
     * @param collapsed Collapsed (content is visible or not)
     */
    public ToggleText(String buttonText, Document content, boolean collapsed) {
	initComponents(buttonText, content);
	initLayout();
	loadUIDefaults();

	button.setSelected(collapsed);
	text.setVisible(!collapsed);
	button.setIcon(!collapsed ? openedIndicator : closedIndicator);
    }

    /**
     * Initializes the components of the panel.
     *
     * @param buttonText Text of the button
     * @param content Document representing the content.
     */
    private void initComponents(String buttonText, Document content) {
	rootPanel = new JPanel();
	button = new JButton(buttonText + "  ");
	String mimeType = content.getMimeType();
	switch (mimeType) {
	    case "text/html":
		ClassLoader loader = ToggleText.class.getClassLoader();
		try {
		    loader.loadClass("javafx.embed.swing.JFXPanel");
		    text = createJfxPanel(mimeType, content.getValue());
		} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException |
			IllegalStateException ex) {
		    logger.error("Failed to initialize JFXPanel", ex);
		    createJTextArea(new String(content.getValue(), Charset.forName("UTF-8")));
		}
		break;
	    case "text/plain":
		createJTextArea(new String(content.getValue(), Charset.forName("UTF-8")));
		break;
	    case "application/pdf":
		try {
		    createTmpDir();
		    String pdfFile = createTmpPdf(content.getValue());
		    createStartPdfViewButton(pdfFile);
		} catch (FileNotFoundException | SecurityException ex) {
		    logger.error("Failed to access the tmp pdf file.", ex);
		    createJTextArea(lang.translationForKey("pdf.creation.failed"));
		} catch (IOException ex) {
		    logger.error("Failed to create the tmp pdf file.", ex);
		    createJTextArea(lang.translationForKey("pdf.creation.failed"));
		}
		break;
	    default:
		logger.warn("Unsupported usage of content of type " + mimeType + " in " + ToggleText.class.getName());
		createJTextArea(lang.translationForKey("unsupported.mimetype", mimeType));
	}

	button.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		text.setVisible(!text.isVisible());
		button.setIcon(text.isVisible() ? openedIndicator : closedIndicator);
		rootPanel.revalidate();
		rootPanel.doLayout();
		rootPanel.repaint();
	    }
	});
    }

    /**
     * Initializes the layout of the panel.
     */
    private void initLayout() {
	rootPanel.setLayout(new GridBagLayout());

	GridBagConstraints gbc = new GridBagConstraints();

	// Add elements
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbc.weightx = 1.0;
	rootPanel.add(button, gbc);

	gbc.gridx = 0;
	gbc.gridy = 1;
	rootPanel.add(text, gbc);
    }

    private void loadUIDefaults() {
	UIDefaults defaults = UIManager.getDefaults();

	Color bg = (Color) defaults.get(TOGGLETEXT_BACKGROUND);
	if (bg == null) {
	    bg = Color.WHITE;
	}
	Color fg = (Color) defaults.get(TOGGLETEXT_FOREGROUND);
	if (fg == null) {
	    fg = Color.BLACK;
	}
	Color fgIndicator = (Color) defaults.get(TOGGLETEXT_INDICATOR_FOREGROUND);
	if (fgIndicator == null) {
	    fgIndicator = Color.LIGHT_GRAY;
	}
	Font font = UIManager.getFont("Label.font");

	button.setOpaque(true);
	button.setFocusPainted(false);
	button.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
	button.setHorizontalAlignment(SwingConstants.LEFT);
	button.setMargin(new Insets(0, 0, 0, 0));
	button.setBounds(0, 0, 0, 0);
	button.setFont(font.deriveFont(Font.BOLD));
	button.setContentAreaFilled(false);
	button.setHorizontalTextPosition(SwingConstants.TRAILING);

	text.setFont(font.deriveFont(Font.PLAIN));

	rootPanel.setBackground(bg);
	rootPanel.setForeground(fg);

	for (int i = 0; i < rootPanel.getComponentCount(); i++) {
	    rootPanel.getComponent(i).setBackground(bg);
	    rootPanel.getComponent(i).setForeground(fg);
	}
    }

    @Override
    public Component getComponent() {
	return rootPanel;
    }

    @Override
    public boolean isValueType() {
	return false;
    }

    @Override
    public boolean validate() {
	return true;
    }

    @Override
    public OutputInfoUnit getValue() {
	return null;
    }

    /**
     * Creates a JPanel containing a JFXPanel by reflection.
     *
     * @param mimeType The MimeType of the content to display. The currently supported MimeTypes are {@code text/html}
     * and {@code application/pdf}.
     * @param content The content which shall be displayed in the JFXPanel.
     * @return A JPanel containing a JFXPanel which displays html oder pdf content.
     * @throws ClassNotFoundException If the underlying HTMLPanel class is not found but this class is always in the jar
     * so this should never happen.
     * @throws IllegalAccessException Should also never happen the createPanel method is public.
     * @throws IllegalArgumentException Should also never happen.
     * @throws InvocationTargetException Should also never happen.
     */
    private JPanel createJfxPanel(String mimeType, byte[] content) throws ClassNotFoundException, IllegalAccessException,
	    IllegalArgumentException, InvocationTargetException {
	ClassLoader loader = ToggleText.class.getClassLoader();
	Class<?> htmlPanelClass = loader.loadClass("org.openecard.gui.swing.components.HTMLPanel");
	for (Method m : htmlPanelClass.getMethods()) {
	    if (m.getName().equals("createPanel")) {
		return (JPanel) m.invoke(null, mimeType, content);
	    }
	}

	throw new IllegalStateException("The required method createPanel was not found.");
    }

    /**
     * Creates a JTextArea containing the given content.
     *
     * @param content The content to display in the JTextArea.
     */
    private void createJTextArea(String content) {
	text = new JTextArea(content);
	JTextArea textAreaObject = (JTextArea) text;
	textAreaObject.setMargin(new Insets(0, 13, 0, 0));
	textAreaObject.setEditable(false);
	textAreaObject.setLineWrap(true);
	textAreaObject.setWrapStyleWord(true);
    }

    /**
     * Creates the temp directory for the PDF, HTML and Java Script files.
     * <br>
     * <br>
     * Note: The directory is deleted as soon as the jvm terminates.
     *
     * @throws IOException
     * @throws SecurityException
     */
    private void createTmpDir() throws IOException, SecurityException {
	String tmpDirPath = FileUtils.getHomeConfigDir().getAbsolutePath() + "/tmp";
	tmpDir = new File(tmpDirPath);
	tmpDir.mkdirs();
	tmpDir.deleteOnExit();
    }

    /**
     * Creates a tmp PDF file containing the given content.
     * <br>
     * <br>
     * Note: The file is deleted as soon as the JVM terminates.
     *
     * @param content The content which shall be written to the PDF file.
     * @return The path to the PDF file.
     * @throws IOException
     * @throws FileNotFoundException
     */
    private String createTmpPdf(byte[] content) throws IOException, FileNotFoundException {
	File tmpPdf = File.createTempFile("tmp", ".pdf", tmpDir);
	tmpPdf.deleteOnExit();
	try (FileOutputStream out = new FileOutputStream(tmpPdf)) {
	    out.write(content);
	    out.flush();
	}
	return tmpPdf.getAbsolutePath();
    }

    private void createStartPdfViewButton(final String pdfFile) {
	JPanel contentPane = new JPanel();
	// TODO translate
	final JButton pdfButton = new JButton(lang.translationForKey("open.pdf.in.external.viewer"));
	pdfButton.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		if (e.getSource() == pdfButton) {
		    try {
			Desktop desktop = Desktop.getDesktop();
			desktop.open(new File(pdfFile));
		    } catch (IOException ex) {
			logger.error("Failed to open pdf file.", ex);
		    }
		}
	    }

	});
	contentPane.add(pdfButton);
	text = contentPane;
    }

}
