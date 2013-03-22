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
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import org.openecard.android.ApplicationContext;
import org.openecard.android.R;
import org.openecard.common.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This Activity shows the log in a scrollable textarea.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class LogViewerActivity extends Activity {

    private static Logger logger = LoggerFactory.getLogger(DeviceOpenActivity.class);
    private final I18n lang = I18n.getTranslation("android");

    private static final String LOG = "/sdcard/.openecard/android_info.log";
    private ApplicationContext applicationContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.logviewactivity);
	TextView logText = (TextView) findViewById(R.id.logText);
	logText.setVerticalScrollBarEnabled(true);
	logText.setMovementMethod(new ScrollingMovementMethod());
	loadLog(logText);
	applicationContext = (ApplicationContext) this.getApplicationContext();
    }

    private void loadLog(TextView logText) {
	FileInputStream stream = null;
	try {
	    stream = new FileInputStream(new File(LOG));
	    FileChannel fc = stream.getChannel();
	    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
	    String log = Charset.defaultCharset().decode(bb).toString();
	    logText.setText(log, BufferType.NORMAL);
	} catch (FileNotFoundException e) {
	    logger.error("Log file could not be found.", e);
	} catch (IOException e) {
	    logger.error("Mapping failed.", e);
	} finally {
	    try {
		stream.close();
	    } catch (IOException e) {
		logger.error("Closing the log file input stream failed.", e);
	    }
	}
    }

    @Override
    protected void onDestroy() {
	applicationContext.shutdown();
	super.onDestroy();
	System.exit(0);
    }

}
