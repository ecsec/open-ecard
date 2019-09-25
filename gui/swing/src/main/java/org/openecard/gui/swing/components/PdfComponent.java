/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.gui.swing.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import javax.swing.JComponent;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple component rendering a page of a PDF document.
 *
 * @author Tobias Wich
 */
public class PdfComponent extends JComponent {

    private static final Logger LOG = LoggerFactory.getLogger(PdfComponent.class);

    private final PDDocument pdfDoc;
    private final PDFRenderer renderer;
    private int curPage;

    public PdfComponent(PDDocument pdfDoc) {
	this.pdfDoc = pdfDoc;
	this.curPage = -1;

	renderer = new PDFRenderer(pdfDoc);
	renderer.setSubsamplingAllowed(true);
    }

    @Override
    public void paintComponent(Graphics g) {
	if (g instanceof Graphics2D) {
	    Graphics2D g2 = (Graphics2D) g;

	    if (curPage >= 0 && curPage < getNumPages()) {
		try {
		    PDRectangle bounds = getPageDim(pdfDoc.getPage(curPage));
		    float pdfHeigth = bounds.getHeight();
		    float pdfWidth = bounds.getWidth();
		    float componentHeigth = getHeight();
		    float componentWidth = getWidth();

		    // scale to height and if that would crop the width, scale according to width
		    float scale = componentHeigth / pdfHeigth;
		    if (pdfWidth * scale > componentWidth) {
			scale = componentWidth / pdfWidth;
		    }

		    g2.setBackground(Color.WHITE);
		    renderer.renderPageToGraphics(curPage, g2, scale);
		} catch (IOException ex) {
		    LOG.error("Failed to draw PDF page.", ex);
		}
	    }
	}
    }

    public int getNumPages() {
	return pdfDoc.getNumberOfPages();
    }

    public void setCurrentPage(int pageNum) {
	int oldPage = this.curPage;
	this.curPage = pageNum;

	firePropertyChange("page", oldPage, pageNum);

	if (isValidPage()) {
	    revalidate();
	    repaint();
	}
    }

    protected boolean isValidPage() {
	return curPage >= 0 && curPage < getNumPages();
    }


    @Override
    public Dimension getPreferredSize() {
	if (! isPreferredSizeSet() && isValidPage()) {
	    PDRectangle bounds = getPageDim(pdfDoc.getPage(curPage));
	    return new Dimension((int) bounds.getWidth(), (int) bounds.getHeight());
	} else {
	    return super.getPreferredSize();
	}
    }

    protected PDPage getCurPage() {
	return pdfDoc.getPage(curPage);
    }

    protected PDRectangle getPageDim(PDPage page) {
	PDRectangle bounds = page.getCropBox();
	return bounds;
    }

}
