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

package org.openecard.client.gui.graphics;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.Icon;

/**
 * This class has been automatically generated using svg2java
 *
 */
public class OecLogoBgWhite implements Icon {

    private float origAlpha = 1.0f;

    /**
     * Paints the transcoded SVG image on the specified graphics context. You can install a custom transformation on the
     * graphics context to scale the image.
     *
     * @param g Graphics context.
     */
    public void paint(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        origAlpha = 1.0f;
        Composite origComposite = g.getComposite();
        if (origComposite instanceof AlphaComposite) {
            AlphaComposite origAlphaComposite = (AlphaComposite) origComposite;
            if (origAlphaComposite.getRule() == AlphaComposite.SRC_OVER) {
                origAlpha = origAlphaComposite.getAlpha();
            }
        }

        // _0
        AffineTransform trans_0 = g.getTransform();
        paintRootGraphicsNode_0(g);
        g.setTransform(trans_0);

    }

    private void paintShapeNode_0_0_0_0_0_0(Graphics2D g) {
        RoundRectangle2D.Double shape0 = new RoundRectangle2D.Double(129.79100036621094, 71.53150177001953, 49.44599914550781, 49.50519943237305, 20.475799560546875, 20.475799560546875);
        g.setPaint(new LinearGradientPaint(new Point2D.Double(128.79100036621094, 96.28410339355469), new Point2D.Double(180.23699951171875, 96.28410339355469), new float[]{0.0f, 1.0f}, new Color[]{new Color(255, 255, 255, 255), new Color(255, 255, 255, 255)}, MultipleGradientPaint.CycleMethod.NO_CYCLE, MultipleGradientPaint.ColorSpaceType.SRGB, new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f)));
        g.fill(shape0);
    }

    private void paintCompositeGraphicsNode_0_0_0_0_0(Graphics2D g) {
        // _0_0_0_0_0_0
        AffineTransform trans_0_0_0_0_0_0 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
        paintShapeNode_0_0_0_0_0_0(g);
        g.setTransform(trans_0_0_0_0_0_0);
    }

    private void paintCompositeGraphicsNode_0_0_0_0(Graphics2D g) {
        // _0_0_0_0_0
        AffineTransform trans_0_0_0_0_0 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
        paintCompositeGraphicsNode_0_0_0_0_0(g);
        g.setTransform(trans_0_0_0_0_0);
    }

    private void paintShapeNode_0_0_0_1_0_0(Graphics2D g) {
        GeneralPath shape1 = new GeneralPath();
        shape1.moveTo(104.65625, 567.59375);
        shape1.lineTo(104.68745, 620.1875);
        shape1.curveTo(115.898735, 622.3748, 124.37495, 632.2423, 124.37495, 644.09375);
        shape1.curveTo(124.37495, 653.61725, 118.901405, 661.8675, 110.93745, 665.875);
        shape1.lineTo(132.37494, 715.46875);
        shape1.curveTo(159.33252, 703.8017, 178.39812, 677.1614, 178.90619, 645.84375);
        shape1.curveTo(179.25227, 624.5215, 170.92862, 605.0594, 157.18744, 590.84375);
        shape1.curveTo(143.04967, 576.7933, 126.39169, 567.7846, 104.65619, 567.59375);
        shape1.closePath();
        g.setPaint(new LinearGradientPaint(new Point2D.Double(165.31300354003906, 114.6709976196289), new Point2D.Double(165.31300354003906, 76.6259994506836), new float[]{0.0f, 1.0f}, new Color[]{new Color(106, 163, 213, 255), new Color(80, 118, 177, 255)}, MultipleGradientPaint.CycleMethod.NO_CYCLE, MultipleGradientPaint.ColorSpaceType.SRGB, new AffineTransform(3.884321928024292f, 0.0f, 0.0f, 3.8868143558502197f, -500.1821594238281f, 269.76171875f)));
        g.fill(shape1);
    }

    private void paintShapeNode_0_0_0_1_0_1(Graphics2D g) {
        GeneralPath shape2 = new GeneralPath();
        shape2.moveTo(95.71875, 567.5625);
        shape2.curveTo(73.93974, 567.6068, 57.232117, 576.49146, 43.0625, 590.40625);
        shape2.curveTo(29.320934, 604.6219, 20.997658, 624.08356, 21.34375, 645.40625);
        shape2.curveTo(21.851818, 676.7239, 40.91742, 703.3642, 67.875, 715.03125);
        shape2.lineTo(89.09375, 665.90625);
        shape2.curveTo(81.10959, 661.90576, 75.625, 653.6321, 75.625, 644.09375);
        shape2.curveTo(75.625, 632.1528, 84.22493, 622.2114, 95.5625, 620.125);
        shape2.lineTo(95.71875, 567.5625);
        shape2.closePath();
        g.setPaint(new LinearGradientPaint(new Point2D.Double(143.79100036621094, 114.55899810791016), new Point2D.Double(143.79100036621094, 76.61710357666016), new float[]{0.0f, 1.0f}, new Color[]{new Color(99, 186, 112, 255), new Color(29, 168, 56, 255)}, MultipleGradientPaint.CycleMethod.NO_CYCLE, MultipleGradientPaint.ColorSpaceType.SRGB, new AffineTransform(3.884321928024292f, 0.0f, 0.0f, 3.8868143558502197f, -500.1821594238281f, 269.76171875f)));
        g.fill(shape2);
    }

    private void paintShapeNode_0_0_0_1_0_2(Graphics2D g) {
        RoundRectangle2D.Double shape3 = new RoundRectangle2D.Double(129.79100036621094, 71.53150177001953, 49.44599914550781, 49.50519943237305, 20.475799560546875, 20.475799560546875);
        g.setPaint(new Color(80, 118, 177, 255));
        g.setStroke(new BasicStroke(2.0f, 0, 0, 4.0f, null, 0.0f));
        g.draw(shape3);
    }

    private void paintCompositeGraphicsNode_0_0_0_1_0(Graphics2D g) {
        // _0_0_0_1_0_0
        AffineTransform trans_0_0_0_1_0_0 = g.getTransform();
        g.transform(new AffineTransform(0.25744518637657166f, 0.0f, 0.0f, 0.2572801113128662f, 128.7694854736328f, -69.40432739257812f));
        paintShapeNode_0_0_0_1_0_0(g);
        g.setTransform(trans_0_0_0_1_0_0);
        // _0_0_0_1_0_1
        AffineTransform trans_0_0_0_1_0_1 = g.getTransform();
        g.transform(new AffineTransform(0.25744518637657166f, 0.0f, 0.0f, 0.2572801113128662f, 128.7694854736328f, -69.40432739257812f));
        paintShapeNode_0_0_0_1_0_1(g);
        g.setTransform(trans_0_0_0_1_0_1);
        // _0_0_0_1_0_2
        AffineTransform trans_0_0_0_1_0_2 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
        paintShapeNode_0_0_0_1_0_2(g);
        g.setTransform(trans_0_0_0_1_0_2);
    }

    private void paintCompositeGraphicsNode_0_0_0_1(Graphics2D g) {
        // _0_0_0_1_0
        AffineTransform trans_0_0_0_1_0 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
        paintCompositeGraphicsNode_0_0_0_1_0(g);
        g.setTransform(trans_0_0_0_1_0);
    }

    private void paintCompositeGraphicsNode_0_0_0(Graphics2D g) {
        // _0_0_0_0
        AffineTransform trans_0_0_0_0 = g.getTransform();
        g.transform(new AffineTransform(1.0808300971984863f, 0.0f, 0.0f, 1.0815237760543823f, -138.87091064453125f, 77.60364532470703f));
        paintCompositeGraphicsNode_0_0_0_0(g);
        g.setTransform(trans_0_0_0_0);
        // _0_0_0_1
        AffineTransform trans_0_0_0_1 = g.getTransform();
        g.transform(new AffineTransform(1.0962419509887695f, 0.0f, 0.0f, 1.0969454050064087f, -141.1625213623047f, 76.13275909423828f));
        paintCompositeGraphicsNode_0_0_0_1(g);
        g.setTransform(trans_0_0_0_1);
    }

    private void paintCanvasGraphicsNode_0_0(Graphics2D g) {
        // _0_0_0
        AffineTransform trans_0_0_0 = g.getTransform();
        g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, -0.02323218062520027f, -153.50230407714844f));
        paintCompositeGraphicsNode_0_0_0(g);
        g.setTransform(trans_0_0_0);
    }

    private void paintRootGraphicsNode_0(Graphics2D g) {
        // _0_0
        g.setComposite(AlphaComposite.getInstance(3, 1.0f * origAlpha));
        AffineTransform trans_0_0 = g.getTransform();
        g.transform(new AffineTransform(3.5399677753448486f, 0.0f, 0.0f, 3.5399677753448486f, 0.17650954739201552f, -0.0f));
        paintCanvasGraphicsNode_0_0(g);
        g.setTransform(trans_0_0);
    }

    /**
     * Returns the X of the bounding box of the original SVG image.
     *
     * @return The X of the bounding box of the original SVG image.
     */
    public int getOrigX() {
        return 1;
    }

    /**
     * Returns the Y of the bounding box of the original SVG image.
     *
     * @return The Y of the bounding box of the original SVG image.
     */
    public int getOrigY() {
        return 0;
    }

    /**
     * Returns the width of the bounding box of the original SVG image.
     *
     * @return The width of the bounding box of the original SVG image.
     */
    public int getOrigWidth() {
        return 200;
    }

    /**
     * Returns the height of the bounding box of the original SVG image.
     *
     * @return The height of the bounding box of the original SVG image.
     */
    public int getOrigHeight() {
        return 201;
    }
    /**
     * The current width of this resizable icon.
     */
    int width;
    /**
     * The current height of this resizable icon.
     */
    int height;

    /**
     * Creates a new transcoded SVG image.
     */
    public OecLogoBgWhite() {
        this.width = getOrigWidth();
        this.height = getOrigHeight();
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.Icon#getIconHeight()
     */
    @Override
    public int getIconHeight() {
        return height;
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.Icon#getIconWidth()
     */
    @Override
    public int getIconWidth() {
        return width;
    }

    /*
     * Set the dimension of the icon.
     */
    public void setDimension(Dimension newDimension) {
        this.width = newDimension.width;
        this.height = newDimension.height;
    }

    /*
     * (non-Javadoc)
     * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
     */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(x, y);

        double coef1 = (double) this.width / (double) getOrigWidth();
        double coef2 = (double) this.height / (double) getOrigHeight();
        double coef = Math.min(coef1, coef2);
        g2d.scale(coef, coef);
        paint(g2d);
        g2d.dispose();
    }
}

