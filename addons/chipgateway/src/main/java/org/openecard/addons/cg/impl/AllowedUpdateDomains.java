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

package org.openecard.addons.cg.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.openecard.common.util.DomainUtils;
import org.openecard.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class AllowedUpdateDomains {

    private static final Logger LOG = LoggerFactory.getLogger(AllowedUpdateDomains.class);

    private static AllowedUpdateDomains inst;

    private final List<String> domainNames;

    private AllowedUpdateDomains() {
	List<String> domainNameStrs = readFile("chipgateway/allowed_update_domains");
	domainNames = domainNameStrs;
    }

    public static AllowedUpdateDomains instance() {
	// synchronization not needed, because at worst we load it several times
	if (inst == null) {
	    inst = new AllowedUpdateDomains();
	}
	return inst;
    }

    public boolean isAllowedDomain(String domainName) {
	for (String nextRef : domainNames) {
	    if (DomainUtils.checkWildcardHostName(nextRef, domainName)) {
		return true;
	    }
	}
	// no match found
	return false;
    }

    private List<String> readFile(String fname) {
	try (InputStream in = FileUtils.resolveResourceAsStream(getClass(), fname)) {
	    return FileUtils.readLinesFromConfig(in);
	} catch (IOException ex) {
	    LOG.error("Failed to read allowed update domains file.", ex);
	}

	return Collections.emptyList();
    }

}
