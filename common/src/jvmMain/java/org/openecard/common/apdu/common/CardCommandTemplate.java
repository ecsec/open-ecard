/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.common.apdu.common;

import iso.std.iso_iec._24727.tech.schema.CardCallTemplateType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.StringUtils;


/**
 * Command APDU template.
 * The template is defined by {@link CardCallTemplateType}. It's underlying type in the xsd document contains a detailed
 * description how template values are evaluated.
 *
 * @author Tobias Wich
 */
public class CardCommandTemplate {

    private static final Pattern EXPRESSION = Pattern.compile("\\{.*?\\}");

    private final CardCallTemplateType template;

    /**
     * Creates the template class based on the given data type class.
     *
     * @param apduTemplate Data type with values needed to evaluate the template to an APDU.
     */
    public CardCommandTemplate(@Nonnull CardCallTemplateType apduTemplate) {
	this.template = apduTemplate;
    }

    /**
     * Evaluate the template defintion wrapped up in this class against the given context.
     * The available values in the context object are dependent on the usage scenario. For example when used in a
     * signature context, there will be variables available for the algorithms and similar information.
     *
     * @param context Context containing functions and values.
     * @return A CardCommandAPDU which can be serialized and used in Transmit and the like.
     * @throws APDUTemplateException Thrown in case no APDU can be derived from the template.
     */
    @Nonnull
    public CardCommandAPDU evaluate(@Nonnull Map<String, Object> context) throws APDUTemplateException {
	byte[] head = evalTemplate(template.getHeaderTemplate(), context);
	byte[] data = evalTemplate(template.getDataTemplate(), context);
	BigInteger length = template.getExpectedLength();

	// a few sanity checks
	if (head.length != 4) {
	    throw new APDUTemplateException("The computed command APDU header is not valid.");
	}

	CardCommandAPDU apdu = new CardCommandAPDU();
	apdu.setCLA(head[0]);
	apdu.setINS(head[1]);
	apdu.setP1(head[2]);
	apdu.setP2(head[3]);
	if (data.length > 0) {
	    apdu.setData(data);
	}
	if (length != null) {
	    apdu.setLE(length.intValue());
	}

	return apdu;
    }

    @Nonnull
    private byte[] evalTemplate(@Nullable String s, @Nonnull Map<String, Object> context) throws APDUTemplateException {
	if (s == null) {
	    return new byte[0];
	}

	Matcher m = EXPRESSION.matcher(s);
	while (m.find()) {
	    // get matching group and cut off the curlies
	    String expr = m.group();
	    expr = expr.substring(1, expr.length() - 1);

	    // get tokens and produce
	    String[] tokens = getTokens(expr);
	    Object firstObj = getFirstObject(tokens, context);

	    // evaluate value or function template
	    String result;
	    if (tokens.length == 1) {
		result = evalObject(firstObj);
	    } else {
		Object[] params = getParameters(tokens, context);
		result = evalObject(firstObj, params);
	    }

	    // cut expression and replace with result
	    s = m.reset().replaceFirst(result);
	    m.reset(s);
	}

	byte[] resultBytes = StringUtils.toByteArray(s, true);
	return resultBytes;
    }

    private String evalObject(Object o) throws APDUTemplateException {
	return evalObject(o, new Object[0]);
    }

    private String evalObject(@Nonnull Object o, @Nonnull Object[] params) throws APDUTemplateException {
	if (o instanceof byte[]) {
	    String result = ByteUtils.toHexString((byte[]) o);
	    return result;
	} else if (o instanceof APDUTemplateFunction) {
	    APDUTemplateFunction fun = (APDUTemplateFunction) o;
	    String result = fun.call(params);
	    return result;
	} else {
	    // this also includes the String class where it is the identity function
	    return o.toString();
	}
    }

    @Nonnull
    private String[] getTokens(@Nonnull String expr) {
	String[] groups = expr.split("\\s+");
	return groups;
    }

    @Nonnull
    private Object getFirstObject(@Nonnull String[] tokens, @Nonnull Map<String, Object> ctx)
	    throws APDUTemplateException {
	Object firstObj = ctx.get(tokens[0]);
	if (firstObj == null) {
	    throw new NullPointerException();
	} else if (tokens.length > 1 && ! (firstObj instanceof APDUTemplateFunction)) {
	    // TODO: error not a function object
	    throw new APDUTemplateException("Multiple element template but no function named.");
	} else {
	    return firstObj;
	}
    }

    @Nonnull
    private Object[] getParameters(@Nonnull String[] tokens, @Nonnull Map<String, Object> context)
	    throws APDUTemplateException {
	ArrayList<Object> result = new ArrayList<Object>(tokens.length - 1);
	// get each token from the context object
	for (int i = 1; i < tokens.length; i++) {
	    String next = tokens[i];
	    if (next.startsWith("0x")) {
		String value = next.substring(2);
		result.add(value);
	    } else {
		Object o = context.get(next);
		if (o instanceof APDUTemplateFunction) {
		    throw new APDUTemplateException("Function used in parameter list.");
		} else {
		    result.add(o);
		}
	    }
	}
	return result.toArray();
    }

}
