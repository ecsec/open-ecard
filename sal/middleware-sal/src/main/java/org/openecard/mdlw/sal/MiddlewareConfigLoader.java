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

package org.openecard.mdlw.sal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBException;


/**
 * Loads all Middleware Configs specified in 'mw_configs.conf' and returns all MiddlewareSAL configs.
 *
 * @author Mike Prechtl
 */
public class MiddlewareConfigLoader {

    private static final String MIDDLEWARE_CONFIG_PATHS_FILE = "/middleware/mw_configs.conf";

    private final List<MiddlewareConfig> mwConfigs = new ArrayList<>();
    private final List<MiddlewareSALConfig> mwSALConfigs = new ArrayList<>();

    /**
     * Creates an instance of the MiddlewareConfigLoader. The MiddlewareConfigLoader loads the 'mw_configs.conf'-file.
     * This file contains the paths to the Middleware Config files. A Middleware Config file is a ZIP-File which contains
     * an xml-file, which specifies Middleware SAL instances, and the card images, which belongs to the cards which are
     * supported by the Middleware.
     * The path parameter can be null, then the standard middleware config file from the classpath will be
     * used ('mw_configs.conf').
     *
     * @param path to the conf-file which contains the paths to the Middleware Configs.
     * @throws IOException
     * @throws FileNotFoundException
     * @throws JAXBException
     */
    public MiddlewareConfigLoader(@Nullable String path) throws IOException, FileNotFoundException, JAXBException {
	loadMiddlewareSALConfigs(path);
    }

    private void loadMiddlewareSALConfigs(@Nullable String path) throws IOException,
	    FileNotFoundException, JAXBException {
	List<String> middlewareConfigPaths;
	if (path == null) {
	    middlewareConfigPaths = getMiddlewareConfigPaths();
	} else {
	    middlewareConfigPaths = getMiddlewareConfigPaths(path);
	}
	for (String mwConfigPath : middlewareConfigPaths) {
	    MiddlewareConfig mwConfig = new MiddlewareConfig(mwConfigPath);
	    mwConfigs.add(mwConfig);
	    mwSALConfigs.addAll(mwConfig.getMiddlewareSALConfigs());
	}
    }

    @Nonnull
    private List<String> getMiddlewareConfigPaths() throws FileNotFoundException {
	try (InputStream is = getClass().getResourceAsStream(MIDDLEWARE_CONFIG_PATHS_FILE)) {
	    if (is != null) {
		return readMiddlewareConfigPaths(is);
	    }
	} catch (IOException ex) {
	    String msg = "Unable to load Middleware Config Paths.";
	    throw new FileNotFoundException(msg);
	}
	return Collections.EMPTY_LIST;
    }

    @Nonnull
    private List<String> getMiddlewareConfigPaths(String pathToFile) throws FileNotFoundException {
	File file = new File(pathToFile);
	try (InputStream fis = new FileInputStream(file)) {
	    return readMiddlewareConfigPaths(fis);
	} catch (IOException ex) {
	    String msg = "Unable to load Middleware Config Paths.";
	    throw new FileNotFoundException(msg);
	}
    }

    @Nonnull
    private List<String> readMiddlewareConfigPaths(InputStream is) {
	Scanner scanner = new Scanner(is, "UTF-8");
	List<String> mwConfigPaths = new ArrayList<>();
	while (scanner.hasNextLine()) {
	    String mwConfigPath = scanner.nextLine();
	    mwConfigPaths.add(mwConfigPath);
	}
	return mwConfigPaths;
    }

    public List<MiddlewareConfig> getMiddlewareConfigs() {
	return mwConfigs;
    }

    public List<MiddlewareSALConfig> getMiddlewareSALConfigs() {
	return mwSALConfigs;
    }

}
