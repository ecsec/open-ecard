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

package org.openecard.control.binding.http.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import org.openecard.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper for managing a CORS origin whitelist.
 *
 * @author Tobias Wich
 */
public class OriginsList {

    private static final Logger LOG = LoggerFactory.getLogger(OriginsList.class);

    private static Set<URI> whitelist;

    static {
	load();
    }

    private static synchronized void load() {
	TreeSet wl = new TreeSet<>();

	try {
	    // read bundled whitelist
	    InputStream bundledWl = FileUtils.resolveResourceAsStream(OriginsList.class, "/binding/origins.whitelist");
	    readWhitelist(wl, bundledWl);

	    // read user supplied whitelist
	    File homePath = FileUtils.getHomeConfigDir();
	    File cfgFile = new File(homePath, "origins.whitelist");
	    if (cfgFile.isFile() && cfgFile.canRead()) {
		InputStream homeWl = new FileInputStream(cfgFile);
		readWhitelist(wl, homeWl);
	    }
	} catch (IOException | SecurityException ex) {
	    LOG.error("Failed to read CORS whitelist.", ex);
	}

	whitelist = wl;
    }

    private static void readWhitelist(Set<URI> wl, InputStream is) throws IOException {
	BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
	String nextLine;
	while ((nextLine = br.readLine()) != null) {
	    nextLine = nextLine.trim();
	    // skip comments and empty lines
	    if (nextLine.startsWith("#") || nextLine.isEmpty()) {
		continue;
	    }

	    // create URI and add it to the list
	    try {
		URI nextUri = new URI(nextLine);
		wl.add(nextUri);
		LOG.debug("Added '{}' to origin whitelist.", nextLine);
	    } catch (URISyntaxException ex) {
		LOG.warn("Failed to add URL '{}' to the whitelist.", nextLine);
	    }
	}
    }

    @Nonnull
    public static File getUserWhitelist() throws IOException, SecurityException {
	File homePath = FileUtils.getHomeConfigDir();
	File cfgFile = new File(homePath, "origins.whitelist");
	if (! cfgFile.exists()) {
	    Writer w = new OutputStreamWriter(new FileOutputStream(cfgFile), StandardCharsets.UTF_8);
	    try (PrintWriter pw = new PrintWriter(w)) {
		pw.println("##");
		pw.println("## List of allowed CORS origins");
		pw.println("## ----------------------------");
		pw.println("## Entries must follow the CORS specification. One origin entry per file.");
		pw.println("## Comments begin with the # character.");
		pw.println("## Example: https://example.com");
		pw.println("##");
	    }
	}
	return cfgFile;
    }


    public static boolean isValidOrigin(@Nonnull String origin) throws URISyntaxException {
	URI uri = new URI(origin);
	return isValidOrigin(uri);
    }

    public static boolean isValidOrigin(@Nonnull URI origin) {
	boolean whitelisted = whitelist.contains(origin);
	return whitelisted;
    }

}
