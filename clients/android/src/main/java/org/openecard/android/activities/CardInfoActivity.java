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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.io.InputStream;
import org.openecard.android.ApplicationContext;
import org.openecard.android.R;
import org.openecard.common.I18n;
import org.openecard.common.enums.EventType;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.recognition.CardRecognition;


/**
 * This Activity shows information about the currently used ecard.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CardInfoActivity extends Activity implements EventCallback {

    private final I18n lang = I18n.getTranslation("android");

    private ApplicationContext appContext;
    private CardRecognition recognition;
    private ImageView imageView;
    private TextView textCardType;
    private TextView textInfo;
    private TextView labelCardType;

    @Override
    public void onCreate(Bundle savedInstanceState) {

	super.onCreate(savedInstanceState);

	// Set up the window layout
	setContentView(R.layout.cardinfo);

	// register for events
	appContext = ((ApplicationContext) getApplicationContext());
	appContext.getEnv().getEventManager().registerAllEvents(this);

	recognition = appContext.getRecognition();
	imageView = (ImageView) findViewById(R.id.imageView_card);
	textCardType = (TextView) findViewById(R.id.text_cardType);
	textInfo = (TextView) findViewById(R.id.label_info);
	labelCardType = (TextView) findViewById(R.id.label_cardType);
	labelCardType.setText(lang.translationForKey("android.cardinfo.type"));
	textInfo.setText(lang.translationForKey("android.cardinfo.request"));
	textCardType.setText(lang.translationForKey("android.cardinfo.cardname"));

	// test if there is already a card available and show the appropriate info
	ConnectionHandleType cHandle = new ConnectionHandleType();
	CardStateEntry entry = appContext.getCardStates().getEntry(cHandle);

	if (entry == null) {
	    InputStream is = recognition.getNoCardImage();
	    Drawable drawable = Drawable.createFromStream(is, null);
	    updateUI(drawable, lang.translationForKey("android.cardinfo.cardname"), true);
	    return;
	} else {
	    cHandle = entry.handleCopy();
	    String cardType = cHandle.getRecognitionInfo().getCardType();
	    InputStream is = recognition.getCardImage(cardType);
	    Drawable drawable = Drawable.createFromStream(is, null);
	    updateUI(drawable, recognition.getTranslatedCardName(cardType), false);
	}

    }

    @Override
    protected void onDestroy() {
	appContext.shutdown();
	super.onDestroy();
    }

    @Override
    public void signalEvent(EventType eventType, Object eventData) {
	if (eventType.equals(EventType.CARD_RECOGNIZED)) {
	    if (eventData instanceof ConnectionHandleType) {
		ConnectionHandleType ch = (ConnectionHandleType) eventData;
		String cardType = ch.getRecognitionInfo().getCardType();
		InputStream is = recognition.getCardImage(cardType);
		Drawable drawable = Drawable.createFromStream(is, null);
		updateUI(drawable, recognition.getTranslatedCardName(cardType), false);
	    }
	} else if (eventType.equals(EventType.CARD_REMOVED)) {
	    InputStream is = recognition.getNoCardImage();
	    Drawable drawable = Drawable.createFromStream(is, null);
	    updateUI(drawable, lang.translationForKey("android.cardinfo.cardname"), true);
	}
    }

    /**
     * Updates the UI with the new cardImage and CardType.
     *
     * @param cardImage image to use
     * @param cardType card type as string
     * @param infoVisible true if the info requesting a card is visible, else false
     */
    private void updateUI(final Drawable cardImage, final String cardType, final boolean infoVisible) {
	runOnUiThread(new Runnable() {
	    public void run() {
		imageView.setImageDrawable(cardImage);
		textCardType.setText(cardType);
		if (infoVisible) {
		    textInfo.setVisibility(View.VISIBLE);
		} else {
		    textInfo.setVisibility(View.INVISIBLE);
		}
	    }
	});
    }

}
