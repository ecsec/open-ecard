/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.common;

import java.io.PrintStream;
import java.io.PrintWriter;
import oasis.names.tc.dss._1_0.core.schema.InternationalStringType;
import oasis.names.tc.dss._1_0.core.schema.Result;


/**
 * Exception class taking care of easy creation of Exceptions based on the oasis Result type.<br/>
 * The usage is as follows.
 * <ol>
 * <li>Derive from this class and leave implementation empty.</li>
 * <li>Call <code>makeException</code> function to create the exception and hand over a freshly copied exception.<br/>
 * <code>throw MyExc.makeException(new MyExc(), "Major", "Minor", "Msg");</code>
 * </li>
 * </ol>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public abstract class ECardException extends Exception {

    private Implementation impl;

    protected final void setImpl(Implementation impl) {
	this.impl = impl;
    }


    public static <E extends ECardException> E makeException(E e, String major, String minor, String msg) {
	Implementation i = new Implementation(major, minor, msg);
	e.setImpl(i);
	return e;
    }
    public static <E extends ECardException> E makeException(E e, String minor, String msg) {
	return makeException(e, ECardConstants.Major.ERROR, minor, msg);
    }
    public static <E extends ECardException> E makeException(E e, String msg) {
	return makeException(e, ECardConstants.Minor.App.UNKNOWN_ERROR, msg);
    }

    public static <E extends ECardException> E makeException(E e, Result r) {
	Implementation i = new Implementation(r);
	e.setImpl(i);
	return e;
    }

    public static <E extends ECardException> E makeException(E e, Throwable t, String major, String minor, String msg) {
	Implementation i = new Implementation(t, major, minor, msg);
	e.setImpl(i);
	return e;
    }
    public static <E extends ECardException> E makeException(E e, Throwable t, String minor, String msg) {
	return makeException(e, t, ECardConstants.Major.ERROR, minor, msg);
    }
    public static <E extends ECardException> E makeException(E e, Throwable t, String msg) {
	return makeException(e, t, ECardConstants.Minor.App.UNKNOWN_ERROR, msg);
    }
    public static <E extends ECardException> E makeException(E e, Throwable t) {
	return makeException(e, t, t.getMessage());
    }



    public static class Implementation extends Exception {

	private String resultMajor;
	private String resultMinor;


	private Implementation(Result r) {
	    super((r.getResultMessage() != null) ? r.getResultMessage().getValue() : "Unknown IFD exception occurred.");
	    this.resultMajor = r.getResultMajor();
	    if (r.getResultMinor() != null) {
		this.resultMinor = r.getResultMinor();
	    } else {
		this.resultMinor = ECardConstants.Minor.App.UNKNOWN_ERROR;
	    }
	}

	/**
	 * Constructs a new ECardException with the specified results and detail result message.
	 * @param resultMajor Result major
	 * @param resultMinor Result minor
	 * @param resultMessage Detail message
	 */
	private Implementation(String resultMajor, String resultMinor, String resultMessage) {
	    super(resultMessage);
	    this.resultMajor = resultMajor;
	    this.resultMinor = resultMinor;
	}

	/**
	 * Constructs a new ECardException with the specified results and detail result message.
	 * @param resultMajor Result major
	 * @param resultMinor Result minor
	 * @param resultMessage Detail message
	 */
	private Implementation(Throwable cause, String resultMajor, String resultMinor, String resultMessage) {
	    super(resultMessage, cause);
	    this.resultMajor = resultMajor;
	    this.resultMinor = resultMinor;
	}

    }



    /**
     * Simple getter method.
     * @return resultMajor
     */
    public final String getResultMajor() {
	return impl.resultMajor;
    }

    /**
     * Simple getter method.
     * @return resultMinor
     */
    public final String getResultMinor() {
	return impl.resultMinor;
    }

    /**
     * Simple getter method.
     * @return resultMessage
     */
    public final String getResultMessage() {
	return impl.getMessage();
    }

    public final Result getResult() {
	Result r = new Result();
	r.setResultMajor(getResultMajor());
	r.setResultMinor(getResultMinor());
	InternationalStringType s = new InternationalStringType();
	s.setLang("en");
	s.setValue(getResultMessage());
        r.setResultMessage(s);
	return r;
    }

    
    ///
    /// Redirect exception functions to implementation
    ///
    
    @Override
    public final String getMessage() {
        return getResultMessage();
    }

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }

    @Override
    public Throwable getCause() {
        return impl.getCause();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return impl.getStackTrace();
    }

    @Override
    public void printStackTrace() {
        impl.printStackTrace();
    }

    @Override
    public void printStackTrace(PrintStream s) {
        impl.printStackTrace(s);
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        impl.printStackTrace(s);
    }

}
