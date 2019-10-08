/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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

package org.openecard.scio;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import org.openecard.common.ifd.scio.NoSuchTerminal;
import org.openecard.common.ifd.scio.SCIOErrorCode;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOTerminal;
import org.openecard.common.ifd.scio.SCIOTerminals;
import org.openecard.common.ifd.scio.TerminalState;
import org.openecard.common.ifd.scio.TerminalWatcher;
import org.openecard.common.util.LinuxLibraryFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Proxy and abstracted Factory for SCIO PC/SC driver.
 *
 * @author Tobias Wich
 * @author Benedikt Biallowons
 */
public class PCSCFactory implements org.openecard.common.ifd.scio.TerminalFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PCSCFactory.class);
    private static final String ALGORITHM = "PC/SC";

    private final String osName;
    private TerminalFactory terminalFactory;

    private final CompletableFuture<Void> initLock;

    /**
     * Default constructor with fixes for the faulty SmartcardIO library.
     *
     * @throws FileNotFoundException if pcsclite for Linux can't be found.
     * @throws NoSuchAlgorithmException if no PC/SC provider can be found.
     */
    public PCSCFactory() throws FileNotFoundException, NoSuchAlgorithmException {
	osName = System.getProperty("os.name");
	if (osName.startsWith("Linux")) {
	    File libFile = LinuxLibraryFinder.getLibraryPath("pcsclite", "1");
	    System.setProperty("sun.security.smartcardio.library", libFile.getAbsolutePath());
	}

	this.initLock = new CompletableFuture<>();

	try {
	    LOG.info("Trying to initialize PCSC subsystem.");
	    loadPCSC();
	    initLock.complete(null);
	    LOG.info("Successfully initialized PCSC subsystem");
	} catch (NoSuchAlgorithmException ex) {
	    LOG.error("Failed to initialize smartcard system.", ex);
	    if (isNoServiceException(ex)) {
		new Thread(() -> {
		    while (! initLock.isDone()) {
			try {
			    LOG.debug("Trying to initialize PCSC subsystem again.");
			    loadPCSC();
			    initLock.complete(null);
			    LOG.info("Successfully initialized PCSC subsystem");
			} catch (NoSuchAlgorithmException exInner) {
			    if (isNoServiceException(exInner)) {
				try {
				    LOG.debug("Retrying PCSC initialization in 5 seconds.");
				    Thread.sleep(5000);
				} catch (InterruptedException ex2) {
				    return;
				}
			    } else {
				LOG.error("Failed to initialize smartcard system.", exInner);
				throw new RuntimeException(exInner);
			    }
			}
		    }
		}).start();
	    } else {
		throw ex;
	    }
	}
    }

    @Override
    public String getType() {
	if (terminalFactory != null) {
	    return terminalFactory.getType();
	} else {
	    return "PC/SC";
	}
    }

    @Override
    public SCIOTerminals terminals() {
	if (terminalFactory != null) {
	    return new PCSCTerminals(this);
	} else {
	    // dummy for use while the initialization is not working properly
	    return new SCIOTerminals() {
		@Override
		public List<SCIOTerminal> list(SCIOTerminals.State state) throws SCIOException {
		    return Collections.emptyList();
		}

		@Override
		public List<SCIOTerminal> list() throws SCIOException {
		    return Collections.emptyList();
		}

		@Override
		public SCIOTerminal getTerminal(String name) throws NoSuchTerminal {
		    throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public TerminalWatcher getWatcher() throws SCIOException {
		    return new TerminalWatcher() {
			@Override
			public SCIOTerminals getTerminals() {
			    return terminals();
			}

			@Override
			public List<TerminalState> start() throws SCIOException {
			    return Collections.emptyList();
			}

			@Override
			public TerminalWatcher.StateChangeEvent waitForChange(long timeout) throws SCIOException {
			    try {
				LOG.debug("Fake waiting for terminal changes during PCSC initialization phase.");
				initLock.get(timeout, TimeUnit.MILLISECONDS);
			    } catch (InterruptedException | ExecutionException | TimeoutException ex) {
				// ignore
			    }
			    LOG.debug("Returning from fake terminal change wait.");
			    return new StateChangeEvent();
			}

			@Override
			public TerminalWatcher.StateChangeEvent waitForChange() throws SCIOException {
			    return waitForChange(Long.MAX_VALUE);
			}
		    };
		}
	    };
	}
    }

    TerminalFactory getRawFactory() {
	return terminalFactory;
    }

    private static Integer versionCompare(String str1, String str2) {
	// code taken from http://stackoverflow.com/a/6702029
	String[] vals1 = str1.split("\\.");
	String[] vals2 = str2.split("\\.");
	int i = 0;

	while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
	    i++;
	}

	if (i < vals1.length && i < vals2.length) {
	    return Integer.signum(Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i])));
	}

	return Integer.signum(vals1.length - vals2.length);
    }

    final void loadPCSC() throws NoSuchAlgorithmException {
	terminalFactory = TerminalFactory.getInstance(ALGORITHM, null);
    }

    void reloadPCSC() {
	try {
	    reloadPCSCInt();
	} catch (NoSuchAlgorithmException ex) {
	    // if it worked once it will work again
	    String msg = "PCSC changed it's algorithm. There is something really wrong.";
	    LOG.error(msg, ex);
	    throw new RuntimeException("PCSC changed it's algorithm. There is something really wrong.");
	} catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException |
		SecurityException ex) {
	    LOG.error("Failed to perform reflection magic to reload TerminalFactory.", ex);
	} catch (InvocationTargetException ex) {
	    if (isNoServiceException(ex)) {
		// silent drop after giving the system some time to recover for themselves
		try {
		    Thread.sleep(5000);
		} catch (InterruptedException ignore) {
		    Thread.currentThread().interrupt();
		}
		return;
	    }

	    LOG.error("Error while invoking PCSC restart functionality.");
	}
    }

    private void reloadPCSCInt() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException,
	    NoSuchMethodException, InvocationTargetException, NoSuchAlgorithmException {
	// code taken from http://stackoverflow.com/questions/16921785/
	Class pcscterminal = Class.forName("sun.security.smartcardio.PCSCTerminals");
	Field contextId = pcscterminal.getDeclaredField("contextId");
	contextId.setAccessible(true);

	if (contextId.getLong(pcscterminal) != 0L) {
	    // First get a new context value
	    Class pcsc = Class.forName("sun.security.smartcardio.PCSC");
	    Method SCardEstablishContext = pcsc.getDeclaredMethod("SCardEstablishContext", Integer.TYPE);
	    SCardEstablishContext.setAccessible(true);

	    Field SCARD_SCOPE_USER = pcsc.getDeclaredField("SCARD_SCOPE_USER");
	    SCARD_SCOPE_USER.setAccessible(true);

	    long newId = ((Long) SCardEstablishContext.invoke(pcsc, SCARD_SCOPE_USER.getInt(pcsc)));
	    contextId.setLong(pcscterminal, newId);

	    loadPCSC();
	    // Then clear the terminals in cache
	    CardTerminals terminals = terminalFactory.terminals();
	    Field fieldTerminals = pcscterminal.getDeclaredField("terminals");
	    fieldTerminals.setAccessible(true);
	    Map termObj = (Map) fieldTerminals.get(terminals);
	    termObj.clear();
	}
    }

    private boolean isNoServiceException(Exception mainException) {
	if (PCSCExceptionExtractor.hasPCSCException(mainException)) {
	    SCIOErrorCode code = PCSCExceptionExtractor.getCode(mainException);
	    if (code == SCIOErrorCode.SCARD_E_NO_SERVICE) {
		return true;
	    }
	}

	return false;
    }

}
