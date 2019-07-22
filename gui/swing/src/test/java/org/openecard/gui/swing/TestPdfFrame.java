/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.gui.swing;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.swing.JFrame;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.openecard.gui.swing.components.PdfComponent;
import org.testng.annotations.Test;


/**
 *
 * @author Tobias Wich
 */
public class TestPdfFrame {

    @Test
    public void startFrame() throws IOException, InterruptedException, ExecutionException {
	JFrame f = new JFrame();
	f.setSize(800, 600);
	f.setLayout(new BorderLayout());
	f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	InputStream in = RunGUI.class.getResourceAsStream("/description.pdf");
	PDDocument doc = PDDocument.load(in);
	PdfComponent pdfComp = new PdfComponent(doc);
	pdfComp.setCurrentPage(0);

	f.add(pdfComp, BorderLayout.CENTER);

	CompletableFuture closed = new CompletableFuture();
	f.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosed(WindowEvent e) {
		closed.complete(null);
		super.windowClosed(e);
	    }
	});

	f.setVisible(true);
	closed.get();
    }

}
