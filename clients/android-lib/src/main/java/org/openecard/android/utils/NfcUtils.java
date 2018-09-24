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

package org.openecard.android.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.TagTechnology;
import android.provider.Settings;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.openecard.android.ex.ApduExtLengthNotSupported;
import org.openecard.scio.NFCFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provides methods to enable/disable the nfc dispatch or to jump to the nfc settings, ...
 *
 * @author Mike Prechtl
 */
public class NfcUtils {

    private static final Logger LOG = LoggerFactory.getLogger(NfcUtils.class);

    private static NfcUtils nfcUtils;

    public static NfcUtils getInstance() {
	synchronized (NfcUtils.class) {
	    if (nfcUtils == null) {
		nfcUtils = new NfcUtils();
	    }
	}
	return nfcUtils;
    }

    /**
     * This method opens the nfc settings on the corresponding device. Now, the user can enable nfc.
     *
     * @param activity
     */
    public void goToNFCSettings(Activity activity) {
	Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
	activity.startActivityForResult(intent, 0);
    }

    public void enableNFCDispatch(Activity activity) {
	if (isNfcEnabled(activity)) {
	    LOG.debug("Enable NFC foreground dispatch...");
	    Intent activityIntent = new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, activityIntent, 0);
	    // enable dispatch of messages with nfc tag
	    NfcAdapter.getDefaultAdapter(activity).enableForegroundDispatch(activity, pendingIntent, null, null);
	}
    }

    public void disableNFCDispatch(Activity activity) {
	if (isNfcEnabled(activity)) {
	    LOG.debug("Disable NFC foreground dispatch...");
	    // disable dispatch of messages with nfc tag
	    NfcAdapter.getDefaultAdapter(activity).disableForegroundDispatch(activity);
	}
    }

    public void retrievedNFCTag(Intent intent) throws ApduExtLengthNotSupported {
	// indicates that a nfc tag is there
	Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	if (tagFromIntent != null) {
	    if (IsoDep.get(tagFromIntent).isExtendedLengthApduSupported()) {
		// set nfc tag with timeout of five seconds
		NFCFactory.setNFCTag(tagFromIntent, 5000);
	    } else {
		throw new ApduExtLengthNotSupported("APDU Extended Length is not supported.");
	    }
	}
    }

    public static boolean isNfcEnabled(Context ctx) {
	NFCFactory.setContext(ctx);
	return NFCFactory.isNFCEnabled();
    }

    public static boolean isNfcAvailable(Context ctx) {
	NFCFactory.setContext(ctx);
	return NFCFactory.isNFCAvailable();
    }


    public static boolean supportsExtendedLength(Context context) {
	NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
	if (nfcAdapter != null) {
	    Object tagObj = getTagObject(nfcAdapter);
	    if (tagObj != null) {
		Boolean extSup = isExtendedLengthSupported(tagObj);
		if (extSup != null) {
		    return extSup;
		}
		Integer maxLen = getMaxTransceiveLength(tagObj);
		if (maxLen != null) {
		    return maxLen > 370; // This is roughly the size of the biggest APDU observed in EAC
		}
		LOG.info("maxLen = {} ; extSup = {}", maxLen, extSup);
	    }
	} else {
	    LOG.warn("NfcAdapter is not available.");
	}

	return false;
    }

    private static Object getTagObject(NfcAdapter nfcAdapter) {
	try {
	    Method getTagFun = nfcAdapter.getClass().getMethod("getTagService");
	    Object tagObj = getTagFun.invoke(nfcAdapter);
	    return tagObj;
	} catch (NoSuchMethodException ex) {
	    LOG.error("Error requesting TagService retrieval method.", ex);
	} catch (SecurityException | IllegalAccessException ex) {
	    LOG.error("Requesting TagService object is not allowed.");
	} catch (InvocationTargetException ex) {
	    LOG.error("Error requesting TagService object.", ex);
	}

	return null;
    }

    private static Integer getMaxTransceiveLength(Object tagObj) {
	int tech = 3; // taken from Android source and used as fallback if lookup fails
	try {
	    Field isoDep = TagTechnology.class.getDeclaredField("ISO_DEP");
	    tech = isoDep.getInt(null);
	} catch (NoSuchFieldException ex) {
	    LOG.error("Error requesting ISO_DEP field.", ex);
	} catch (SecurityException | IllegalAccessException ex) {
	    LOG.error("Requesting ISO_DEP tech constant is not allowed.");
	} catch (NullPointerException | IllegalArgumentException ex) {
	    LOG.error("Invalid parameters for requesting ISO_DEP tech constant.", ex);
	}

	try {
	    Method tlenFun = tagObj.getClass().getMethod("getMaxTransceiveLength", int.class);
	    Object lenObj = tlenFun.invoke(tagObj, tech);
	    LOG.debug("Transceive Length == {}", lenObj);
	    if (lenObj instanceof Integer) {
		return (Integer) lenObj;
	    }
	} catch (NoSuchMethodException ex) {
	    LOG.debug("Error requesting max transceive length retrieval method.", ex);
	} catch (SecurityException | IllegalAccessException ex) {
	    LOG.debug("Requesting max transceive length is not allowed.");
	} catch (InvocationTargetException ex) {
	    LOG.debug("Error requesting max transceive length.", ex);
	}

	return null;
    }

    private static Boolean isExtendedLengthSupported(Object tagObj) {
	try {
	    Method extSupFun = tagObj.getClass().getMethod("getExtendedLengthApdusSupported");
	    Object extSupObj = extSupFun.invoke(tagObj);
	    LOG.debug("Extended Length Support == {}", extSupObj);
	    if (extSupObj instanceof Boolean) {
		return (Boolean) extSupObj;
	    }
	} catch (NoSuchMethodException ex) {
	    LOG.debug("Error requesting extended length support retrieval method.", ex);
	} catch (SecurityException | IllegalAccessException ex) {
	    LOG.debug("Requesting extended length support is not allowed.");
	} catch (InvocationTargetException ex) {
	    LOG.debug("Error requesting extended length support.", ex);
	}

	return null;
    }

}
