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
package org.openecard.gui.swing.components

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.activation.MimeType
import jakarta.activation.MimeTypeParseException
import org.apache.pdfbox.pdmodel.PDDocument
import org.openecard.common.I18n
import org.openecard.common.util.FileUtils.homeConfigDir
import org.openecard.gui.definition.Document
import org.openecard.gui.definition.OutputInfoUnit
import org.openecard.gui.definition.ToggleText
import org.openecard.gui.swing.common.GUIDefaults
import java.awt.Color
import java.awt.Component
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.IllegalCharsetNameException
import java.nio.charset.StandardCharsets
import java.nio.charset.UnsupportedCharsetException
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JTextPane
import javax.swing.SwingConstants
import javax.swing.UIManager
import javax.swing.border.EmptyBorder
import javax.swing.text.BadLocationException
import javax.swing.text.ChangedCharSetException
import javax.swing.text.html.HTMLDocument
import javax.swing.text.html.HTMLEditorKit
import kotlin.math.ceil

private val LOG = KotlinLogging.logger { }

/**
 *
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 */
class ToggleText(
	buttonText: String,
	content: Document,
	collapsed: Boolean,
	private val externalPdf: Boolean = false,
) : StepComponent {
	private val rootPanel: JPanel = JPanel()
	private val button: JButton = JButton("$buttonText  ")
	private var text: Component

	/**
	 * Creates a new ToggleText.
	 *
	 * @param toggleText
	 */
	constructor(toggleText: ToggleText) : this(
		toggleText.title!!,
		toggleText.document!!,
		toggleText.isCollapsed,
	)

	/**
	 * Creates a new ToggleText instance.
	 *
	 * @param buttonText Text of the button
	 * @param contentText The test to display if collapsed = false
	 * @param collapsed Indicates whether the `contentText` is displayed or not.
	 */
	@JvmOverloads
	constructor(buttonText: String, contentText: String, collapsed: Boolean = false) : this(
		buttonText,
		Document(
			"text/plain",
			contentText.toByteArray(
				Charset.forName("UTF-8"),
			),
		),
		collapsed,
	)

	/**
	 * Creates a new ToggleText.
	 *
	 * @param buttonText Text of the button
	 * @param content [Document] representing the content of the instance.
	 * @param collapsed Collapsed (content is visible or not)
	 */
	init {
		text = initText(content)
		initComponents()
		initLayout()
		loadUIDefaults()

		button.setSelected(collapsed)
		text.isVisible = !collapsed
		button.setIcon(if (!collapsed) OPENED_INDICATOR else CLOSED_INDOCATOR)
	}

	private fun initText(content: Document): JComponent {
		val mimeType = content.mimeType
		return when (mimeType) {
			"text/html" -> createHtmlPane(content.value)
			"text/plain" -> createJTextArea(content.value.decodeToString())
			"application/pdf" -> {
				if (!externalPdf) {
					createPdfComponent(content.value)
				} else {
					val pdfFile = createTmpPdf(content.value)
					createStartPdfViewButton(pdfFile)
				}
			}

			else -> {
				LOG.warn {
					"Unsupported usage of content of type $mimeType in ${org.openecard.gui.swing.components.ToggleText::class.java}"
				}
				createJTextArea(LANG.translationForKey("unsupported.mimetype", mimeType))
			}
		}
	}

	private fun initComponents() {
		button.addActionListener(
			ActionListener { e: ActionEvent? ->
				text.isVisible = !text.isVisible
				button.setIcon(if (text.isVisible) OPENED_INDICATOR else CLOSED_INDOCATOR)
				rootPanel.revalidate()
				rootPanel.doLayout()
				rootPanel.repaint()
			},
		)
	}

	/**
	 * Initializes the layout of the panel.
	 */
	private fun initLayout() {
		rootPanel.setLayout(GridBagLayout())

		val gbc = GridBagConstraints()

		// Add elements
		gbc.gridx = 0
		gbc.gridy = 0
		gbc.fill = GridBagConstraints.HORIZONTAL
		gbc.gridwidth = GridBagConstraints.REMAINDER
		gbc.weightx = 1.0
		rootPanel.add(button, gbc)

		gbc.gridx = 0
		gbc.gridy = 1
		rootPanel.add(text, gbc)
	}

	private fun loadUIDefaults() {
		val defaults = UIManager.getDefaults()

		var bg = defaults[TOGGLETEXT_BACKGROUND] as Color?
		if (bg == null) {
			bg = Color.WHITE
		}
		var fg = defaults[TOGGLETEXT_FOREGROUND] as Color?
		if (fg == null) {
			fg = Color.BLACK
		}
		var fgIndicator = defaults[TOGGLETEXT_INDICATOR_FOREGROUND] as Color?
		if (fgIndicator == null) {
			fgIndicator = Color.LIGHT_GRAY
		}
		val font = UIManager.getFont("Label.font")

		button.setOpaque(true)
		button.setFocusPainted(false)
		button.setBorder(EmptyBorder(Insets(0, 0, 0, 0)))
		button.setHorizontalAlignment(SwingConstants.LEFT)
		button.setMargin(Insets(0, 0, 0, 0))
		button.setBounds(0, 0, 0, 0)
		button.setFont(font.deriveFont(Font.BOLD))
		button.setContentAreaFilled(false)
		button.setHorizontalTextPosition(SwingConstants.TRAILING)

		text.setFont(font.deriveFont(Font.PLAIN))

		rootPanel.setBackground(bg)
		rootPanel.setForeground(fg)

		for (i in 0..<rootPanel.componentCount) {
			rootPanel.getComponent(i).setBackground(bg)
			rootPanel.getComponent(i).setForeground(fg)
		}
	}

	override val component: Component = rootPanel

	override val isValueType: Boolean = false

	override fun validate(): Boolean = true

	override val value: OutputInfoUnit? = null

	/**
	 * Creates a JTextArea containing the given content.
	 *
	 * @param content The content to display in the JTextArea.
	 */
	private fun createJTextArea(content: String): JTextArea {
		val textAreaObject = JTextArea(content)
		textAreaObject.setMargin(Insets(0, 13, 0, 0))
		textAreaObject.isEditable = false
		textAreaObject.setLineWrap(true)
		textAreaObject.setWrapStyleWord(true)

		return textAreaObject
	}

	private fun createHtmlPane(content: ByteArray): JTextPane {
		// charset specifications inside the document don't work with the parser
		// we try to parse the file with UTF-8 and try to determine the actual encoding if the parser chokes
		// if the determination is not successful we fall back to UTF-8
		// charset errors are ignored in the
		var charset = StandardCharsets.UTF_8
		try {
			val kit = HTMLEditorKit()
			val doc = kit.createDefaultDocument() as HTMLDocument?
			kit.read(InputStreamReader(content.inputStream()), doc, 0)
		} catch (ex: ChangedCharSetException) {
			try {
				val spec = ex.getCharSetSpec()
				val mt = MimeType(spec)
				val charsetString = mt.getParameter("charset")
				if (charsetString != null && !charsetString.isEmpty()) {
					charset = Charset.forName(charsetString)
				}
			} catch (ex2: MimeTypeParseException) {
				LOG.warn(ex2) { "Failed to parse MIME Type specification." }
			} catch (ex2: IllegalCharsetNameException) {
				LOG.warn { "Unsupported charset specification inside HTML document." }
			} catch (ex2: UnsupportedCharsetException) {
				LOG.warn { "Unsupported charset specification inside HTML document." }
			}
		} catch (ex: BadLocationException) {
			LOG.error(ex) { "Failed to parse HTML document." }
		} catch (ex: IOException) {
			LOG.error(ex) { "Failed to parse HTML document." }
		}

		val htmlText = JTextPane()
		htmlText.setMargin(Insets(0, 13, 0, 0))
		htmlText.isEditable = false
		htmlText.setContentType("text/html")

		val doc = htmlText.document as HTMLDocument
		doc.putProperty("IgnoreCharsetDirective", true)
		htmlText.text = String(content, charset)

		return htmlText
	}

	private fun createStartPdfViewButton(pdfFile: String): JPanel {
		val contentPane = JPanel()
		// TODO translate
		val pdfButton = JButton(LANG.translationForKey("open.pdf.in.external.viewer"))
		pdfButton.addActionListener(
			ActionListener { e ->
				if (e.getSource() === pdfButton) {
					try {
						val desktop = Desktop.getDesktop()
						desktop.open(File(pdfFile))
					} catch (ex: IOException) {
						LOG.error(ex) { "Failed to open pdf file." }
					}
				}
			},
		)
		contentPane.add(pdfButton)
		return contentPane
	}

	private fun createPdfComponent(pdfData: ByteArray): JComponent {
		try {
			val doc = PDDocument.load(pdfData)
			val pdfComp: PdfComponent =
				object : PdfComponent(doc) {
					// override so sizing the pdf component works properly in the gridlayout
					override fun getPreferredSize(): Dimension? {
						if (!isPreferredSizeSet && isValidPage) {
							val compWidth = getWidth()
							val pdfRect = getPageDim(getCurPage())
							if (compWidth > 0) {
								val scale = compWidth / pdfRect.width
								val prefHeight = ceil((pdfRect.height * scale).toDouble()).toInt()
								return Dimension(compWidth, prefHeight)
							}
						}

						return super.getPreferredSize()
					}
				}
			pdfComp.setCurrentPage(0)
			return pdfComp
		} catch (ex: IOException) {
			LOG.error { "Failed to load PDF document." }
			return JLabel("Failed to load PDF.")
		}
	}
}

private val LANG: I18n = I18n.getTranslation("swing")

private const val TOGGLETEXT = "ToggleText"
private val TOGGLETEXT_FOREGROUND = "$TOGGLETEXT.foreground"
private val TOGGLETEXT_BACKGROUND = "$TOGGLETEXT.background"
private val TOGGLETEXT_INDICATOR_FOREGROUND = TOGGLETEXT + "Indicator.foreground"
private val OPENED_INDICATOR: Icon = GUIDefaults.getImage("ToggleText.selectedIcon")!!
private val CLOSED_INDOCATOR: Icon = GUIDefaults.getImage("ToggleText.icon")!!

/**
 * Creates a tmp PDF file containing the given content.
 *
 * Note: The file is deleted as soon as the JVM terminates.
 *
 * @param content The content which shall be written to the PDF file.
 * @return The path to the PDF file.
 */
@Throws(IOException::class, FileNotFoundException::class)
private fun createTmpPdf(content: ByteArray): String {
	val tmpPdf = File.createTempFile("tmp", ".pdf", createTmpDir())
	tmpPdf.deleteOnExit()
	tmpPdf.outputStream().use {
		it.write(content)
	}

	return tmpPdf.absolutePath
}

/**
 * Creates the temp directory for the PDF, HTML and Java Script files.
 *
 * Note: The directory is deleted as soon as the jvm terminates.
 */
@Throws(IOException::class, SecurityException::class)
private fun createTmpDir(): File {
	val tmpDirPath = homeConfigDir.absolutePath + "/tmp"
	val tmpDir = File(tmpDirPath)
	tmpDir.mkdirs()
	tmpDir.deleteOnExit()
	return tmpDir
}
