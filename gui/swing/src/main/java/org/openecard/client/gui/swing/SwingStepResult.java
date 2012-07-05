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
