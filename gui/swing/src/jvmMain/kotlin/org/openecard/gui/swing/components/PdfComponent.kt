/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 */
package org.openecard.gui.swing.components

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.rendering.PDFRenderer
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.io.IOException
import javax.swing.JComponent

private val LOG = KotlinLogging.logger { }

/**
 * Simple component rendering a page of a PDF document.
 *
 * @author Tobias Wich
 */
open class PdfComponent(
	private val pdfDoc: PDDocument,
) : JComponent() {
	private val renderer: PDFRenderer
	private var curPage: Int

	init {
		this.curPage = -1

		renderer = PDFRenderer(pdfDoc)
		renderer.isSubsamplingAllowed = true
	}

	public override fun paintComponent(g: Graphics?) {
		if (g is Graphics2D) {
			val g2 = g

			if (curPage >= 0 && curPage < this.numPages) {
				try {
					val bounds = getPageDim(pdfDoc.getPage(curPage))
					val pdfHeigth = bounds.height
					val pdfWidth = bounds.width
					val componentHeigth = getHeight().toFloat()
					val componentWidth = getWidth().toFloat()

					// scale to height and if that would crop the width, scale according to width
					var scale = componentHeigth / pdfHeigth
					if (pdfWidth * scale > componentWidth) {
						scale = componentWidth / pdfWidth
					}

					g2.background = Color.WHITE
					renderer.renderPageToGraphics(curPage, g2, scale)
				} catch (ex: IOException) {
					LOG.error(ex) { "Failed to draw PDF page." }
				}
			}
		}
	}

	val numPages: Int
		get() = pdfDoc.numberOfPages

	fun setCurrentPage(pageNum: Int) {
		val oldPage = this.curPage
		this.curPage = pageNum

		firePropertyChange("page", oldPage, pageNum)

		if (this.isValidPage) {
			revalidate()
			repaint()
		}
	}

	protected val isValidPage: Boolean
		get() = curPage >= 0 && curPage < this.numPages

	override fun getPreferredSize(): Dimension? {
		if (!isPreferredSizeSet && this.isValidPage) {
			val bounds = getPageDim(pdfDoc.getPage(curPage))
			return Dimension(bounds.width.toInt(), bounds.height.toInt())
		} else {
			return super.getPreferredSize()
		}
	}

	protected fun getCurPage(): PDPage = pdfDoc.getPage(curPage)

	protected fun getPageDim(page: PDPage): PDRectangle {
		val bounds = page.cropBox
		return bounds
	}
}
