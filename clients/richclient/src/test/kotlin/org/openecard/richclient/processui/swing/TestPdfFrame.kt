/*
 * Copyright (C) 2019 ecsec GmbH.
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
 */

package org.openecard.richclient.processui.swing

import org.apache.pdfbox.Loader
import org.apache.pdfbox.io.NonSeekableRandomAccessReadInputStream
import org.junit.jupiter.api.Disabled
import org.openecard.richclient.processui.swing.components.PdfComponent
import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import javax.swing.JFrame
import kotlin.test.Test

/**
 *
 * @author Tobias Wich
 */
class TestPdfFrame {
	@Disabled
	@Test
	@Throws(IOException::class, InterruptedException::class, ExecutionException::class)
	fun startFrame() {
		val f = JFrame()
		f.setSize(800, 600)
		f.layout = BorderLayout()
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE)

		val `in` = RunGUI::class.java.getResourceAsStream("/description.pdf")
		val doc = Loader.loadPDF(NonSeekableRandomAccessReadInputStream(`in`))
		val pdfComp = PdfComponent(doc)
		pdfComp.setCurrentPage(0)

		f.add(pdfComp, BorderLayout.CENTER)

		val closed: CompletableFuture<*> = CompletableFuture<Any?>()
		f.addWindowListener(
			object : WindowAdapter() {
				override fun windowClosed(e: WindowEvent?) {
					closed.complete(null)
					super.windowClosed(e)
				}
			},
		)

		f.isVisible = true
		closed.get()
	}
}
