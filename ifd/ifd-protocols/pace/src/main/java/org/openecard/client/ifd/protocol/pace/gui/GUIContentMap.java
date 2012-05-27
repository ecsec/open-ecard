package org.openecard.client.ifd.protocol.pace.gui;

import java.util.HashMap;


/**
 * Provides a container to store data for the GUI and user interaction.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class GUIContentMap {

    public enum ELEMENT {

	PIN,
	PIN_ID;
    }

    private HashMap<String, Object> map = new HashMap<String, Object>();

    /**
     * Creates a new GUI Content map.
     */
    public GUIContentMap() {
    }

    /**
     * Adds a new value to the GUI content map.
     *
     * @param id ID
     * @param value Value
     */
    public void add(ELEMENT id, Object value) {
	add(id.name(), value);
    }

    /**
     * Adds a new value to the GUI content map.
     *
     * @param id ID
     * @param value Value
     */
    public void add(String id, Object value) {
	map.put(id, value);
    }

    /**
     * Returns the corresponding value.
     *
     * @param id ID
     * @return Value
     */
    public Object get(ELEMENT id) {
	return get(id.name());
    }

    /**
     * Returns the corresponding value.
     *
     * @param id ID
     * @return Value
     */
    public Object get(String id) {
	return map.get(id);
    }
}
