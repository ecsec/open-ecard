/****************************************************************************
 * Copyright (C) 2015-2016 ecsec GmbH.
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

import org.openecard.mdlw.sal.config.MiddlewareSALConfig;
import org.openecard.mdlw.sal.struct.CkInfo;
import org.openecard.mdlw.sal.struct.CkAttribute;
import org.openecard.mdlw.sal.struct.CkSlot;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;
import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;
import javax.annotation.Nullable;
import org.openecard.common.ThreadTerminateException;
import org.openecard.crypto.common.UnsupportedAlgorithmException;
import org.openecard.mdlw.sal.exceptions.InvalidArgumentsException;
import org.openecard.mdlw.sal.exceptions.AuthenticationException;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.openecard.mdlw.sal.exceptions.DataInvalidException;
import org.openecard.mdlw.sal.exceptions.PinIncorrectException;
import org.openecard.mdlw.sal.exceptions.SessionException;
import org.openecard.mdlw.sal.exceptions.TokenException;
import org.openecard.mdlw.sal.cryptoki.CK_ATTRIBUTE;
import org.openecard.mdlw.sal.cryptoki.CK_C_INITIALIZE_ARGS;
import org.openecard.mdlw.sal.cryptoki.CK_INFO;
import org.openecard.mdlw.sal.cryptoki.CK_MECHANISM;
import org.openecard.mdlw.sal.cryptoki.CK_MECHANISM_INFO;
import org.openecard.mdlw.sal.cryptoki.CK_SESSION_INFO;
import org.openecard.mdlw.sal.cryptoki.CK_SLOT_INFO;
import org.openecard.mdlw.sal.cryptoki.CK_TOKEN_INFO;
import org.openecard.mdlw.sal.cryptoki.CryptokiLibrary;
import org.openecard.mdlw.sal.exceptions.AlreadyInitializedException;
import org.openecard.mdlw.sal.exceptions.CancellationException;
import org.openecard.mdlw.sal.exceptions.CryptographicException;
import org.openecard.mdlw.sal.exceptions.GeneralError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 * @author Jan Mannsbart
 */
public class MiddleWareWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(MiddleWareWrapper.class);

    private final CryptokiLibrary lib;
    private static int libIdx = 0;

    private final Semaphore threadLock;

    public MiddleWareWrapper(MiddlewareSALConfig mwSALConfig) throws UnsatisfiedLinkError {
        String libName = mwSALConfig.getMiddlewareSpec().getLibName();

	String osName = System.getProperty("os.name", "");
	String arch = System.getProperty("os.arch", "");

        for (String searchPath : mwSALConfig.getSearchPaths()) {
            NativeLibrary.addSearchPath(libName, searchPath);
        }

	// paths on windows
	if (osName.startsWith("Windows")) {
	    if ("x86".equals(arch)) {
                for (String x32SearchPath : mwSALConfig.getX32SearchPaths()) {
                    NativeLibrary.addSearchPath(libName, x32SearchPath);
                }
	    } else if ("amd64".equals(arch)) {
                for (String x64SearchPath : mwSALConfig.getX64SearchPaths()) {
                    NativeLibrary.addSearchPath(libName, x64SearchPath);
                }
	    }
	}

	HashMap<String, Object> options = new HashMap<>();
	options.put("lib-index", libIdx++);

        lib = (CryptokiLibrary) Native.loadLibrary(libName, CryptokiLibrary.class, options);

	threadLock = new Semaphore(1, true);
    }

    private LockedObject lockInternal() throws InterruptedException {
	try {
	    threadLock.acquire();
	    return new LockedObject();
	} catch (InterruptedException ex) {
	    throw new ThreadTerminateException("Waiting for middleware mutex failed.");
	}
    }

    public LockedMiddlewareWrapper lock() throws InterruptedException {
	threadLock.acquire();
	return new LockedMiddlewareWrapper();
    }

    public void initialize(@Nullable CK_C_INITIALIZE_ARGS arg) throws CryptokiException {
	Pointer p = null;
	if (arg != null) {
	    p = arg.getPointer();
	    arg.write();
	}

	check("C_Initialize", lib.C_Initialize(p));
    }

    public void initialize() throws CryptokiException {
	try {
	    CK_C_INITIALIZE_ARGS arg = new CK_C_INITIALIZE_ARGS();
	    arg.setFlags(CryptokiLibrary.CKF_OS_LOCKING_OK);

	    initialize(arg);

	    return;
	} catch (CryptokiException ex) {
	    LOG.warn("Failed to initialize middleware to perform locking by itself.");
	}

	try {
	    CK_C_INITIALIZE_ARGS arg = new CK_C_INITIALIZE_ARGS();
	    arg.setFlags(CryptokiLibrary.CKF_OS_LOCKING_OK);

	    MutexStore mutexStore = new MutexStore();
	    arg.setCreateMutex(mutexStore.getCreateMutexFun());
	    arg.setDestroyMutex(mutexStore.getDestroyMutexFun());
	    arg.setLockMutex(mutexStore.getLockMutexFun());
	    arg.setUnlockMutex(mutexStore.getUnlockMutexFun());

	    initialize(arg);

	    return;
	} catch (CryptokiException ex) {
	    LOG.warn("Failed to initialize middleware to perform locking with Java locks.");
	}

	LOG.warn("Initializing middleware without thread safety values.");
	initialize(null);
    }

    public void destroy(@Nullable Pointer arg) throws CryptokiException {
	check("C_Finalize", lib.C_Finalize(arg));
    }

    public void destroy() throws CryptokiException {
        destroy(Pointer.NULL);
    }

    public CkInfo getInfo() throws CryptokiException {
	try (LockedObject lo = lockInternal()) {
	    CK_INFO info = new CK_INFO();
	    check("C_GetInfo", lib.C_GetInfo(info));
	    return new CkInfo(info);
	} catch (InterruptedException ex) {
	    throw new IllegalStateException("Failed to release lock for middleware access.");
	}
    }

    public long[] getSlotList(boolean withToken) throws CryptokiException {
	long slotsAvailable = getSlotsAvailable(withToken);

	if (slotsAvailable > 0) {
	    // allocate buffer with size of slots available in the system
	    NativeLongArray slots = new NativeLongArray((int) slotsAvailable);
	    NativeLongByReference slotCountRef = new NativeLongByReference(new NativeLong(slotsAvailable));

	    // includes only those slots with a token present = 0x01
	    byte tokenPresent = (byte) (withToken ? 0x01 : 0x00);

	    try (LockedObject lo = lockInternal()) {
		// get slot list
		check("C_GetSlotList", lib.C_GetSlotList(tokenPresent, slots.getReference(), slotCountRef));

		// create array with size of slots available and fills it with slot
		long[] result = slots.getValues(slotCountRef.getValue().intValue());
		return result;
	    } catch (InterruptedException ex) {
		throw new IllegalStateException("Failed to release lock for middleware access.");
	    }
	} else {
	    return new long[0];
	}
    }

    private long getSlotsAvailable(boolean withToken) throws CryptokiException {
	// init with null to recive number of slots available with or without tokens available
	NativeLongByReference count = new NativeLongByReference();

	// includes only those slots with a token present = 0x01
	byte tokenPresent = (byte) (withToken ? 0x01 : 0x00);

	try (LockedObject lo = lockInternal()) {
	    check("C_GetSlotList", lib.C_GetSlotList(tokenPresent, null, count));

	    // return slot count
	    return count.getValue().longValue();
	} catch (InterruptedException ex) {
	    throw new IllegalStateException("Failed to release lock for middleware access.");
	}
    }

    public CkSlot getSlotInfo(long slotID) throws CryptokiException {
	// init slot info
	CK_SLOT_INFO info = new CK_SLOT_INFO();
	try (LockedObject lo = lockInternal()) {
	    // fill slot info with data recieved from lib
	    check("C_GetSlotInfo", lib.C_GetSlotInfo(new NativeLong(slotID), info));
	    // return new info
	    return new CkSlot(info, slotID);
	} catch (InterruptedException ex) {
	    throw new IllegalStateException("Failed to release lock for middleware access.");
	}
    }

    public MwToken getTokenInfo(long slotID) throws CryptokiException {
	// init token info
	CK_TOKEN_INFO pInfo = new CK_TOKEN_INFO();
	try (LockedObject lo = lockInternal()) {
	    // fill token info
	    check("C_GetTokenInfo", lib.C_GetTokenInfo(new NativeLong(slotID), pInfo));
	    // return new token info
	    return new MwToken(pInfo);
	} catch (InterruptedException ex) {
	    throw new IllegalStateException("Failed to release lock for middleware access.");
	}
    }

    public MwMechanism getMechanismInfo(long slotID, long type) throws CryptokiException {
	// init new mecha info
	CK_MECHANISM_INFO info = new CK_MECHANISM_INFO();
	try (LockedObject lo = lockInternal()) {
	    // fill info
	    check("C_GetMechanismInfo", lib.C_GetMechanismInfo(new NativeLong(slotID), new NativeLong(type), info));
	    // return new mecha info
	    try {
		return new MwMechanism(info, type);
	    } catch (UnsupportedAlgorithmException ex) {
		raiseError(CryptokiLibrary.CKR_MECHANISM_INVALID);
		throw new IllegalStateException("Unreachable code reached.");
	    }
	} catch (InterruptedException ex) {
	    throw new IllegalStateException("Failed to release lock for middleware access.");
	}
    }

    private long getMechanismListCnt(long slotID) throws CryptokiException {
	NativeLongByReference pulCount = new NativeLongByReference(new NativeLong(0));
	try (LockedObject lo = lockInternal()) {
	    // get number
	    check("C_GetMechanismList", lib.C_GetMechanismList(new NativeLong(slotID), null, pulCount));
	    // return number
	    return pulCount.getValue().longValue();
	} catch (InterruptedException ex) {
	    throw new IllegalStateException("Failed to release lock for middleware access.");
	}
    }

    public long[] getMechanismList(long slotID) throws CryptokiException {
	// get number of mechanisms
	long cnt = getMechanismListCnt(slotID);

	if (cnt > 0) {
	    NativeLongArray pMechanismList = new NativeLongArray((int) cnt);
	    NativeLongByReference pulCount = new NativeLongByReference(new NativeLong(cnt));

	    try (LockedObject lo = lockInternal()) {
		check("C_GetMechanismList", lib.C_GetMechanismList(new NativeLong(slotID), pMechanismList.getReference(), pulCount));

		// convert to array
		long[] result = pMechanismList.getValues(pulCount.getValue().intValue());
		return result;
	    } catch (InterruptedException ex) {
		throw new IllegalStateException("Failed to release lock for middleware access.");
	    }
	} else {
	    return new long[0];
	}
    }

    public long openSession(long slotID, long flags) throws CryptokiException {
	// open session to given slot id
	NativeLongByReference session = new NativeLongByReference();
	Pointer pApplication = new Memory(NativeLong.SIZE);

	try (LockedObject lo = lockInternal()) {
	    // open session
	    check("C_OpenSession", lib.C_OpenSession(new NativeLong(slotID), new NativeLong(flags), pApplication, null, session));

	    // return session id
	    return session.getValue().longValue();
	} catch (InterruptedException ex) {
	    throw new IllegalStateException("Failed to release lock for middleware access.");
	}
    }

    public void closeSession(long session) throws CryptokiException {
	try (LockedObject lo = lockInternal()) {
	    // close session
	    check("C_CloseSession", lib.C_CloseSession(new NativeLong(session)));
	} catch (InterruptedException ex) {
	    throw new IllegalStateException("Failed to release lock for middleware access.");
	}
    }

    public CK_SESSION_INFO getSessionInfo(long session) throws CryptokiException {
	// TODO: convert result to own struct
	NativeLong sessionId = new NativeLong(session);
	CK_SESSION_INFO sessionInfo = new CK_SESSION_INFO();
	try (LockedObject lo = lockInternal()) {
	    // close session
	    check("C_CloseSession", lib.C_GetSessionInfo(sessionId, sessionInfo));
	    return sessionInfo;
	} catch (InterruptedException ex) {
	    throw new IllegalStateException("Failed to release lock for middleware access.");
	}
    }

    public void initPin(long hSession, @Nullable byte[] newPin) throws CryptokiException {
	ByteBuffer pinBytes = null;
	NativeLong pinLen = new NativeLong(0);
	if (newPin != null) {
	    pinBytes = ByteBuffer.wrap(newPin);
	    pinLen.setValue(newPin.length);
	}
	try (LockedObject lo = lockInternal()) {
	    // login to session with pin and usertype
	    check("C_InitPIN", lib.C_InitPIN(new NativeLong(hSession), pinBytes, pinLen));
	} catch (InterruptedException ex) {
	    throw new IllegalStateException("Failed to release lock for middleware access.");
	}
    }

    public void setPin(long hSession, @Nullable byte[] oldPin, @Nullable byte[] newPin) throws CryptokiException {
	try (LockedObject lo = lockInternal()) {
	    if (oldPin != null && oldPin.length > 0 && newPin != null && newPin.length > 0) {
		NativeLong oldPinlen = new NativeLong(oldPin.length);
		ByteBuffer oldPinBytes = ByteBuffer.wrap(oldPin);
		NativeLong newPinLen = new NativeLong(newPin.length);
		ByteBuffer newPinBytes = ByteBuffer.wrap(newPin);

		check("C_SetPIN", lib.C_SetPIN(new NativeLong(hSession),
			oldPinBytes, oldPinlen, newPinBytes, newPinLen));
	    } else {
		check("C_SetPIN", lib.C_SetPIN(new NativeLong(hSession),
			(ByteBuffer) null, new NativeLong(0), (ByteBuffer) null, new NativeLong(0)));
	    }
	} catch (InterruptedException ex) {
	    throw new IllegalStateException("Failed to release lock for middleware access.");
	}
    }

    public void login(final long hSession, final long userType, @Nullable byte[] pPin) throws CryptokiException {
	ByteBuffer pinBytesTmp = null;
	final NativeLong pinLen = new NativeLong(0);
	if (pPin != null) {
	    pinBytesTmp = ByteBuffer.wrap(pPin);
	    pinLen.setValue(pPin.length);
	}
	final ByteBuffer pinBytes = pinBytesTmp;

	try (LockedObject lo = lockInternal()) {
	    FutureTask<Void> task = new FutureTask<>(new Callable<Void>() {
		@Override
		public Void call() throws Exception {
		    // login to session with pin and usertype
		    check("C_Login", MiddleWareWrapper.this.lib.C_Login(new NativeLong(hSession),
			    new NativeLong(userType), pinBytes, pinLen), (long) CryptokiLibrary.CKR_OK,
			    (long) CryptokiLibrary.CKR_USER_ALREADY_LOGGED_IN);
		    return null;
		}
	    });

	    Thread t = new Thread(task, "Middleware-Login");
	    t.setDaemon(true);
	    t.start();

	    try {
		task.get();
	    } catch (ExecutionException ex) {
		Throwable cause = ex.getCause();
		if (cause instanceof CryptokiException) {
		    throw (CryptokiException) cause;
		} else if (cause instanceof RuntimeException) {
		    throw (RuntimeException) cause;
		} else {
		    throw new RuntimeException("Unexpected error received during C_Login call.", cause);
		}
	    } catch (InterruptedException ex) {
		task.cancel(true);
		LOG.info("Interrupted while waiting for C_Login task.", ex);
		throw new ThreadTerminateException("Waiting interrupted by an external thread.", ex);
	    }
	} catch (InterruptedException ex) {
	    throw new IllegalStateException("Failed to release lock for middleware access.");
	}
    }

    public void logout(long hSession) throws CryptokiException {
	try (LockedObject lo = lockInternal()) {
	    // logout from session
	    check("C_Logout", lib.C_Logout(new NativeLong(hSession)));
	} catch (InterruptedException ex) {
	    throw new IllegalStateException("Failed to release lock for middleware access.");
	}
    }

    public CkAttribute getAttributeValue(long hSession, long hObject, long type) throws CryptokiException {
	return getAttributeValues(hSession, hObject, type).get(0);
    }

    public List<CkAttribute> getAttributeValues(long hSession, long hObject, long... types) throws CryptokiException {
	CK_ATTRIBUTE baseAttr = new CK_ATTRIBUTE();
	CK_ATTRIBUTE[] attrs = (CK_ATTRIBUTE[]) baseAttr.toArray(types.length);
	for (int i = 0; i < types.length; i++) {
	    CK_ATTRIBUTE attr = attrs[i];
	    attr.setType(new NativeLong(types[i]));
	    attr.setPValue(Pointer.NULL);
	    attr.setUlValueLen(new NativeLong(0));
	}

	try (LockedObject lo = lockInternal()) {
	    // determine size of data to read and allocate space
	    check("C_GetAttributeValue", lib.C_GetAttributeValue(new NativeLong(hSession), new NativeLong(hObject),
		    baseAttr, new NativeLong(attrs.length)));
	    for (CK_ATTRIBUTE next : attrs) {
		long valueLen = next.getUlValueLen().longValue();
		if (valueLen > 0) {
		    Memory newMem = new Memory(valueLen);
		    next.setPValue(newMem);
		}
	    }

	    // read attributes
	    check("C_GetAttributeValue", lib.C_GetAttributeValue(new NativeLong(hSession), new NativeLong(hObject),
		    baseAttr, new NativeLong(attrs.length)));

	    ArrayList<CkAttribute> result = new ArrayList<>();
	    for (CK_ATTRIBUTE next : attrs) {
		CkAttribute resultAttr = new CkAttribute(next.getPValue(), next.getUlValueLen());
		result.add(resultAttr);
	    }
	    return result;
	} catch (InterruptedException ex) {
	    throw new IllegalStateException("Failed to release lock for middleware access.");
	}
    }


    public long waitForSlotEvent(long flags) throws CryptokiException {
	// waiting for a event to happen
	// flags indicates if blocking or non-blocking
	NativeLongByReference pSlot = new NativeLongByReference();

	// LOG.debug("Waiting for slotevent...");

	check("C_WaitForSlotEvent", lib.C_WaitForSlotEvent(new NativeLong(flags), pSlot, Pointer.NULL));

	// return slotid that changed
	return pSlot.getValue().longValue();
    }


    private static void check(String fname, NativeLong result, Long... validResults) throws CryptokiException {
	if (LOG.isDebugEnabled()) {
	    long resultValue = result.longValue();
	    String constantName = CryptokiException.getErrorConstantName(resultValue);
	    LOG.debug("Return code for {}: {} -> {}", fname, String.format("%#08X", resultValue), constantName);
	}


        if (! Arrays.asList(validResults).contains(result.longValue())) {
	    raiseError(result.longValue());
        }
    }

    private static void check(String fname, NativeLong result) throws CryptokiException {
	check(fname, result, (long) CryptokiLibrary.CKR_OK);
    }

    private static void raiseError(long errorCode) throws CryptokiException {
	String msg;
	switch ((int) errorCode) {
	    // AlreadyInitializedException
	    case CryptokiLibrary.CKR_CRYPTOKI_ALREADY_INITIALIZED:
		msg = "The middleware is already initialized.";
		throw new AlreadyInitializedException(msg, errorCode);

	    // GeneralException
	    case CryptokiLibrary.CKR_GENERAL_ERROR:
		msg = "Some unrecoverable error has occurred.";
		throw new GeneralError(msg, errorCode);
	    case CryptokiLibrary.CKR_HOST_MEMORY:
		msg = "The computer has insufficient memory to perform the requested function.";
		throw new GeneralError(msg, errorCode);
	    case CryptokiLibrary.CKR_FUNCTION_FAILED:
		msg = "The requested function could not be performed.";
		throw new GeneralError(msg, errorCode);
	    case CryptokiLibrary.CKR_FUNCTION_NOT_SUPPORTED:
		msg = "Function not supported.";
		throw new GeneralError(msg, errorCode);
	    case CryptokiLibrary.CKR_CRYPTOKI_NOT_INITIALIZED:
		msg = "The middleware is not initialized.";
		throw new GeneralError(msg, errorCode);
	    case CryptokiLibrary.CKR_OPERATION_NOT_INITIALIZED:
		msg = "The operation is not initialized..";
		throw new GeneralError(msg, errorCode);
	    case CryptokiLibrary.CKR_MECHANISM_INVALID:
		msg = "Mechanism invalid.";
		throw new GeneralError(msg, errorCode);

	    // SessionException
	    case CryptokiLibrary.CKR_SESSION_HANDLE_INVALID:
		msg = "The session handle is invalid.";
		throw new SessionException(msg, errorCode);
	    case CryptokiLibrary.CKR_SESSION_CLOSED:
		msg = "The session has been closed during the execution of the function.";
		throw new SessionException(msg, errorCode);
	    case CryptokiLibrary.CKR_SLOT_ID_INVALID:
		msg = "Slot ID is invalid.";
		throw new SessionException(msg, errorCode);
	    case CryptokiLibrary.CKR_KEY_HANDLE_INVALID:
		msg = "Key handle is invalid.";
		throw new SessionException(msg, errorCode);
	    case CryptokiLibrary.CKR_OBJECT_HANDLE_INVALID:
		msg = "Object handle invalid.";
		throw new SessionException(msg, errorCode);
	    case CryptokiLibrary.CKR_OPERATION_ACTIVE:
		msg = "There is already an active operation or combination of active operations.";
		throw new SessionException(msg, errorCode);

	    case CryptokiLibrary.CKR_DEVICE_REMOVED:
		msg = "The token was removed from its slot during the execution of the function.";
		throw new SessionException(msg, errorCode);
	    case CryptokiLibrary.CKR_TOKEN_NOT_PRESENT:
		msg = "The token was not present in its slot at the time that the function was invoked.";
		throw new SessionException(msg, errorCode);
	    case CryptokiLibrary.CKR_KEY_CHANGED:
		msg = "One of the keys specified is not the same key that was being used in the original session.";
		throw new SessionException(msg, errorCode);

	    // TokenException
	    case CryptokiLibrary.CKR_DEVICE_MEMORY:
		msg = "The token does not have sufficient memory to perform the requested function.";
		throw new TokenException(msg, errorCode);
	    case CryptokiLibrary.CKR_DEVICE_ERROR:
		msg = "Some problem has occurred with the token and/or slot.";
		throw new TokenException(msg, errorCode);
	    case CryptokiLibrary.CKR_USER_PIN_NOT_INITIALIZED:
		msg = "The user PIN is not initialized.";
		throw new TokenException(msg, errorCode);

	    // PinIncorrectException
	    case CryptokiLibrary.CKR_PIN_INCORRECT:
		msg = "Pin incorrect.";
		throw new PinIncorrectException(msg, errorCode);
	    case CryptokiLibrary.CKR_PIN_INVALID:
		msg = "The PIN contains invalid characters.";
		throw new PinIncorrectException(msg, errorCode);
	    case CryptokiLibrary.CKR_PIN_LEN_RANGE:
		msg = "The PIN is too short or too long.";
		throw new PinIncorrectException(msg, errorCode);
	    case CryptokiLibrary.CKR_PIN_TOO_WEAK:
		msg = "The PIN is too weak.";
		throw new PinIncorrectException(msg, errorCode);

	    // AuthenticationException
	    case CryptokiLibrary.CKR_USER_ALREADY_LOGGED_IN:
		msg = "The user is already logged in.";
		throw new AuthenticationException(msg, errorCode);
	    case CryptokiLibrary.CKR_USER_ANOTHER_ALREADY_LOGGED_IN:
		msg = "Another user is already logged in.";
		throw new AuthenticationException(msg, errorCode);
	    case CryptokiLibrary.CKR_PIN_LOCKED:
		msg = "The PIN is locked.";
		throw new AuthenticationException(msg, errorCode);
	    case CryptokiLibrary.CKR_PIN_EXPIRED:
		msg = "The PIN is expired.";
		throw new AuthenticationException(msg, errorCode);

	    // CryptographicException
	    case CryptokiLibrary.CKR_KEY_FUNCTION_NOT_PERMITTED:
		msg = "Use of a key for a cryptographic purpose that the key's attributes are not set to allow it to do.";
		throw new CryptographicException(msg, errorCode);
	    case CryptokiLibrary.CKR_KEY_INDIGESTIBLE:
		msg = "Indicates that the value of the specified key cannot be digested for some reason.";
		throw new CryptographicException(msg, errorCode);
	    case CryptokiLibrary.CKR_KEY_NEEDED:
		msg = "Key used in the original sesion needs to be supplied.";
		throw new CryptographicException(msg, errorCode);
	    case CryptokiLibrary.CKR_KEY_NOT_NEEDED:
		msg = "An extraneous key was supplied.";
		throw new CryptographicException(msg, errorCode);
	    case CryptokiLibrary.CKR_KEY_NOT_WRAPPABLE:
		msg = "Unable to wrap the key as requested.";
		throw new CryptographicException(msg, errorCode);
	    case CryptokiLibrary.CKR_KEY_SIZE_RANGE:
		msg = "The supplied key's size is outside the range of key sizes that it can handle.";
		throw new CryptographicException(msg, errorCode);
	    case CryptokiLibrary.CKR_KEY_TYPE_INCONSISTENT:
		msg = "The specified key is not the correct type of key to use with the specified mechanism.";
		throw new CryptographicException(msg, errorCode);
	    case CryptokiLibrary.CKR_KEY_UNEXTRACTABLE:
		msg = "The specified private or secret key can't be wrapped.";
		throw new CryptographicException(msg, errorCode);

	    // InvalidArgumentsException
	    case CryptokiLibrary.CKR_ATTRIBUTE_TYPE_INVALID:
		msg = "Attribute type invalid.";
		throw new InvalidArgumentsException(msg, errorCode);
	    case CryptokiLibrary.CKR_TEMPLATE_INCONSISTENT:
		msg = "Template inconsistent.";
		throw new InvalidArgumentsException(msg, errorCode);
	    case CryptokiLibrary.CKR_ARGUMENTS_BAD:
		msg = "Bad arguments.";
		throw new InvalidArgumentsException(msg, errorCode);
	    case CryptokiLibrary.CKR_MECHANISM_PARAM_INVALID:
		msg = "Invalid parameters were supplied to the mechanism specified to the cryptographic operation.";
		throw new InvalidArgumentsException(msg, errorCode);

	    // CancellationException
	    case CryptokiLibrary.CKR_FUNCTION_CANCELED:
		msg = "The function has been cancelled.";
		throw new CancellationException(msg, errorCode);

	    // DataInvalidException
	    case CryptokiLibrary.CKR_DATA_INVALID:
		msg = "Data is invalid.";
		throw new DataInvalidException(msg, errorCode);


	    default:
		msg = "Undefined error occurred.";
		throw new GeneralError(msg, errorCode);
	}
    }



    private class LockedObject implements Closeable {

	@Override
	public void close() {
	    threadLock.release();
	}

    }

    public class LockedMiddlewareWrapper extends LockedObject {

	public void findObjectsInit(long hSession, CK_ATTRIBUTE pTemplate, int ulCount) throws CryptokiException {
	    NativeLong arraySizeRef = new NativeLong(ulCount);

	    // intialize find object with template and session
	    // ulCount is the number of attributes in the search template
	    check("C_FindObjectsInit", lib.C_FindObjectsInit(new NativeLong(hSession), pTemplate, arraySizeRef));
	}

	public List<Long> findObjects(long hSession) throws CryptokiException {
	    // search for objects
	    int maxObjects = 2048;
	    NativeLongArray phObject = new NativeLongArray(maxObjects);
	    NativeLongByReference pulObjectCount = new NativeLongByReference();

	    // session
	    // phObject points to the location that receives the list (array) of
	    // additional object handles
	    // ulMaxObjectCount is the maximum number of object handles to be
	    // returned
	    // pulObjectCount points to the location that receives the actual
	    // number of object handles returned
	    check("C_FindObjects", lib.C_FindObjects(new NativeLong(hSession), phObject.getReference(),
		    new NativeLong(maxObjects), pulObjectCount));

	    long[] result = phObject.getValues(pulObjectCount.getValue().intValue());
	    ArrayList<Long> resultLst = new ArrayList<>(result.length);
	    for (int i = 0; i < result.length; i++) {
		resultLst.add(result[i]);
	    }
	    return resultLst;
	}

	public void findObjectsFinalize(long hSession) throws CryptokiException {
	    // finish search for token/session
	    check("C_FindObjectsFinal", lib.C_FindObjectsFinal(new NativeLong(hSession)));
	}

	public void signInit(long hSession, CK_MECHANISM pMechanism, long hKey) throws CryptokiException {
	    check("C_SignInit", lib.C_SignInit(new NativeLong(hSession), pMechanism, new NativeLong(hKey)));
	}

	public byte[] sign(long hSession, byte[] data) throws CryptokiException {
	    ByteBuffer dataBuf = ByteBuffer.wrap(data);
	    int maxSigLen = 8 * 1024;
	    ByteBuffer sigValue = ByteBuffer.allocate(maxSigLen); // allocate 8k for the signature
	    NativeLongByReference sigLen = new NativeLongByReference(new NativeLong(maxSigLen));

	    check("C_Sign", lib.C_Sign(new NativeLong(hSession), dataBuf, new NativeLong(data.length), sigValue, sigLen));

	    byte[] result = new byte[sigLen.getValue().intValue()];
	    sigValue.get(result);

	    return result;
	}

    }

}
