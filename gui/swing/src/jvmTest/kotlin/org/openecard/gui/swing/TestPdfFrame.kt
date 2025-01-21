/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 */
package org.openecard.gui.swing

import org.apache.pdfbox.pdmodel.PDDocument
import org.openecard.gui.swing.components.PdfComponent
import org.testng.annotations.Test
import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import javax.swing.JFrame

/**
 *
 * @author Tobias Wich
 */
class TestPdfFrame {

    @Test(enabled = false)
    @Throws(IOException::class, InterruptedException::class, ExecutionException::class)
    fun startFrame() {
        val f = JFrame()
        f.setSize(800, 600)
		f.layout = BorderLayout()
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE)

        val `in` = RunGUI::class.java.getResourceAsStream("/description.pdf")
        val doc = PDDocument.load(`in`)
        val pdfComp = PdfComponent(doc)
        pdfComp.setCurrentPage(0)

        f.add(pdfComp, BorderLayout.CENTER)

        val closed: CompletableFuture<*> = CompletableFuture<Any?>()
        f.addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent?) {
                closed.complete(null)
                super.windowClosed(e)
            }
        })

		f.isVisible = true
        closed.get()
    }
}
