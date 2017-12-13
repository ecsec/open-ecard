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

package org.openecard.android.system;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Represents the response which is sent from the core library to the android app. The response codes which are used are
 * available in {@link ServiceResponseStatusCodes}, the messages are available in {@link ServiceMessages}.
 *
 * @author Mike Prechtl
 */
public class ServiceResponse implements Parcelable {

    private final ServiceResponseLevel level;
    private final String message;
    private final int statusCode;

    // TODO aufspalten in AppErrorResponse AppWarningResponse

    public ServiceResponse(Parcel in) {
	this.level = ServiceResponseLevel.valueOf(in.readString());
	this.statusCode = in.readInt();
	this.message = in.readString();
    }

    public ServiceResponse(int statusCode, String message) {
	this.level = ServiceResponseLevel.INFO;
	this.statusCode = statusCode;
	this.message = message;
    }

    public ServiceResponse(ServiceResponseLevel level, int statusCode, String message) {
	this.level = level;
	this.statusCode = statusCode;
	this.message = message;
    }

    @Override
    public int describeContents() {
	return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
	parcel.writeString(level.name());
	parcel.writeInt(statusCode);
	parcel.writeString(message);
    }

    public String getMessage() {
	return message;
    }

    public int getStatusCode() {
	return statusCode;
    }

    public ServiceResponseLevel getResponseLevel() {
	return level;
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<ServiceResponse> CREATOR = new Parcelable.Creator<ServiceResponse>() {
	@Override
	public ServiceResponse createFromParcel(Parcel in) {
	    return new ServiceResponse(in);
	}

	@Override
	public ServiceResponse[] newArray(int size) {
	    return new ServiceResponse[size];
	}
    };

}
