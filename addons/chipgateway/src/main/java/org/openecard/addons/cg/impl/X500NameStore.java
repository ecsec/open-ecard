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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x500.X500Name;
import org.openecard.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public abstract class X500NameStore {

    private static final Logger LOG = LoggerFactory.getLogger(X500NameStore.class);

    private final List<X500Name> subjectNames;

    protected X500NameStore(String fileName) {
	List<String> subjectNameStrs = readFile(fileName);
	subjectNames = convertX500Names(subjectNameStrs);
    }


    public boolean isInSubjects(String subject) {
	X500Name requestedSubject = new X500Name(subject);
	return subjectNames.contains(requestedSubject);
    }

    public boolean isInSubjects(X500Principal subj) {
	return isInSubjects(subj.getName());
    }

    private List<String> readFile(String fname) {
	try (InputStream in = FileUtils.resolveResourceAsStream(getClass(), fname)) {
	    return FileUtils.readLinesFromConfig(in);
	} catch (IOException ex) {
	    LOG.error("Failed to read allowed subjects file.", ex);
	}

	return Collections.emptyList();
    }

    private List<X500Name> convertX500Names(Collection<String> subjectNameStrs) {
	ArrayList<X500Name> result = new ArrayList<>();
	for (String next: subjectNameStrs) {
	    X500Name name = convertX500Name(next);
	    result.add(name);
	}
	return result;
    }

    private X500Name convertX500Name(String nameStr) {
	X500Name name = new X500Name(nameStr);
	return name;
    }

}
