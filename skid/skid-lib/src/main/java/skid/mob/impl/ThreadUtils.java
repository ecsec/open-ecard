/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.impl;

/**
 *
 * @author Tobias Wich
 */
public class ThreadUtils {

    public static void ifNotInterrupted(VoidCallback cb) {
	if (! Thread.currentThread().isInterrupted()) {
	    cb.fun();
	}
    }

    public static interface VoidCallback {
	void fun();
    }

}
