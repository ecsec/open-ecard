package org.openecard.client.gui.swing;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Exchanger;
import org.openecard.client.gui.ResultStatus;
import org.openecard.client.gui.StepResult;
import org.openecard.client.gui.definition.OutputInfoUnit;


/**
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SwingStepResult implements StepResult {

    public Exchanger syncPoint = new Exchanger();
    private String id;
    private ResultStatus status;
    private List<OutputInfoUnit> results;

    public SwingStepResult(String id) {
	this(id, null);
    }

    public SwingStepResult(String id, ResultStatus status) {
	this.id = id;
	this.status = status;
    }

    public void setResultStatus(ResultStatus status) {
	this.status = status;
    }

    public void setResult(List<OutputInfoUnit> results) {
	this.results = results;
    }

    @Override
    public String getStepID() {
	return id;
    }

    @Override
    public ResultStatus getStatus() {
	synchronize();
	return status;
    }

    @Override
    public boolean isOK() {
	// Warum muss ich das machen?
	synchronize();
	synchronized (this) {
	    return getStatus() == ResultStatus.OK;
	}
    }

    @Override
    public boolean isBack() {
	// Warum muss ich das machen?
	synchronize();
	synchronized (this) {
	    return getStatus() == ResultStatus.BACK;
	}
    }

    @Override
    public boolean isCancelled() {
	// Warum muss ich das machen?
	synchronize();
	synchronized (this) {
	    return getStatus() == ResultStatus.CANCEL;
	}
    }

    @Override
    public List<OutputInfoUnit> getResults() {
	synchronize();
	synchronized (this) {
	    if (results == null) {
		results = Collections.unmodifiableList(new LinkedList());
	    }
	    return results;
	}
    }

    private void synchronize() {
	if (status == null) {
	    try {
		syncPoint.exchange(null);
	    } catch (InterruptedException ignore) {
	    }
	}
    }
}
