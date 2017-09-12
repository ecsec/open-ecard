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

package org.openecard.gui.android;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;


/**
 *
 * @author Tobias Wich
 * @param <T>
 */
public class ParcelableCreator <T> implements Parcelable.Creator<T> {

    private final Class<T> clazz;
    private final Constructor<T> ctor;

    public ParcelableCreator(Class<T> clazz) throws SecurityException, IllegalArgumentException {
	this.clazz = clazz;
	try {
	    ctor = clazz.getConstructor(Parcel.class);
	} catch (NoSuchMethodException ex) {
	    throw new IllegalArgumentException("Parcelable class has no matching constructor.", ex);
	}
    }

    @Override
    public T createFromParcel(Parcel source) throws IllegalArgumentException {
	try {
	    T inst = ctor.newInstance(source);
	    return inst;
	} catch (ReflectiveOperationException ex) {
	    throw new IllegalArgumentException("Creator could not call constructor.", ex);
	}
    }

    @Override
    public T[] newArray(int size) {
	return (T[]) Array.newInstance(clazz, size);
    }

}
