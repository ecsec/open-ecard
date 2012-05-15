package org.openecard.client.gui.swing;

import java.awt.Color;
import java.awt.Font;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class GUIDefaults {

    private static UIDefaults defaults = UIManager.getDefaults();

    public static Color getColor(String identifier) {
	Color color = (Color) get(identifier);

	if (color == null) {
	    return Color.WHITE;
	}

	return color;
    }

    public static Font getFont(String identifier) {
	Font font = (Font) get(identifier);

	if (font == null) {
	    return new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	}

	return font;
    }

    private static void initialize() {
    }

    private static Object get(String identifier) {
	return defaults.get(identifier);
    }
}
