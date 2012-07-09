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

package org.openecard.client.gui.swing.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class GUIDefaults {

    private static final Logger logger = LoggerFactory.getLogger(GUIDefaults.class.getName());
    // Regex pattern for hex colors
    private static final String HEX_PATTERN = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
    private static final Pattern hexColorPattern = Pattern.compile(HEX_PATTERN);
    // Swing UIDefaults
    private static final UIDefaults defaults = UIManager.getDefaults();
    private static ArrayList<String> colorProperties = new ArrayList<String>() {

	{
	    add("foreground");
	    add("background");
	    add("selectionBackground");
	    add("selectionForeground");
	    add("disabledText");
	}
    };
    private static ArrayList<String> fontProperties = new ArrayList<String>() {

	{
	    add("font");
	    add("titleFont");
	    add("acceleratorFont");
	}
    };
    private static ArrayList<String> iconProperties = new ArrayList<String>() {

	{
	    add("icon");
	    add("selectedIcon");
	    add("disabledIcon");
	    add("disabledSelectedIcon");
	}
    };

    private static Object getProperty(String identifier) {
	return defaults.get(identifier);
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
	Object obj = getProperty(identifier);
	if (obj instanceof ImageIcon) {
	    return (ImageIcon) obj;
	} else if (obj instanceof URL || obj instanceof String) {
	    ImageIcon logo = new ImageIcon();
	    Toolkit toolkit = Toolkit.getDefaultToolkit();
	    Image image;
	    if (obj instanceof URL) {
		image = toolkit.getImage((URL)obj);
	    } else {
		image = toolkit.getImage((String)obj);
	    }
	    if (width > -1) {
		image = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
	    }
	    logo.setImage(image);
	    return logo;
	} else {
	    return null;
	}
    }

    public static ImageIcon getImage(String identifier) {
	return getImage(identifier, -1, -1);
    }

    public static void initialize() {
	try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

	    GUIProperties guiProps = new GUIProperties();
	    Properties props = guiProps.properties();

	    for (Map.Entry<Object, Object> entry : props.entrySet()) {
		String property = (String) entry.getKey();

		try {
		    String propertyName = property.substring(0, property.indexOf("."));
		    String propertyAttribute = property.substring(propertyName.length() + 1, property.length());
		    String value = (String) entry.getValue();

		    // Parse color property
		    if (colorProperties.contains(propertyAttribute)) {
			validateHexColor(value);
			if (value.length() == 4) {
			    StringBuilder sb = new StringBuilder("#");
			    for (int i = 1; i < value.length(); i++) {
				sb.append(value.substring(i, i + 1));
				sb.append(value.substring(i, i + 1));
			    }
			    value = sb.toString();
			}
			defaults.put(property, Color.decode(value));
		    } // Parse font propertiy
		    else if (fontProperties.contains(propertyAttribute)) {
			defaults.put(property, Font.decode(value));
		    }// Parse icon propertiy
		    else if (iconProperties.contains(propertyAttribute)) {
			URL url = guiProps.getDependentResource(value);
			if (url == null) {
			    logger.error("Cannot parse the property: " + property);
			} else {
			    defaults.put(property, url);
			}
		    }
		} catch (Exception e) {
		    logger.error("Cannot parse the property: " + property);
		}
	    }
	} catch (Exception e) {
	    logger.error(e.getMessage());
	}
    }

    private static void validateHexColor(String hex) throws IllegalArgumentException {
	if (!hexColorPattern.matcher(hex).matches()) {
	    throw new IllegalArgumentException();
	}
    }

}
