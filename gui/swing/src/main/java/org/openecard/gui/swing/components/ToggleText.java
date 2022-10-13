/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
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

    private static final Logger LOG = LoggerFactory.getLogger(ToggleText.class);
    private static final I18n LANG = I18n.getTranslation("swing");

    private final static String TOGGLETEXT = "ToggleText";
    private final static String TOGGLETEXT_FOREGROUND = TOGGLETEXT + ".foreground";
    private final static String TOGGLETEXT_BACKGROUND = TOGGLETEXT + ".background";
    private final static String TOGGLETEXT_INDICATOR_FOREGROUND = TOGGLETEXT + "Indicator.foreground";
    private final static Icon OPENED_INDICATOR = GUIDefaults.getImage("ToggleText.selectedIcon");
    private final static Icon CLOSED_INDOCATOR = GUIDefaults.getImage("ToggleText.icon");

    private JPanel rootPanel;
    private JButton button;
    private Component text;
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
	button.setIcon(!collapsed ? OPENED_INDICATOR : CLOSED_INDOCATOR);
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
		createHtmlPane(content.getValue());
		break;
	    case "text/plain":
		createJTextArea(new String(content.getValue(), Charset.forName("UTF-8")));
		break;
	    case "application/pdf":
		createPdfComponent(content.getValue());
//		try {
//		    createTmpDir();
//		    String pdfFile = createTmpPdf(content.getValue());
//		    createStartPdfViewButton(pdfFile);
//		} catch (FileNotFoundException | SecurityException ex) {
//		    LOG.error("Failed to access the tmp pdf file.", ex);
//		    createJTextArea(LANG.translationForKey("pdf.creation.failed"));
//		} catch (IOException ex) {
//		    LOG.error("Failed to create the tmp pdf file.", ex);
//		    createJTextArea(LANG.translationForKey("pdf.creation.failed"));
//		}
		break;
	    default:
		LOG.warn("Unsupported usage of content of type {} in {}", mimeType, ToggleText.class);
		createJTextArea(LANG.translationForKey("unsupported.mimetype", mimeType));
	}

	button.addActionListener((ActionEvent e) -> {
	    text.setVisible(!text.isVisible());
	    button.setIcon(text.isVisible() ? OPENED_INDICATOR : CLOSED_INDOCATOR);
	    rootPanel.revalidate();
	    rootPanel.doLayout();
	    rootPanel.repaint();
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
     * Creates a JTextArea containing the given content.
     *
     * @param content The content to display in the JTextArea.
     */
    private void createJTextArea(String content) {
	JTextArea textAreaObject = new JTextArea(content);
	textAreaObject.setMargin(new Insets(0, 13, 0, 0));
	textAreaObject.setEditable(false);
	textAreaObject.setLineWrap(true);
	textAreaObject.setWrapStyleWord(true);

	text = textAreaObject;
    }

    private void createHtmlPane(byte[] content) {
	// charset specifications inside the document don't work with the parser
	// we try to parse the file with UTF-8 and try to determine the actual encoding if the parser chokes
	// if the determination is not successful we fall back to UTF-8
	// charset errors are ignored in the
	Charset charset = StandardCharsets.UTF_8;
	try {
	    HTMLEditorKit kit = new HTMLEditorKit();
	    HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
	    kit.read(new InputStreamReader(new ByteArrayInputStream(content)), doc, 0);
	} catch (ChangedCharSetException ex) {
	    try {
		String spec = ex.getCharSetSpec();
		MimeType mt = new MimeType(spec);
		String charsetString = mt.getParameter("charset");
		if (charsetString != null && ! charsetString.isEmpty()) {
		    charset = Charset.forName(charsetString);
		}
	    } catch (MimeTypeParseException ex2) {
		LOG.warn("Failed to parse MIME Type specification.", ex2);
	    } catch (IllegalCharsetNameException | UnsupportedCharsetException ex2) {
		LOG.warn("Unsupported charset specification inside HTML docuemnt.");
	    }
	} catch (BadLocationException | IOException ex) {
	    LOG.error("Failed to parse HTML document.", ex);
	}


	JTextPane htmlText = new JTextPane();
	htmlText.setMargin(new Insets(0, 13, 0, 0));
	htmlText.setEditable(false);
	htmlText.setContentType("text/html");

	HTMLDocument doc = (HTMLDocument) htmlText.getDocument();
	doc.putProperty("IgnoreCharsetDirective", true);
	htmlText.setText(new String(content, charset));

	text = htmlText;
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
	final JButton pdfButton = new JButton(LANG.translationForKey("open.pdf.in.external.viewer"));
	pdfButton.addActionListener((ActionEvent e) -> {
	    if (e.getSource() == pdfButton) {
		try {
		    Desktop desktop = Desktop.getDesktop();
		    desktop.open(new File(pdfFile));
		} catch (IOException ex) {
		    LOG.error("Failed to open pdf file.", ex);
		}
	    }
	});
	contentPane.add(pdfButton);
	text = contentPane;
    }

    private void createPdfComponent(byte[] pdfData) {
	try {
	    PDDocument doc = PDDocument.load(pdfData);
	    PdfComponent pdfComp = new PdfComponent(doc) {
		// override so sizing the pdf component works properly in the gridlayout
		@Override
		public Dimension getPreferredSize() {
		    if (!isPreferredSizeSet() && isValidPage()) {
			int compWidth = getWidth();
			PDRectangle pdfRect = getPageDim(getCurPage());
			if (compWidth > 0) {
			    float scale = compWidth / pdfRect.getWidth();
			    int prefHeight = (int) Math.ceil(pdfRect.getHeight() * scale);
			    return new Dimension(compWidth, prefHeight);
			}
		    }

		    return super.getPreferredSize();
		}
	    };
	    pdfComp.setCurrentPage(0);
	    text = pdfComp;
	} catch (IOException ex) {
	    LOG.error("Failed to load PDF document.");
	    text = new JLabel("Failed to load PDF.");
	}
    }

}
