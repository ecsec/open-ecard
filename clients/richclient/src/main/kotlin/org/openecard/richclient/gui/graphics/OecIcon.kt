/****************************************************************************
 * Copyright (C) 2025 ecsec GmbH.
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

package org.openecard.richclient.gui.graphics

import org.openecard.common.util.FileUtils.resolveResourceAsStream
import java.awt.Image
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.awt.image.FilteredImageSource
import javax.swing.GrayFilter

enum class OecIconType(
	val path: String,
	val grayVal: Int = 0,
) {
	COLORED("images/oec_logo_bg-white.svg"),
	BLACK("images/oec_logo_black_bg-transparent.svg", 0),
	GRAY("images/oec_logo_black_bg-transparent.svg", 75),
	WHITE("images/oec_logo_black_bg-transparent.svg", 100),
}

fun grayscale(
	img: Image,
	grayVal: Int,
): BufferedImage {
	val gf = GrayFilter(true, grayVal)
	val producer = FilteredImageSource(img.source, gf)
	val img = Toolkit.getDefaultToolkit().createImage(producer)
	return img.toBufferedImage()
}

fun oecImage(
	kind: OecIconType,
	width: Int,
	height: Int,
): BufferedImage {
	val input =
		resolveResourceAsStream(kind.javaClass, kind.path)
			?: throw(IllegalStateException("Image could not be loaded"))
	return SvgTranscoder(input, width, height)
		.apply {
			when (kind) {
				OecIconType.WHITE,
				OecIconType.BLACK,
				OecIconType.GRAY,
				-> {
					filters.add { img ->
						grayscale(img, kind.grayVal)
					}
				}
				else -> {}
			}
		}.getBufferedImage() ?: throw(IllegalStateException("Image could not be loaded"))
}
