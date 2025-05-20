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

import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.ImageTranscoder
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.InputStream

fun Image.toBufferedImage() =
	when (val it = this) {
		is BufferedImage -> it
		else -> {
			BufferedImage(
				this.getWidth(null),
				this.getHeight(null),
				BufferedImage.TYPE_INT_ARGB,
			).apply {
				createGraphics().apply {
					drawImage(it, 0, 0, null)
					dispose()
				}
			}
		}
	}

class SvgTranscoder(
	val svg: InputStream,
	width: Int,
	height: Int,
) : ImageTranscoder() {
	var image: BufferedImage? = null
	val filters: MutableList<((BufferedImage) -> BufferedImage)> = mutableListOf()

	init {
		addTranscodingHint(KEY_WIDTH, width.toFloat())
		addTranscodingHint(KEY_HEIGHT, height.toFloat())
	}

	override fun createImage(
		p0: Int,
		p1: Int,
	): BufferedImage? =
		BufferedImage(
			p0,
			p1,
			BufferedImage.TYPE_INT_ARGB,
		)

	override fun writeImage(
		img: BufferedImage?,
		tout: TranscoderOutput?,
	) {
		img?.let {
			var filteredImage = it
			filters.forEach { filter ->
				filteredImage = filter(filteredImage)
			}
			image = filteredImage
		}
	}

	fun getBufferedImage(): BufferedImage? {
		val input = TranscoderInput(svg)
		super.transcode(input, null)
		return image
	}
}
