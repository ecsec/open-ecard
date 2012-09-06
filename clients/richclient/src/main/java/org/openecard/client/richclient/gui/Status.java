/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.richclient.gui;

import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.CardTypeType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import oasis.names.tc.dss._1_0.core.schema.InternationalStringType;
import org.openecard.client.common.I18n;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.EventCallback;
import org.openecard.client.recognition.CardRecognition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public class Status implements EventCallback {

    private static final Logger logger = LoggerFactory.getLogger(Status.class);

    private static final String NO_TERMINAL_CONNECTED = "noTerminalConnected";

    private final I18n lang = I18n.getTranslation("richclient");

    private Map<String, JPanel> infoMap = new ConcurrentSkipListMap<String, JPanel>();
    private HashMap<String, ImageIcon> cardIcons = new HashMap<String, ImageIcon>();
    private Container contentPane;
    private JPanel infoView;
    private JPanel noTerminal;
    private InfoPopup popup;
    private final CardRecognition recognition;

    public Status(CardRecognition recognition) {
	this.recognition = recognition;
	setupBaseUI();
    }

    public void showInfo() {
	showInfo(null);
    }

    public void showInfo(Point p) {
	if (popup != null) {
	    popup.dispose();
	}
	popup = new InfoPopup(contentPane, p);
    }


    private void setupBaseUI() {
	contentPane = new Container();
	contentPane.setLayout(new BorderLayout());
	contentPane.setBackground(Color.white);

	noTerminal = new JPanel();
	noTerminal.setLayout(new FlowLayout(FlowLayout.LEFT));
	noTerminal.setBackground(Color.white);
	noTerminal.add(createInfoLabel());
	infoMap.put(NO_TERMINAL_CONNECTED, noTerminal);

	infoView = new JPanel();
	infoView.setLayout(new BoxLayout(infoView, BoxLayout.PAGE_AXIS));
	infoView.setBackground(Color.white);
	infoView.add(Box.createRigidArea(new Dimension(0, 5)));
	infoView.add(noTerminal);

	JLabel label = new JLabel(" " + lang.translationForKey("tray.title") + " ");
	label.setBackground(Color.white);
	label.setFont(new Font("Dialog", Font.BOLD, 20));

	contentPane.add(label, BorderLayout.NORTH);
	contentPane.add(infoView, BorderLayout.CENTER);
    }

    private synchronized void addInfo(String ifdName, RecognitionInfo info) {
	if (infoMap.containsKey(NO_TERMINAL_CONNECTED)) {
	    infoMap.remove(NO_TERMINAL_CONNECTED);
	    infoView.removeAll();
	}

	JPanel panel = new JPanel();
	panel.setLayout(new FlowLayout(FlowLayout.LEFT));
	panel.add(createInfoLabel(ifdName, info));
	infoMap.put(ifdName, panel);
	infoView.add(panel);

	if (popup != null) {
	    popup.updateContent(contentPane);
	}
    }

    private synchronized void updateInfo(String ifdName, RecognitionInfo info) {
	JPanel panel = infoMap.get(ifdName);
	if (panel != null) {
	    panel.removeAll();
	    panel.add(createInfoLabel(ifdName, info));

	    if (popup != null) {
		popup.updateContent(contentPane);
	    }
	}
    }

    private synchronized void removeInfo(String ifdName) {
	JPanel panel = infoMap.get(ifdName);
	if (panel != null) {
	    infoMap.remove(ifdName);
	    infoView.remove(panel);

	    if (infoMap.isEmpty()) {
		infoMap.put(NO_TERMINAL_CONNECTED, noTerminal);
		infoView.add(noTerminal);
	    }

	    if (popup != null) {
		popup.updateContent(contentPane);
	    }
	}
    }

    private synchronized ImageIcon getCardIcon(String cardType) {
	if (cardType == null) {
	    cardType = "http://openecard.org/cif/no-card";
	}

	if (! cardIcons.containsKey(cardType)) {
	    InputStream is = recognition.getCardImage(cardType);
	    if (is == null) {
		is = recognition.getUnknownCardImage();
	    }
	    ImageIcon icon = GuiUtils.getImageIcon(is);
	    cardIcons.put(cardType, icon);
	}

	return cardIcons.get(cardType);
    }

    private String getCardType(RecognitionInfo info) {
	if (info != null) {
	    String cardType = info.getCardType();

	    if (cardType != null) {
		return resolveCardType(cardType);
	    } else {
		return lang.translationForKey("status.nocard");
	    }
	} else {
	    return lang.translationForKey("status.nocard");
	}
    }

    private String resolveCardType(String cardType) {
	if (cardType.equals("http://bsi.bund.de/cif/unknown")) {
	    return lang.translationForKey("status.unknowncard");
	} else {
	    // read CardTypeName from CardInfo file
	    CardInfoType cif = recognition.getCardInfo(cardType);
	    String cardTypeName = cardType;

	    if (cif != null) {
		CardTypeType type = cif.getCardType();
		if (type != null) {
		    boolean found = false;
		    String[] languages = new String[] {Locale.getDefault().getLanguage(), "en"};

		    // check native lang, then english
		    for (String language : languages) {
			if (found) { // stop when the inner loop terminated
			    break;
			}

			List<InternationalStringType> cardTypeNames = type.getCardTypeName();
			for (InternationalStringType ist : cardTypeNames) {
			    if (ist.getLang().equalsIgnoreCase(language)) {
				cardTypeName = ist.getValue();
				found = true;
				break;
			    }
			}
		    }
		}
	    }

	    return cardTypeName;
	}
    }

    private JLabel createInfoLabel() {
	return createInfoLabel(null, null);
    }

    private JLabel createInfoLabel(String ifdName, RecognitionInfo info) {
	JLabel label = new JLabel();

	if (ifdName != null) {
	    String cardType = info != null ? info.getCardType() : "http://openecard.org/cif/no-card";
	    label.setIcon(getCardIcon(cardType));
	    label.setText("<html><b>" + getCardType(info) + "</b><br><i>" + ifdName + "</i></html>");
	} else {
	    // no_terminal.png is based on klaasvangend_USB_plug.svg by klaasvangend
	    // see: http://openclipart.org/detail/3705/usb-plug-by-klaasvangend
	    label.setIcon(getCardIcon("http://openecard.org/cif/no-terminal"));
	    label.setText("<html><i>" + lang.translationForKey("status.noterminal") + "</i></html>");
	}

	label.setIconTextGap(10);
	label.setBackground(Color.white);
	return label;
    }

    @Override
    public void signalEvent(EventType eventType, Object eventData) {
	if (eventData instanceof ConnectionHandleType) {
	    ConnectionHandleType ch = (ConnectionHandleType) eventData;
	    RecognitionInfo info = ch.getRecognitionInfo();
	    String ifdName = ch.getIFDName();

	    if (eventType.equals(EventType.TERMINAL_ADDED)) {
		addInfo(ifdName, info);
	    } else if (eventType.equals(EventType.TERMINAL_REMOVED)) {
		removeInfo(ifdName);
	    } else {
		updateInfo(ifdName, info);
	    }
	}
    }

}
