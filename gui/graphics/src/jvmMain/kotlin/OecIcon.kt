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

import org.openecard.common.util.FileUtils.resolveResourceAsStream
import java.awt.image.BufferedImage
import kotlin.jvm.java

fun oecImage(
	width: Int,
	height: Int,
): BufferedImage {
	val input =
		resolveResourceAsStream(object {}::class.java, "openecard_logo.svg")
			?: throw(IllegalStateException("Image could not be loaded"))
	return SvgTranscoder(input, width, height)
		.getBufferedImage() ?: throw(IllegalStateException("Image could not be loaded"))
}
