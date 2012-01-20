/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.gui.android;

import android.content.Context;
import android.content.Intent;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Exchanger;
import org.openecard.client.gui.ResultStatus;
import org.openecard.client.gui.StepResult;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.OutputInfoUnit;
import org.openecard.client.gui.definition.Step;

/**
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 *
 */
public class AndroidNavigator implements UserConsentNavigator {

	private int curStep = -1;
	private final int numSteps;
	private Context context;
	private StepActivity activity;
	public static List<Step> steps;
	public static AndroidStepResult stepResult;
	private static AndroidNavigator instance = null;

	public static AndroidNavigator getInstance() {
		return instance;
	}

	public AndroidNavigator(List<Step> s, Context context) {
		instance = this;
		steps = s;
		numSteps = steps.size();
		this.context = context;
		stepResult = new AndroidStepResult();
		Intent i = new Intent(this.context, StepActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
		while (!(activity instanceof StepActivity)) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setActivity(StepActivity activity) {
		this.activity = activity;
	}
	
	@Override
	public void close() {
		activity.finish();
	}

	public void setStepResult(boolean back, boolean cancelled, List<OutputInfoUnit> results) throws InterruptedException {
		AndroidNavigator.stepResult.done = true;
		stepResult.back = back;
		stepResult.cancelled = cancelled;
		stepResult.results = results;
		AndroidNavigator.stepResult.syncPoint.exchange(null);
	}

	public StepResult getStepResult() {
		return stepResult;
	}

	@Override
	public StepResult current() {
		AndroidNavigator.stepResult.done = false;
		activity.showStep(steps.get(curStep));
		return this.getStepResult();
	}

	@Override
	public boolean hasNext() {
		return curStep < numSteps - 1;
	}

	@Override
	public StepResult next() {
		AndroidNavigator.stepResult.done = false;
		curStep++;
		activity.showStep(steps.get(curStep));
		return this.getStepResult();
	}

	@Override
	public StepResult previous() {
		AndroidNavigator.stepResult.done = false;
		curStep--;
		activity.showStep(steps.get(curStep));
		return this.getStepResult();
	}

	@Override
	public StepResult replaceCurrent(Step arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public StepResult replaceNext(Step arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public StepResult replacePrevious(Step arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	class AndroidStepResult implements StepResult {

		public Exchanger syncPoint = new Exchanger();
		public boolean done = false;
		public boolean cancelled = false;
		public boolean back = false;
		public List<OutputInfoUnit> results = null;

		@Override
		public String stepName() {
			return steps.get(curStep).getName();
		}

		@Override
		public ResultStatus status() {
			if (!done) {
				try {
					syncPoint.exchange(null);
				} catch (InterruptedException ex) {
				}
			}
			// return appropriate result
			synchronized (this) {
				if (cancelled) {
					return ResultStatus.CANCEL;
				} else if (back) {
					return ResultStatus.BACK;
				} else {
					return ResultStatus.OK;
				}
			}
		}

		@Override
		public boolean isOK() {
			if (!done) {
				try {
					syncPoint.exchange(null);
				} catch (InterruptedException ex) {
				}
			}
			synchronized (this) {
				return status() == ResultStatus.OK;
			}
		}

		@Override
		public boolean isBack() {
			if (!done) {
				try {
					syncPoint.exchange(null);
				} catch (InterruptedException ex) {
				}
			}
			synchronized (this) {
				return status() == ResultStatus.BACK;
			}
		}

		@Override
		public boolean isCancelled() {
			if (!done) {
				try {
					syncPoint.exchange(null);
				} catch (InterruptedException ex) {
				}
			}
			synchronized (this) {
				return status() == ResultStatus.CANCEL;
			}
		}

		@Override
		public List<OutputInfoUnit> results() {
			if (!done) {
				try {
					syncPoint.exchange(null);
				} catch (InterruptedException ex) {
				}
			}
			synchronized (this) {
				if (results == null) {
					results = Collections.unmodifiableList(stepResult.results);
				}
				return results;
			}
		}
	}
}
