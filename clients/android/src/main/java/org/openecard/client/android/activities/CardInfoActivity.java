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

package org.openecard.client.android.activities;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import org.openecard.client.android.ApplicationContext;
import org.openecard.client.android.R;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.EventCallback;


/**
 * This Activity shows information about the currently used ecard.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CardInfoActivity extends Activity implements EventCallback {

    private ApplicationContext applicationContext;
    private ImageView imageView;
    private TextView textCardType;

    @Override
    public void onCreate(Bundle savedInstanceState) {

	super.onCreate(savedInstanceState);

	// Set up the window layout
	setContentView(R.layout.cardinfo);

	// initialize and register for events
	applicationContext = ((ApplicationContext) getApplicationContext());
	applicationContext.initialize();
	applicationContext.getEnv().getEventManager().registerAllEvents(this);
	imageView = (ImageView) findViewById(R.id.imageView_card);
	textCardType  = (TextView) findViewById(R.id.text_cardType);
	Button b = (Button) findViewById(R.id.button_back);
	b.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		finish();
	    }
	});
 
    }
    
    @Override
    protected void onDestroy() {
	applicationContext.shutdown();
	super.onDestroy();
    }
        
    @Override
    public void signalEvent(EventType eventType, Object eventData) {
	if (eventType.equals(EventType.CARD_RECOGNIZED)) {
	    if (eventData instanceof ConnectionHandleType) {
		ConnectionHandleType ch = (ConnectionHandleType) eventData;
		InputStream is = applicationContext.getRecognition()
			.getCardImage(ch.getRecognitionInfo().getCardType());
		Drawable drawable = Drawable.createFromStream(is, null);
		updateUI(drawable, ch.getRecognitionInfo().getCardType());
	    }
	} else if (eventType.equals(EventType.CARD_REMOVED)) {
	    InputStream is = applicationContext.getRecognition().getNoCardImage();
	    Drawable drawable = Drawable.createFromStream(is, null);
	    updateUI(drawable, this.getString(R.string.text_cardTye));
	}
    }

    /**
     * Updates the UI with the new cardImage and CardType.
     * @param cardImage image to use
     * @param cardType card type as string
     */
    private void updateUI(final Drawable cardImage, final String cardType) {
	runOnUiThread(new Runnable() {
	    public void run() {
		imageView.setImageDrawable(cardImage);
		textCardType.setText(cardType);
	    }
	});
    }

}
