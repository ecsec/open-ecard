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

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import org.openecard.android.ApplicationContext;
import org.openecard.android.R;
import org.openecard.common.I18n;
import org.openecard.recognition.CardRecognition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A TerminalFragment shows a gui representation of the state of the terminal it belongs to.
 * 
 * @author Dirk Petrautzki
 */
public class TerminalFragment extends Fragment {

    // logger and translation
    private static final Logger logger = LoggerFactory.getLogger(TerminalFragment.class);
    private final I18n lang = I18n.getTranslation("android");

    // gui constants
    private static final String TERMINAL = "android.cardinfo.terminal";
    private static final String CARDTYPE = "android.cardinfo.type";
    private static final String NO_CARD = "android.cardinfo.cardname";

    private ImageView imageView;
    private TextView textCardType;
    private TextView labelCardType;
    private TextView labelTerminal;
    private TextView textTerminal;
    private String ifdName;
    private CardRecognition recognition;

    /**
     * Creates a reusable fragment representing the state of a terminal and inserted cards.
     * Fragments need to provide an empty default constructor.
     */
    public TerminalFragment() {}

    public void setIFDName(String ifdName) {
	this.ifdName = ifdName;
    }

    public void setRecognition(CardRecognition recognition) {
	this.recognition = recognition;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	View view = inflater.inflate(R.layout.terminalfragment, container, false);

	// get views from layout
	imageView = (ImageView) view.findViewById(R.id.imageView_card);
	textCardType = (TextView) view.findViewById(R.id.text_cardType);
	labelCardType = (TextView) view.findViewById(R.id.label_cardType);
	labelTerminal = (TextView) view.findViewById(R.id.label_terminal);
	textTerminal = (TextView) view.findViewById(R.id.text_terminal);

	// set default content
	textTerminal.setText(ifdName);
	labelCardType.setText(lang.translationForKey(CARDTYPE));
	textCardType.setText(lang.translationForKey(NO_CARD));
	labelTerminal.setText(lang.translationForKey(TERMINAL));
	Drawable drawable;
	if (((ApplicationContext) this.getActivity().getApplicationContext()).usingNFC()) {
	    try {
		drawable = Drawable.createFromStream(this.getActivity().getAssets().open("NFC-logo.png"), null);
	    } catch (IOException e) {
		logger.error("Coudn't load nfc logo; using default no card image.", e);
		InputStream is = recognition.getNoCardImage();
		drawable = Drawable.createFromStream(is, null);
	    }
	} else {
	    InputStream is = recognition.getNoCardImage();
	    drawable = Drawable.createFromStream(is, null);
	}
	imageView.setImageDrawable(drawable);

	return view;
    }

    /**
     * Updates the UI with the new cardImage and CardType.
     * 
     * @param cardImage image to use
     * @param cardType card type as string
     */
    public void updateFragment(final Drawable cardImage, final String cardType) {
	this.imageView.setImageDrawable(cardImage);
	this.textCardType.setText(cardType);
    }

}
