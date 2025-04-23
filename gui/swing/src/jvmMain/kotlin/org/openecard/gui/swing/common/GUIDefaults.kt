/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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
package org.openecard.gui.swing.common

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.util.FileUtils.resolveResourceAsURL
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Image
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.regex.Pattern
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.UIDefaults
import javax.swing.UIManager

private val LOG = KotlinLogging.logger { }

/**
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
object GUIDefaults {
	// Regex pattern for hex colors
	private const val HEX_PATTERN = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"
	private val HEX_COLOR_PATTERN: Pattern = Pattern.compile(HEX_PATTERN)

	// Swing UIDefaults
	private val DEFAULTS: UIDefaults = UIManager.getDefaults()
	private val OWN_DEFAULTS = UIDefaults()
	private val COLOR_PROPERTIES =
		listOf("foreground", "background", "selectionBackground", "selectionForeground", "disabledText")
	private val FONT_PROPERTIES = listOf("font", "titleFont", "acceleratorFont")
	private val ICON_PROPERTIES = listOf("icon", "selectedIcon", "disabledIcon", "disabledSelectedIcon")

	private fun getProperty(identifier: String): Any? = OWN_DEFAULTS[identifier]

	fun getColor(identifier: String): Color {
		val color = getProperty(identifier) as Color?
		return color ?: Color.WHITE
	}

	fun getFont(identifier: String): Font {
		val font = getProperty(identifier) as Font?
		return font ?: Font(Font.SANS_SERIF, Font.PLAIN, 12)
	}

	fun getImage(
		identifier: String,
		width: Int,
		height: Int,
	): ImageIcon? =
		(getProperty(identifier) as ImageIcon?) ?.let { icon ->
			if (width > -1 || height > -1) {
				var image = icon.getImage()
				image = image.getScaledInstance(width, height, Image.SCALE_SMOOTH)
				ImageIcon(image)
			} else {
				icon
			}
		}

	@JvmStatic
	fun getImage(identifier: String): ImageIcon? = getImage(identifier, -1, -1)

	@JvmStatic
	fun getImageStream(
		identifier: String,
		width: Int,
		height: Int,
	): InputStream? = getImage(identifier, width, height)?.let { getImageStream(it) }

	fun getImageStream(identifier: String): InputStream? = getImage(identifier)?.let { getImageStream(it) }

	private fun getImageStream(icon: ImageIcon): InputStream {
		val bi =
			BufferedImage(
				icon.iconWidth,
				icon.iconHeight,
				BufferedImage.TYPE_INT_ARGB,
			)
		val g: Graphics = bi.createGraphics()
		// paint the Icon to the BufferedImage
		icon.paintIcon(null, g, 0, 0)
		g.dispose()

		try {
			val os = ByteArrayOutputStream()
			ImageIO.write(bi, "PNG", os)
			val `is`: InputStream = ByteArrayInputStream(os.toByteArray())

			return `is`
		} catch (ex: IOException) {
			throw IllegalArgumentException("Failed to convert image to PNG.")
		}
	}

	private var isInitialized = false

	@JvmStatic
	fun initialize() {
		if (isInitialized) {
			return
		}

		try {
			// disabled as this causes hangs with gtk native calls inside swing and systray
			// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

			val toolkit = Toolkit.getDefaultToolkit()
			val guiProps = GUIProperties()
			val props = guiProps.properties()

			for (property in props.stringPropertyNames()) {
				try {
					val propertyName = property.substring(0, property.indexOf('.'))
					val propertyAttribute = property.substring(propertyName.length + 1, property.length)
					var value = props.getProperty(property) as String

					if (COLOR_PROPERTIES.contains(propertyAttribute)) {
						// Parse color property
						validateHexColor(value)
						if (value.length == 4) {
							val sb = StringBuilder("#")
							for (i in 1..<value.length) {
								sb.append(value.substring(i, i + 1))
								sb.append(value.substring(i, i + 1))
							}
							value = sb.toString()
						}
						val color = Color.decode(value)
						DEFAULTS.put(property, color)
						OWN_DEFAULTS.put(property, color)
					} else if (FONT_PROPERTIES.contains(propertyAttribute)) {
						// Parse font property
						val font = Font.decode(value)
						DEFAULTS.put(property, font)
						OWN_DEFAULTS.put(property, font)
					} else if (ICON_PROPERTIES.contains(propertyAttribute)) {
						// Parse icon property
						val url = resolveResourceAsURL(guiProps.javaClass, value)
						if (url == null) {
							LOG.error { "Cannot parse the property: $property" }
						} else {
							val image = toolkit.getImage(url)
							val icon = ImageIcon(image)
							DEFAULTS.put(property, icon)
							OWN_DEFAULTS.put(property, icon)
						}
					}
				} catch (e: Exception) {
					LOG.error { "Cannot parse the property: $property" }
				}
			}
		} catch (e: Exception) {
			LOG.error { e.message }
		}

		isInitialized = true
	}

	@Throws(IllegalArgumentException::class)
	private fun validateHexColor(hex: String) {
		require(HEX_COLOR_PATTERN.matcher(hex).matches())
	}
}
