/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.addon;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Extends Timer to make it reschedulable.
 * @author Dirk Petrautzki
 *
 */
class ReschedulableTimer extends Timer {

    private Runnable task;
    private TimerTask timerTask;

    /**
     * Schedules the specified task for execution after the specified delay.
     * @param runnable the task to execute after the delay
     * @param delay execution delay in milliseconds
     */
    public void schedule(Runnable runnable, long delay) {
	task = runnable;

	timerTask = new TimerTask() {
	    @Override
	    public void run() {
			if(task != null) {
				task.run();
			}
	    };
	};

	super.schedule(timerTask, delay);
    }

    /**
     * Reschedule the timer with a new delay.
     * @param delay reschedule delay in milliseconds
     */
    public void reschedule(long delay) {
	timerTask.cancel();

	timerTask = new TimerTask() {
	    @Override
	    public void run() {
			if(task != null) {
				task.run();
			}
	    };
	};

	super.schedule(timerTask, delay);
    }

}
