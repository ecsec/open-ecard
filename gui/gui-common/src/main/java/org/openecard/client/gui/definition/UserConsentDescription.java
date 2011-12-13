package org.openecard.client.gui.definition;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class UserConsentDescription {

    private String title;
    private String dialogType;
    private ArrayList<Step> steps;

    public UserConsentDescription(String title, String dialogType) {
	this.title = title;
	this.dialogType = dialogType;
    }

    public UserConsentDescription(String title) {
	this.title = title;
    }


    /**
     * @return the title
     */
    public String getTitle() {
	return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
	this.title = title;
    }

    /**
     * @return the dialogType
     */
    public String getDialogType() {
	return dialogType;
    }

    /**
     * @param dialogType the dialogType to set
     */
    public void setDialogType(String dialogType) {
	this.dialogType = dialogType;
    }

    public List<Step> getSteps() {
	if (steps == null) {
	    steps = new ArrayList<Step>();
	}
	return steps;
    }

}
