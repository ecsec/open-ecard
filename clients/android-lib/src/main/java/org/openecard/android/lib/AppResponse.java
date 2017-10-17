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

package org.openecard.android.lib;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Represents the response which is sent from the core library to the android app. The response codes which are used are
 * available in {@link AppResponseStatusCodes}, the messages are available in {@link AppMessages}.
 *
 * @author Mike Prechtl
 */
public class AppResponse implements Parcelable {

    private final String message;
    private final int statusCode;

    public AppResponse(Parcel in) {
	this.statusCode = in.readInt();
	this.message = in.readString();
    }

    public AppResponse(int statusCode, String message) {
	this.statusCode = statusCode;
	this.message = message;
    }

    @Override
    public int describeContents() {
	return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
	parcel.writeInt(statusCode);
	parcel.writeString(message);
    }

    public String getMessage() {
	return message;
    }

    public int getStatusCode() {
	return statusCode;
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<AppResponse> CREATOR = new Parcelable.Creator<AppResponse>() {
	@Override
	public AppResponse createFromParcel(Parcel in) {
	    return new AppResponse(in);
	}

	@Override
	public AppResponse[] newArray(int size) {
	    return new AppResponse[size];
	}
    };

}
