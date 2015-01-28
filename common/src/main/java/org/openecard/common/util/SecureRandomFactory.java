/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

package org.openecard.common.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Factory class for SecureRandom instances.
 * This factory contains special handling for platforms where inadequate SecureRandom objects are created. For Linux
 * that means /dev/urandom is used instead of /dev/random, so that the input dows not block. For special purposes it is
 * still possible to use Java's default SecureRandom.
 *
 * @author Tobias Wich
 */
public class SecureRandomFactory {

    private static final Logger logger = LoggerFactory.getLogger(SecureRandomFactory.class);

    /**
     * Creates a new SecureRandom instance.
     * The instance is seeded with its own seed mechanism.
     *
     * @param numSeedBytes
     * @return Seeded instance of SecureRandom.
     * @see SecureRandom#generateSeed(int)
     */
    public static SecureRandom create(int numSeedBytes) {
	numSeedBytes = numSeedBytes < 0 ? 0 : numSeedBytes;

	SecureRandom r;
	if ("Linux".equals(System.getProperty("os.name"))) {
	    r = new LinuxSecureRandom();
	} else {
	    // the default is fine
	    r = new SecureRandom();
	}

	if (numSeedBytes > 0) {
	    r.setSeed(r.generateSeed(numSeedBytes));
	}

	return r;
    }


    private static class LinuxSecureRandom extends SecureRandom {

	private final SeedSource seedSource;

	public LinuxSecureRandom() {
	    SeedSource s;
	    try {
		s = new UrandomSeedSource();
	    } catch (FileNotFoundException | SecurityException ex) {
		logger.warn("Failed to open entropy source to /dev/urandom, Falling back to default implementation.");
		s = new SecureRandomSeedSource();
	    }
	    seedSource = s;
	}

	@Override
	public byte[] generateSeed(int numBytes) {
	    return seedSource.genSeed(numBytes);
	}

    }


    /**
     * Seed source interface to abstract the dirty details of reading file streams in the actual implementation.
     */
    private interface SeedSource {

	byte[] genSeed(int numBytes);

    }


    private static class SecureRandomSeedSource implements SeedSource {

	@Override
	public byte[] genSeed(int numBytes) {
	    return SecureRandom.getSeed(numBytes);
	}

    }

    private static class UrandomSeedSource implements SeedSource {

	private final InputStream randomStream;

	public UrandomSeedSource() throws FileNotFoundException, SecurityException {
	    randomStream = new FileInputStream("/dev/urandom");
	}

	@Override
	public byte[] genSeed(int numBytes) {
	    try {
	    byte[] result = new byte[numBytes];
	    int remaining = numBytes;
	    int start = 0;
	    while (remaining > 0) {
		int len = randomStream.read(result, start, remaining);
		if (len < 0) {
		    throw new InternalError("Entropy source /dev/urandom returned EOF.");
		}
		start += len;
		remaining -= len;
	    }
	    return result;
	    } catch (IOException ex) {
		throw new InternalError("Error while reading random numbers from /dev/urandom.\n  " + ex.getMessage());
	    }
	}

    }

}
