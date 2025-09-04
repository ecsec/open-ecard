package org.openecard.richclient.pinmanagement.util

import javafx.scene.image.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

fun BufferedImage.toFXImage(): Image {
	val output = ByteArrayOutputStream()
	ImageIO.write(this, "png", output)
	return Image(ByteArrayInputStream(output.toByteArray()))
}
