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

package org.openecard.richclient.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import javax.swing.JPanel;


/**
 * This class creates a Panel having a gradient as background. The gradient runs form left to right, with color1
 * being {@link java.awt.Color#LIGHT_GRAY LIGHT_GRAY} and color2 being {@link java.awt.Color#DARK_GRAY DARK_GRAY}.
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public class GradientPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private Color color1;
    private Color color2;

    /**
     * Constructor of GradientPanel class.
     */
    public GradientPanel() {
	super();
    }

    /**
     * Constructor of GradientPanel class.
     *
     * @param color1 color on the left
     * @param color2 color on the right
     */
    public GradientPanel(Color color1, Color color2) {
	this.color1 = color1;
	this.color2 = color2;
    }

    /**
     * Constructor of GradientPanel class.
     *
     * @param layout layout of the panel
     */
    public GradientPanel(LayoutManager layout) {
	super(layout);
    }

    /**
     * Constructor of GradientPanel class.
     *
     * @param layout layout of the panel
     * @param color1 color on the left
     * @param color2 color on the right
     */
    public GradientPanel(LayoutManager layout, Color color1, Color color2) {
	super(layout);

	this.color1 = color1;
	this.color2 = color2;
    }

    /**
     * Constructor of GradientPanel class.
     *
     * @param isDoubleBuffered set to true to enable double-buffering
     */
    public GradientPanel(boolean isDoubleBuffered) {
	super(isDoubleBuffered);
    }

    /**
     * Constructor of GradientPanel class.
     *
     * @param isDoubleBuffered set to true to enable double-buffering
     * @param color1 color on the left
     * @param color2 color on the right
     */
    public GradientPanel(boolean isDoubleBuffered, Color color1, Color color2) {
	super(isDoubleBuffered);

	this.color1 = color1;
	this.color2 = color2;
    }

    /**
     * Constructor of GradientPanel class.
     *
     * @param layout layout of the panel
     * @param isDoubleBuffered set to true to enable double-buffering
     */
    public GradientPanel(LayoutManager layout, boolean isDoubleBuffered) {
	super(layout, isDoubleBuffered);
    }

    /**
     * Constructor of GradientPanel class.
     *
     * @param layout layout of the panel
     * @param isDoubleBuffered set to true to enable double-buffering
     * @param color1 color on the left
     * @param color2 color on the right
     */
    public GradientPanel(LayoutManager layout, boolean isDoubleBuffered, Color color1, Color color2) {
	super(layout, isDoubleBuffered);

	this.color1 = color1;
	this.color2 = color2;
    }

    @Override
    protected void paintComponent(Graphics g) {
	Graphics2D g2d = (Graphics2D) g.create();

	if (color1 == null) {
	    color1 = Color.LIGHT_GRAY;
	}

	if (color2 == null) {
	    color2 = Color.DARK_GRAY;
	}

	int w = getWidth();
	int h = getHeight();

	// gradient from left to right
	GradientPaint gp = new GradientPaint(0, 0, color1, w, 0, color2);

	g2d.setPaint(gp);
	g2d.fillRect(0, 0, w, h);
	g2d.dispose();

	// The gradient will be painted on top of the panel so that all components added to it will be hidden.
	// In order to make them visible again paintComponent(g) must be called on the superclass.
	super.paintComponent(g);
    }

    public Color getColor1() {
	return color1;
    }

    public void setColor1(Color color1) {
	this.color1 = color1;
    }

    public Color getColor2() {
	return color2;
    }

    public void setColor2(Color color2) {
	this.color2 = color2;
    }

}
