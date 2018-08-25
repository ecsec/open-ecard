/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.gui.android;

import java.util.List;
import javax.annotation.Nullable;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.Step;


/**
 *
 * @author Tobias Wich
 */
public class AndroidResult implements StepResult {

    private final Step step;
    private final ResultStatus status;
    private final List<OutputInfoUnit> outputUnits;
    private final Step replacement;

    public AndroidResult(Step step, ResultStatus status, List<OutputInfoUnit> outputUnits) {
	this(step, status, outputUnits, null);
    }

    public AndroidResult(Step step, ResultStatus status, List<OutputInfoUnit> outputUnits, @Nullable Step replacement) {
	this.step = step;
	this.status = status;
	this.outputUnits = outputUnits;
	this.replacement = replacement;
    }

    @Override
    public Step getStep() {
	return step;
    }

    @Override
    public String getStepID() {
	return step.getID();
    }

    @Override
    public ResultStatus getStatus() {
	return status;
    }

    @Override
    public boolean isOK() {
	return getStatus() == ResultStatus.OK;
    }

    @Override
    public boolean isBack() {
	return getStatus() == ResultStatus.BACK;
    }

    @Override
    public boolean isCancelled() {
	return getStatus() == ResultStatus.CANCEL;
    }

    @Override
    public boolean isReload() {
	return getStatus() == ResultStatus.RELOAD;
    }

    @Override
    public List<OutputInfoUnit> getResults() {
	return outputUnits;
    }

    @Override
    public Step getReplacement() {
	return replacement;
    }

}
