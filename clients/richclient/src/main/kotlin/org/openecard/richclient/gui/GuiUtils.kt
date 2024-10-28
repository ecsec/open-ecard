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

package org.openecard.richclient.gui

import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.Image
import java.awt.Toolkit
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import javax.imageio.ImageIO
import javax.swing.ImageIcon

private val LOG = KotlinLogging.logger {  }

/**
 *
 * @author Johannes Schmölz
 */
object GuiUtils {

    private const val IMG_HEIGHT = 81
    private const val IMG_WIDTH = 128

    fun getScaledCardImageIcon(imageStream: InputStream): ImageIcon {
        var icon = ImageIcon()
        try {
            icon = ImageIcon(ImageIO.read(imageStream))
            icon.image = icon.image.getScaledInstance(IMG_WIDTH, IMG_HEIGHT, Image.SCALE_SMOOTH)
        } catch (ex: IOException) {
			LOG.error(ex) { "Failed to read image stream." }
        }
        return icon
    }

    fun getImage(name: String): Image {
        val imgData = getImageData(name)
        val img = Toolkit.getDefaultToolkit().createImage(imgData)
        return img
    }

    private fun getImageData(name: String): ByteArray {
        var imageUrl = GuiUtils::class.java.getResource("images/$name")
        if (imageUrl == null) {
            imageUrl = GuiUtils::class.java.getResource("/images/$name")
        }
        if (imageUrl == null) {
			LOG.error { "Failed to find image ${name}." }
            return ByteArray(0)
        }

        try {
            imageUrl.openStream().use { `in` ->
                return getImageData(`in`)
            }
        } catch (ex: IOException) {
			LOG.error(ex) { "Failed to read image $name." }
            return ByteArray(0)
        }
    }

    @Throws(IOException::class)
    private fun getImageData(`in`: InputStream): ByteArray {
        val out = ByteArrayOutputStream(40 * 1024)
        val buf = ByteArray(4096)
        var numRead: Int

        while ((`in`.read(buf).also { numRead = it }) != -1) {
            out.write(buf, 0, numRead)
        }

        return out.toByteArray()
    }
}
