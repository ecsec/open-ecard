/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openecard.client.ifd.protocol.pace.gui;

import java.util.HashMap;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class GUIContentMap {

    public enum ELEMENT {

	PIN,
	PIN_TYPE;
    }

    private HashMap<String, Object> map = new HashMap<String, Object>();

    public GUIContentMap() {
    }

    public void add(ELEMENT id, Object value) {
	add(id.name(), value);
    }

    public void add(String id, Object value) {
	map.put(id, value);
    }

    public Object get(ELEMENT id) {
	return get(id.name());
    }

    public Object get(String id) {
	return map.get(id);
    }
}
