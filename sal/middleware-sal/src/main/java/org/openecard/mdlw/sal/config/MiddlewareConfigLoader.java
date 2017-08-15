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

package org.openecard.mdlw.sal.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.xml.bind.JAXBException;
import org.openecard.common.util.FileUtils;


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
     * Creates an instance of the MiddlewareConfigLoader.
     * The MiddlewareConfigLoader loads the 'mw_configs.conf'-file. This file contains the paths to the Middleware
     * Config files. A Middleware Config file is a ZIP-File which contains an xml-file, which specifies Middleware SAL
     * instances, and the card images, which belongs to the cards which are supported by the Middleware.
     *
     * @throws IOException
     * @throws FileNotFoundException
     * @throws JAXBException
     */
    public MiddlewareConfigLoader() throws IOException, FileNotFoundException, JAXBException {
	loadMiddlewareSALConfigs();
    }

    private void loadMiddlewareSALConfigs() throws IOException,
	    FileNotFoundException, JAXBException {
	// load bundled Middleware config
	List<String> middlewareConfigPaths = getBundledConfigPaths();
	for (String mwConfigPath : middlewareConfigPaths) {
	    InputStream bundleStream = FileUtils.resolveResourceAsStream(getClass(), mwConfigPath);
	    MiddlewareConfig mwConfig = new MiddlewareConfig(bundleStream);
	    mwConfigs.add(mwConfig);
	    mwSALConfigs.addAll(mwConfig.getMiddlewareSALConfigs());
	}

	// TODO: load config from home directory based on property mw.sals.scan_home
	// TODO: consider renaming the property before implementing this
    }

    @Nonnull
    private List<String> getBundledConfigPaths() throws FileNotFoundException {
	try (InputStream is = FileUtils.resolveResourceAsStream(getClass(), MIDDLEWARE_CONFIG_PATHS_FILE)) {
	    if (is != null) {
		return FileUtils.readLinesFromConfig(is);
	    }
	} catch (IOException ex) {
	    String msg = "Unable to load Middleware Config Paths.";
	    throw new FileNotFoundException(msg);
	}
	return Collections.EMPTY_LIST;
    }

    public List<MiddlewareConfig> getMiddlewareConfigs() {
	return mwConfigs;
    }

    public List<MiddlewareSALConfig> getMiddlewareSALConfigs() {
	return mwSALConfigs;
    }

}
