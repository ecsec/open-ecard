package org.openecard.client.gui.definition;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Step {

    private String name;
    private boolean reversible=true;
    private boolean instantReturn=false;
    private List<InputInfoUnit> inputInfoUnits;

    public Step(String name) {
	this.name = name;
    }


    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public boolean isReversible() {
	return reversible;
    }

    public void setReversible(boolean reversible) {
	this.reversible = reversible;
    }

    public boolean isInstantReturn() {
	return instantReturn;
    }

    public void setInstantReturn(boolean instantReturn) {
	this.instantReturn = instantReturn;
    }

    public List<InputInfoUnit> getInputInfoUnits() {
	if (inputInfoUnits == null) {
	    inputInfoUnits = new ArrayList<InputInfoUnit>();
	}
	return inputInfoUnits;
    }

    public boolean isMetaStep() {
	return getInputInfoUnits().isEmpty();
    }

}
