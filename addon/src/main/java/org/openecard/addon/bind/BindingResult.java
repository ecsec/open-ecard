/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

package org.openecard.addon.bind;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
public class BindingResult {

    private BindingResultCode resultCode;
    private Body body;
    private String resultMessage;
    private final Map<String, String> parameters = new HashMap<>();
    private final Map<String, String> auxData = new HashMap<>();
    private final List<Attachment> attachments = new ArrayList<>(10);

    /**
     * Creates a response with result code OK.
     */
    public BindingResult() {
	resultCode = BindingResultCode.OK;
    }

    public BindingResult(@Nonnull BindingResultCode resultCode) {
	if (resultCode == null) {
	    throw new IllegalArgumentException("No result code given in constructor invocation.");
	}
	this.resultCode = resultCode;
    }

    public BindingResult setResultCode(@Nonnull BindingResultCode resultCode) {
	if (resultCode == null) {
	    throw new IllegalArgumentException("No result code given in constructor invocation.");
	}
	this.resultCode = resultCode;
	return this;
    }

    @Nonnull
    public BindingResultCode getResultCode() {
	return resultCode;
    }

    public BindingResult setBody(@Nullable Body body) {
	this.body = body;
	return this;
    }

    @Nullable
    public Body getBody() {
	return this.body;
    }

    @Nonnull
    public Map<String, String> getParameters() {
	return this.parameters;
    }

    public BindingResult addParameter(@Nonnull String key, @Nullable String value) {
	parameters.put(key, value);
	return this;
    }

    public BindingResult addParameters(@Nonnull Map<String, String> parameters) {
	this.parameters.putAll(parameters);
	return this;
    }

    @Nullable
    public String removeParameter(@Nonnull String key) {
	return this.parameters.remove(key);
    }

    @Nonnull
    public List<Attachment> getAttachments() {
	return attachments;
    }

    @Nonnull
    public Map<String, String> getAuxResultData() {
	return this.auxData;
    }

    public BindingResult addAuxResultData(@Nonnull String key, @Nullable String value) {
	this.auxData.put(key, value);
	return this;
    }

    public BindingResult setResultMessage(@Nullable String resultMessage) {
	this.resultMessage = resultMessage;
	return this;
    }

    @Nullable
    public String getResultMessage() {
	return resultMessage;
    }

    @Override
    public String toString() {
	StringWriter w = new StringWriter();
	// Header
	w.write("BindingResult <");
	w.write(getResultCode().name());
	w.write(", ");
	w.write(getResultMessage() == null ? "" : "'");
	w.write(getResultMessage());
	w.write(getResultMessage() == null ? "" : "'");
	w.write(">\n");
	// Parameter
	Map<String, String> params = getParameters();
	printMap(w, "  ", "Parameters", params);
	// AuxData
	Map<String, String> aux = getAuxResultData();
	printMap(w, "  ", "AuxResultData", aux);
	// Body
	Body b = getBody();
	if (b != null) {
	    w.append("  Body type: ").append(body.getMimeType()).append("\n");
	}
	// Attachments
	List<Attachment> atts = getAttachments();
	for (Attachment a : atts) {
	    w.append("  Attachment with type: ").append(a.getMIMEType()).append("\n");
	}
	// done
	return w.toString();
    }

    private <V> void printMap(StringWriter w, String prefix, String identifier, Map<String, V> map) {
	if (! map.isEmpty()) {
	    w.append(prefix).append(identifier).append(" {\n");
	    for (Map.Entry<String, V> next : map.entrySet()) {
		w.append(prefix).append(prefix).append("'").append(next.getKey()).append("': ");
		V v = next.getValue();
		if (v instanceof String) {
		    w.append("'").append(v.toString()).append("',\n");
		} else {
		    String s = v == null ? "null" : v.toString();
		    w.append(s).append(",\n");
		}
	    }
	    w.append(prefix).append("}\n");
	}
    }

}
