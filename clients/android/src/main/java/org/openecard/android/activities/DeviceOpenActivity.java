/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.android.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.XmlResourceParser;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Xml;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import org.openecard.android.AndroidUtils;
import org.openecard.android.ApplicationContext;
import org.openecard.android.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


/**
 * This Activity finds and opens usb devices and starts the socket communication with libusb.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class DeviceOpenActivity extends Activity {

    private static Logger logger = LoggerFactory.getLogger(DeviceOpenActivity.class);

    private static final String USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String PRODUCT_ID = "product-id";
    private static final String VENDOR_ID = "vendor-id";

    private static Intent intent;
    private static HashMap<String, Integer> fileDescriptors = new HashMap<String, Integer>();
    private static Thread t;
    private static PendingIntent permissionIntent;

    private final BroadcastReceiver mUsbReceiver = new UsbPermissionReceiver();

    private String fdSocket;
    private String pathSocket;

    static {
	System.loadLibrary("LibusbSocketCommunicator");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	intent = getIntent();
	// finish, we've been only started to lay on top of the activity stack for the next start
	if (intent.getBooleanExtra(AndroidUtils.EXIT, false)) {
	    finish();
	}
	fdSocket = getFilesDir().getAbsolutePath() + "/socket";
	pathSocket = getFilesDir().getAbsolutePath() + "/socket2";
	if (t == null) {
	    t = new Thread(new SocketCommunicationRunnable());
	    t.start();
	}
    }

    @Override
    protected void onStart() {
	super.onStart();
	permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(USB_PERMISSION), 0);
	registerReceiver(mUsbReceiver, new IntentFilter(USB_PERMISSION));
	findDevice(DeviceOpenActivity.this);
    }

    @Override
    protected void onStop() {
	super.onStop();
	unregisterReceiver(mUsbReceiver);
    }

    /**
     * Opens a connection to a certain usb device and stores its file descriptor.
     * 
     * @param device the usb device to open
     */
    public void openDevice(final UsbDevice device) {
	final UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

	if (device != null && !manager.hasPermission(device)) {
	    logger.debug("Requesting permission for device {}", device.getDeviceName());
	    manager.requestPermission(device, permissionIntent);
	    return;
	}

	if (device == null || !manager.hasPermission(device)) {
	    return;
	}

	final UsbDeviceConnection connection = manager.openDevice(device);
	if (connection != null) {
	    logger.debug("Adding file descriptor {} for {}", connection.getFileDescriptor(), device.getDeviceName());
	    fileDescriptors.put(device.getDeviceName(), connection.getFileDescriptor());
	}
    }

    /**
     * Gets a usb device via intent or the first one returned from the usb service and opens it.
     * 
     * @param ctx Context of the DeviceOpenActivity
     */
    public static void findDevice(final DeviceOpenActivity ctx) {
	final UsbManager manager = (UsbManager) ctx.getSystemService(Context.USB_SERVICE);
	UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

	if (device == null) {
	    final HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
	    HashSet<String> allowed = getDeviceData(ctx);
	    logger.debug("No device in intent; iterating through device list.");
	    for (String desc : deviceList.keySet()) {
		UsbDevice usbDevice = deviceList.get(desc);
		String candstr = VENDOR_ID + usbDevice.getVendorId() + PRODUCT_ID
			+ usbDevice.getProductId();
		if (allowed.contains(candstr)) {
		    logger.debug("opening device {}", desc);
		    ctx.openDevice(usbDevice);
		}
	    }

	} else {
	    ctx.openDevice(device);
	}
	// no device found, just start without one
	if (!((ApplicationContext) ctx.getApplicationContext()).isInitialized()) {
	    Intent i = new Intent(ctx, AboutActivity.class);
	    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
	    ctx.startActivity(i);
	}
	ctx.finish();
    }

    /**
     * Parses the device_filter.xml-file and returns a set containing all allowed devices.
     * 
     * @param ctx ApplicationContext to get the xml resource
     * @return a HashSet containing the allowed devices from the device list file
     */
    private static HashSet<String> getDeviceData(final Context ctx) {
	HashSet<String> allowed = new HashSet<String>();

	XmlResourceParser xml = ctx.getResources().getXml(R.xml.device_filter);

	try {
	    xml.next();

	    int eventType;
	    while ((eventType = xml.getEventType()) != XmlPullParser.END_DOCUMENT) {

		switch (eventType) {
		    case XmlPullParser.START_TAG:
			if (xml.getName().equals("usb-device")) {
			    AttributeSet as = Xml.asAttributeSet(xml);
			    String vendorId = as.getAttributeValue(null, VENDOR_ID);
			    String productId = as.getAttributeValue(null, PRODUCT_ID);
			    allowed.add(VENDOR_ID + vendorId + PRODUCT_ID + productId);
			}
			break;
		}
		xml.next();
	    }
	} catch (XmlPullParserException e) {
	    logger.error("Error parsing device filter xml.", e);
	} catch (IOException e) {
	    logger.error("Error parsing device filter xml.", e);
	}
	return allowed;
    }

    /**
     * Starts a socket for sending a usb device file descriptor.
     * 
     * @param location path of the socket
     * @param fd the file descriptor to pass
     */
    public static native void startUnixSocketServer(final String location, int fd);

    /**
     * Listens on a socket for an incoming usb device path.
     * 
     * @param location path of the socket
     * @return recieved device path
     */
    public static native String listenUnixSocketServer(final String location);

    /**
     * Opens an usb device after the user grants permission to use it.
     * 
     * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
     */
    private final class UsbPermissionReceiver extends BroadcastReceiver {
	public void onReceive(Context context, Intent intent) {
	    String action = intent.getAction();
	    if (USB_PERMISSION.equals(action)) {
		synchronized (this) {
		    final UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

		    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
			if (device != null) {
			    openDevice(device);
			} else {
			    logger.error("Device is null");
			}
		    } else {
			logger.error("Usb permission was not granted.");
		    }
		}
	    }
	}
    }

    /**
     * Never ending Runnable implementing the socket communication with libusb.
     * 
     * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
     */
    private final class SocketCommunicationRunnable implements Runnable {

	@Override
	public void run() {
	    while (true) {
		String devicePath = listenUnixSocketServer(pathSocket);
		logger.debug("Received a file descriptor request for device {}", devicePath);

		Integer deviceDescriptor = null;
		for (int i = 0; i < 10; i++) {
		    deviceDescriptor = fileDescriptors.get(devicePath);
		    if (deviceDescriptor != null) {
			logger.debug("returning device descriptor: {}", deviceDescriptor);
			startUnixSocketServer(fdSocket, deviceDescriptor);
			break;
		    }
		    try {
			logger.debug("No matching device descriptor recorded yet; waiting 1 second.");
			Thread.sleep(1000);
		    } catch (InterruptedException e) {
			// ignore
		    }
		}
		if (deviceDescriptor == null) {
		    logger.error("No matching device descriptor, returnning -1");
		    startUnixSocketServer(fdSocket, -1);
		}
	    }
	}
    }

}
