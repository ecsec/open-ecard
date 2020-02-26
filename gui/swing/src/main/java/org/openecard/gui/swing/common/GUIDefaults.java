/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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

package org.openecard.gui.swing.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import org.openecard.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public class GUIDefaults {

    private static final Logger LOG = LoggerFactory.getLogger(GUIDefaults.class.getName());

    // Regex pattern for hex colors
    private static final String HEX_PATTERN = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile(HEX_PATTERN);
    // Swing UIDefaults
    private static final UIDefaults DEFAULTS = UIManager.getDefaults();
    private static final UIDefaults OWN_DEFAULTS = new UIDefaults();
    private static final ArrayList<String> COLOR_PROPERTIES = new ArrayList<String>() {
	private static final long serialVersionUID = 1L;
	{
	    add("foreground");
	    add("background");
	    add("selectionBackground");
	    add("selectionForeground");
	    add("disabledText");
	}
    };
    private static final ArrayList<String> FONT_PROPERTIES = new ArrayList<String>() {
	private static final long serialVersionUID = 1L;
	{
	    add("font");
	    add("titleFont");
	    add("acceleratorFont");
	}
    };
    private static final ArrayList<String> ICON_PROPERTIES = new ArrayList<String>() {
	private static final long serialVersionUID = 1L;
	{
	    add("icon");
	    add("selectedIcon");
	    add("disabledIcon");
	    add("disabledSelectedIcon");
	}
    };

    private static Object getProperty(String identifier) {
	return OWN_DEFAULTS.get(identifier);
    }

    public static Color getColor(String identifier) {
	Color color = (Color) getProperty(identifier);

	if (color == null) {
	    return Color.WHITE;
	}

	return color;
    }

    public static Font getFont(String identifier) {
	Font font = (Font) getProperty(identifier);

	if (font == null) {
	    return new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	}

	return font;
    }

    public static ImageIcon getImage(String identifier, int width, int height) {
	ImageIcon icon = (ImageIcon) getProperty(identifier);

	if (width > -1 || height > -1) {
	    Image image = icon.getImage();
	    image = image.getScaledInstance(width, height,  Image.SCALE_SMOOTH);
	    icon = new ImageIcon(image);
	}

	return icon;
    }

    public static ImageIcon getImage(String identifier) {
	return getImage(identifier, -1, -1);
    }

    public static InputStream getImageStream(String identifier, int width, int height) {
	return getImageStream(getImage(identifier, width, height));
    }

    public static InputStream getImageStream(String identifier) {
	return getImageStream(getImage(identifier));
    }

    private static InputStream getImageStream(ImageIcon icon) {
	BufferedImage bi = new BufferedImage(
		icon.getIconWidth(),
		icon.getIconHeight(),
		BufferedImage.TYPE_INT_ARGB);
	Graphics g = bi.createGraphics();
	// paint the Icon to the BufferedImage
	icon.paintIcon(null, g, 0, 0);
	g.dispose();

	try {
	    ByteArrayOutputStream os = new ByteArrayOutputStream();
	    ImageIO.write(bi, "PNG", os);
	    InputStream is = new ByteArrayInputStream(os.toByteArray());

	    return is;
	} catch (IOException ex) {
	    throw new IllegalArgumentException("Failed to convert image to PNG.");
	}
    }

    public static void initialize() {
	try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

	    Toolkit toolkit = Toolkit.getDefaultToolkit();
	    GUIProperties guiProps = new GUIProperties();
	    Properties props = guiProps.properties();

	    for (String property : props.stringPropertyNames()) {
		try {
		    String propertyName = property.substring(0, property.indexOf('.'));
		    String propertyAttribute = property.substring(propertyName.length() + 1, property.length());
		    String value = (String) props.getProperty(property);

		    // Parse color property
		    if (COLOR_PROPERTIES.contains(propertyAttribute)) {
			validateHexColor(value);
			if (value.length() == 4) {
			    StringBuilder sb = new StringBuilder("#");
			    for (int i = 1; i < value.length(); i++) {
				sb.append(value.substring(i, i + 1));
				sb.append(value.substring(i, i + 1));
			    }
			    value = sb.toString();
			}
			Color color = Color.decode(value);
			DEFAULTS.put(property, color);
			OWN_DEFAULTS.put(property, color);
		    } // Parse font propertiy
		    else if (FONT_PROPERTIES.contains(propertyAttribute)) {
			Font font = Font.decode(value);
			DEFAULTS.put(property, font);
			OWN_DEFAULTS.put(property, font);
		    }// Parse icon propertiy
		    else if (ICON_PROPERTIES.contains(propertyAttribute)) {
			URL url = FileUtils.resolveResourceAsURL(guiProps.getClass(), value);
			if (url == null) {
			    LOG.error("Cannot parse the property: {}", property);
			} else {
			    Image image = toolkit.getImage(url);
			    ImageIcon icon = new ImageIcon(image);
			    DEFAULTS.put(property, icon);
			    OWN_DEFAULTS.put(property, icon);
			}
		    }
		} catch (Exception e) {
		    LOG.error("Cannot parse the property: {}", property);
		}
	    }
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	}
    }

    private static void validateHexColor(String hex) throws IllegalArgumentException {
	if (! HEX_COLOR_PATTERN.matcher(hex).matches()) {
	    throw new IllegalArgumentException();
	}
    }

}
