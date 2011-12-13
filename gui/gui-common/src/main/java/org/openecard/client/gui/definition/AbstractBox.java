package org.openecard.client.gui.definition;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public abstract class AbstractBox implements InputInfoUnit, OutputInfoUnit {

    private String groupText;
    private List<BoxItem> boxItems;

    /**
     * @return the groupText
     */
    public String getGroupText() {
	return groupText;
    }

    /**
     * @param groupText the groupText to set
     */
    public void setGroupText(String groupText) {
	this.groupText = groupText;
    }

    public List<BoxItem> getBoxItems() {
	if (boxItems == null) {
	    boxItems = new ArrayList<BoxItem>();
	}
	return boxItems;
    }

}
