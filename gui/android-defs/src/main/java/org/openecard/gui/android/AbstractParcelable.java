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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 * @param <T>
 */
public abstract class AbstractParcelable <T> implements Parcelable {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractParcelable.class);

    @Override
    public int describeContents() {
	return containsFileDescriptor() ? CONTENTS_FILE_DESCRIPTOR : 0;
    }

    /**
     * Override if this class contains a file descriptor.
     * If not overridden, this method always returns {@code false}.
     *
     * @return {@code true} if this Parcelable contains a file descriptor, {@code false} otherwise.
     */
    protected boolean containsFileDescriptor() {
	return false;
    }

    protected final void readFromParcel(Parcel src) throws IllegalArgumentException {
	try {
	    for (Field f : getFields()) {
		try {
		    f.setAccessible(true);
		} catch (SecurityException ex) {
		    LOG.debug("Faild to set accessibility flag of the class");
		}

		Class<?> type = f.getType();
		if (type.isArray()) {
		    if (boolean[].class == type) {
			f.set(this, src.createBooleanArray());
		    } else if (byte[].class == type) {
			f.set(this, src.createByteArray());
		    } else if (short[].class == type) {
			short[] result = null;
			int[] data = src.createIntArray();
			if (data != null) {
			    result = new short[data.length];
			    for (int i=0; i < data.length; i++) {
				result[i] = (short) data[i];
			    }
			}
			f.set(this, result);
		    } else if (int[].class == type) {
			f.set(this, src.createIntArray());
		    } else if (long[].class == type) {
			f.set(this, src.createLongArray());
		    } else if (float[].class == type) {
			f.set(this, src.createFloatArray());
		    } else if (double[].class == type) {
			f.set(this, src.createDoubleArray());
		    } else if (char[].class == type) {
			f.set(this, src.createCharArray());
		    }
		} else if (type.isPrimitive()) {
		    if (boolean.class == type) {
			f.set(this, src.readByte() != 0);
		    } else if (byte.class == type) {
			f.set(this, src.readByte());
		    } else if (short.class == type) {
			f.set(this, (short) src.readInt());
		    } else if (int.class == type) {
			f.set(this, src.readInt());
		    } else if (long.class == type) {
			f.set(this, src.readLong());
		    } else if (float.class == type) {
			f.set(this, src.readFloat());
		    } else if (double.class == type) {
			f.set(this, src.readDouble());
		    } else if (char.class == type) {
			f.set(this, src.createCharArray()[0]);
		    }
		} else {
		    Object value = f.get(this);
		    if (Parcelable.class.isAssignableFrom(type)) {
			f.set(this, src.readParcelable(this.getClass().getClassLoader()));
		    } else if (List.class.isAssignableFrom(type)) {
			ArrayList list = new ArrayList();
			src.readList(list, this.getClass().getClassLoader());
			f.set(this, list);
		    } else if (Map.class.isAssignableFrom(type)) {
			HashMap map = new HashMap();
			src.readMap(map, this.getClass().getClassLoader());
			f.set(this, map);
		    } else if (String.class.isAssignableFrom(type)) {
			f.set(this, src.readString());
		    } else {
			throw new IllegalArgumentException("Invalid type annotated with @Serialize.");
		    }
		}
	    }
	} catch (IllegalAccessException ex) {
	    throw new IllegalArgumentException("Failed to write filed value.", ex);
	}
    }

    @Override
    public final void writeToParcel(Parcel dest, int flags) throws IllegalArgumentException {
	try {
	    for (Field f : getFields()) {
		try {
		    f.setAccessible(true);
		} catch (SecurityException ex) {
		    LOG.debug("Faild to set accessibility flag of the class");
		}

		Class<?> type = f.getType();
		if (type.isArray()) {
		    Object obj = f.get(this);
		    if (boolean[].class == type) {
			dest.writeBooleanArray((boolean[]) obj);
		    } else if (byte[].class == type) {
			dest.writeByteArray((byte[]) obj);
		    } else if (short[].class == type) {
			if (obj != null) {
			    short[] data = (short[]) obj;
			    int[] result = new int[data.length];
			    for (int i=0; i < data.length; i++) {
				result[i] = data[i];
			    }
			    obj = result;
			}
			dest.writeIntArray((int[]) obj);
		    } else if (int[].class == type) {
			dest.writeIntArray((int[]) obj);
		    } else if (long[].class == type) {
			dest.writeLongArray((long[]) obj);
		    } else if (float[].class == type) {
			dest.writeFloatArray((float[]) obj);
		    } else if (double[].class == type) {
			dest.writeDoubleArray((double[]) obj);
		    } else if (char[].class == type) {
			dest.writeCharArray((char[]) obj);
		    }
		} else if (type.isPrimitive()) {
		    if (boolean.class == type) {
			dest.writeByte((byte) (f.getBoolean(this) ? 1 : 0));
		    } else if (byte.class == type) {
			dest.writeByte(f.getByte(this));
		    } else if (short.class == type) {
			dest.writeInt(f.getShort(this));
		    } else if (int.class == type) {
			dest.writeInt(f.getInt(this));
		    } else if (long.class == type) {
			dest.writeLong(f.getLong(this));
		    } else if (float.class == type) {
			dest.writeFloat(f.getFloat(this));
		    } else if (double.class == type) {
			dest.writeDouble(f.getDouble(this));
		    } else if (char.class == type) {
			dest.writeCharArray(new char[]{f.getChar(this)});
		    }
		} else {
		    Object value = f.get(this);
		    if (Parcelable.class.isInstance(value)) {
			dest.writeParcelable((Parcelable) value, flags);
		    } else if (List.class.isInstance(value)) {
			dest.writeList((List) value);
		    } else if (Map.class.isInstance(value)) {
			dest.writeMap((Map) value);
		    } else if (String.class.isInstance(value)) {
			dest.writeString((String) value);
		    } else {
			throw new IllegalArgumentException("Invalid type annotated with @Serialize.");
		    }
		}
	    }
	} catch (IllegalAccessException ex) {
	    throw new IllegalArgumentException("Inaccessible field annotated.", ex);
	}
    }

    private List<Field> getFields() {
	Class<?> clazz = this.getClass();
	ArrayList<Field> fields = new ArrayList<>();

	// extract all Serialize annotated fields down to this class
	do {
	    fields.addAll(getFields(clazz));
	    clazz = clazz.getSuperclass();
	} while (clazz != null && ! clazz.equals(AbstractParcelable.class));

	// sort fields -- hoping there are no duplicates and no hash collisions
	Collections.sort(fields, new Comparator<Field>() {
	    @Override
	    public int compare(Field o1, Field o2) {
		int diff = o1.hashCode() - o2.hashCode();
		return diff;
	    }
	});

	return fields;
    }

    private List<Field> getFields(Class<?> clazz) {
	ArrayList<Field> result = new ArrayList<>();
	for (Field next: clazz.getDeclaredFields()) {
	    if (next.getAnnotation(Serialize.class) != null) {
		result.add(next);
	    }
	}
	return result;
    }

}
