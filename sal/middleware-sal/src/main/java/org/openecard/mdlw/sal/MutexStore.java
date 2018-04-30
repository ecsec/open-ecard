/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.mdlw.sal;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.openecard.mdlw.sal.cryptoki.CryptokiLibrary;


/**
 *
 * @author Tobias Wich
 */
public class MutexStore {

    private final HashMap<Long, ReentrantLock> entries = new HashMap<>();
    private long mutexNums = 1;

    public CryptokiLibrary.CK_CREATEMUTEX getCreateMutexFun() {
	return new CryptokiLibrary.CK_CREATEMUTEX() {
	    @Override
	    public synchronized NativeLong apply(PointerByReference ppMutex) {
		// create mutex index number
		long mutexIdx = mutexNums++;
		Pointer mutexRef = Pointer.createConstant(mutexIdx);
		// add entry
		ReentrantLock mutex = new ReentrantLock();
		entries.put(mutexIdx, mutex);
		// give handle to the caller
		ppMutex.setValue(mutexRef);
		return new NativeLong(CryptokiLibrary.CKR_OK);
	    }
	};
    }

    public CryptokiLibrary.CK_DESTROYMUTEX getDestroyMutexFun() {
	return new CryptokiLibrary.CK_DESTROYMUTEX() {
	    @Override
	    public synchronized NativeLong apply(Pointer pMutex) {
		long mutexIdx = Pointer.nativeValue(pMutex);
		ReentrantLock mutex = entries.remove(mutexIdx);
		if (mutex != null) {
		    return new NativeLong(CryptokiLibrary.CKR_OK);
		} else {
		    return new NativeLong(CryptokiLibrary.CKR_MUTEX_BAD);
		}
	    }
	};
    }

    public CryptokiLibrary.CK_LOCKMUTEX getLockMutexFun() {
	return new CryptokiLibrary.CK_LOCKMUTEX() {
	    @Override
	    public NativeLong apply(Pointer pMutex) {
		long mutexIdx = Pointer.nativeValue(pMutex);
		ReentrantLock mutex = entries.get(mutexIdx);
		if (mutex != null) {
		    mutex.lock();
		    return new NativeLong(CryptokiLibrary.CKR_OK);
		} else {
		    return new NativeLong(CryptokiLibrary.CKR_MUTEX_BAD);
		}
	    }
	};
    }

    public CryptokiLibrary.CK_UNLOCKMUTEX getUnlockMutexFun() {
	return new CryptokiLibrary.CK_UNLOCKMUTEX() {
	    @Override
	    public NativeLong apply(Pointer pMutex) {
		long mutexIdx = Pointer.nativeValue(pMutex);
		ReentrantLock mutex = entries.get(mutexIdx);
		if (mutex != null) {
		    try {
			mutex.unlock();
			return new NativeLong(CryptokiLibrary.CKR_OK);
		    } catch (IllegalMonitorStateException ex) {
			return new NativeLong(CryptokiLibrary.CKR_GENERAL_ERROR);
		    }
		} else {
		    return new NativeLong(CryptokiLibrary.CKR_MUTEX_BAD);
		}
	    }
	};
    }

}
