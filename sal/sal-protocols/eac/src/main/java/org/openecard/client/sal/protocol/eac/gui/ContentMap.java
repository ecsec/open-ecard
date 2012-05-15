/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openecard.client.sal.protocol.eac.gui;

import java.util.HashMap;

/**
 *
 * @author John
 */
public class ContentMap {

    public enum ELEMENT {

	CERTIFICATE,
	CERTIFICATEDESCRIPTION,
	REQUIRED_CHAT,
	OPTIONAL_CHAT;
    }
    private HashMap<String, Object> map = new HashMap<String, Object>();

    public ContentMap() {
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
