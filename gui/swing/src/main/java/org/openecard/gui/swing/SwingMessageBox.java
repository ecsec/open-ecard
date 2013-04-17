/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
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

package org.openecard.gui.swing;

import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.openecard.common.I18n;
import org.openecard.gui.MessageBox;
import org.openecard.gui.messagebox.MessageBoxResult;
import org.openecard.gui.swing.common.GUIDefaults;


/**
 * Swing based MessageBox implementation.
 * This implementation wraps the {@link JOptionPane} class.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class SwingMessageBox implements MessageBox {


    private final I18n lang = I18n.getTranslation("gui");
    private static final Image frameIcon = GUIDefaults.getImage("Frame.icon", 45, 45).getImage();
    private final String DEFAULT_TITLE_INPUT;
    private final String DEFAULT_TITLE_MESSAGE;
    private final String DEFAULT_TITLE_CONFIRM;

    public SwingMessageBox() {
	DEFAULT_TITLE_INPUT = lang.translationForKey("default_title_input");
	DEFAULT_TITLE_MESSAGE = lang.translationForKey("default_title_message");
	DEFAULT_TITLE_CONFIRM = lang.translationForKey("default_title_confirm");
    }

    @Override
    public MessageBoxResult showMessage(String msg) {
	JOptionPane jop = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE);
	JDialog dialog = jop.createDialog(DEFAULT_TITLE_MESSAGE);
	dialog.setIconImage(frameIcon);
	dialog.setVisible(true);
	return new MessageBoxResult(MessageBox.OK);
    }

    @Override
    public MessageBoxResult showMessage(String msg, String title, int msgType) {
	JOptionPane jop = new JOptionPane(msg, msgType);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	dialog.setVisible(true);
	return new MessageBoxResult(MessageBox.OK);
    }

    @Override
    public MessageBoxResult showMessage(String msg, String title, int msgType, byte[] iconData) {
	ImageIcon icon = new ImageIcon(iconData);
	JOptionPane jop = new JOptionPane(msg, msgType, JOptionPane.DEFAULT_OPTION, icon);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	dialog.setVisible(true);
	return new MessageBoxResult(MessageBox.OK);
    }

    @Override
    public MessageBoxResult showConfirmDialog(String msg) {
	JOptionPane jop = new JOptionPane(msg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
	JDialog dialog = jop.createDialog(DEFAULT_TITLE_CONFIRM);
	dialog.setIconImage(frameIcon);
	dialog.setVisible(true);

	Object returnValue = jop.getValue();
	if (returnValue == null) {
	    return new MessageBoxResult(-1);
	} else {
	    return new MessageBoxResult((Integer) returnValue);
	}
    }

    @Override
    public MessageBoxResult showConfirmDialog(String msg, String title, int optionType) {
	JOptionPane jop = new JOptionPane(msg, JOptionPane.QUESTION_MESSAGE, optionType);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	dialog.setVisible(true);

	Object returnValue = jop.getValue();
	if (returnValue == null) {
	    return new MessageBoxResult(-1);
	} else {
	    return new MessageBoxResult((Integer) returnValue);
	}
    }

    @Override
    public MessageBoxResult showConfirmDialog(String msg, String title, int optionType, int msgType) {
	JOptionPane jop = new JOptionPane(msg, msgType, optionType);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	dialog.setVisible(true);

	Object returnValue = jop.getValue();
	if (returnValue == null) {
	    return new MessageBoxResult(-1);
	} else {
	    return new MessageBoxResult((Integer) returnValue);
	}
    }

    @Override
    public MessageBoxResult showConfirmDialog(String msg, String title, int optionType, int msgType, byte[] iconData) {
	ImageIcon icon = new ImageIcon(iconData);
	JOptionPane jop = new JOptionPane(msg, msgType, optionType, icon);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	dialog.setVisible(true);

	Object returnValue = jop.getValue();
	if (returnValue == null) {
	    return new MessageBoxResult(-1);
	} else {
	    return new MessageBoxResult((Integer) returnValue);
	}
    }

    @Override
    public MessageBoxResult showInputDialog(String msg) {
	JOptionPane jop = new JOptionPane(msg, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
	JDialog dialog = jop.createDialog(DEFAULT_TITLE_INPUT);
	dialog.setIconImage(frameIcon);
	jop.setWantsInput(true);
	dialog.setVisible(true);
	Object returnValue = jop.getInputValue();
	if (returnValue == null) {
	    return new MessageBoxResult(null);
	} else {
	    return new MessageBoxResult((String) returnValue);
	}
    }

    @Override
    public MessageBoxResult showInputDialog(String msg, String initialSelectionValue) {
	JOptionPane jop = new JOptionPane(msg, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
	JDialog dialog = jop.createDialog(DEFAULT_TITLE_INPUT);
	dialog.setIconImage(frameIcon);
	jop.setInitialSelectionValue(initialSelectionValue);
	jop.setWantsInput(true);
	dialog.setVisible(true);
	Object returnValue = jop.getInputValue();
	if (returnValue == null) {
	    return new MessageBoxResult(null);
	} else {
	    return new MessageBoxResult((String) returnValue);
	}
    }

    @Override
    public MessageBoxResult showInputDialog(String msg, String title, int msgType) {
	JOptionPane jop = new JOptionPane(msg, msgType, JOptionPane.OK_CANCEL_OPTION);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	jop.setWantsInput(true);
	dialog.setVisible(true);
	Object returnValue = jop.getInputValue();
	if (returnValue == null) {
	    return new MessageBoxResult(null);
	} else {
	    return new MessageBoxResult((String) returnValue);
	}
    }

    @Override
    public MessageBoxResult showInputDialog(String msg, String title, int msgType, byte[] iconData, String[] selectionValues, String initialSelectionValue) {
	ImageIcon icon = new ImageIcon(iconData);
	JOptionPane jop = new JOptionPane(msg, msgType, JOptionPane.OK_CANCEL_OPTION, icon);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	jop.setSelectionValues(selectionValues);
	jop.setInitialSelectionValue(initialSelectionValue);
	jop.setWantsInput(true);
	dialog.setVisible(true);
	Object returnValue = jop.getInputValue();
	if (returnValue == null) {
	    return new MessageBoxResult(null);
	} else {
	    return new MessageBoxResult((String) returnValue);
	}
    }

    @Override
    public MessageBoxResult showOptionDialog(String msg, String title, int optionType, int msgType, byte[] iconData, String[] options, String initialValue) {
	ImageIcon icon = new ImageIcon(iconData);
	JOptionPane jop = new JOptionPane(msg, msgType, optionType, icon, options, initialValue);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	dialog.setVisible(true);
	Object returnValue = jop.getValue();
	if (returnValue == null) {
	    return new MessageBoxResult(MessageBox.CLOSED);
	}
	if (options == null) {
	    if (returnValue instanceof Integer) {
		return new MessageBoxResult(((Integer) returnValue).intValue());
	    }
	    return new MessageBoxResult(MessageBox.CLOSED);
	}
	for (int counter = 0, maxCounter = options.length;
		counter < maxCounter; counter++) {
	    if (options[counter].equals(returnValue)) {
		return new MessageBoxResult(counter);
	    }
	}
	return new MessageBoxResult(MessageBox.CLOSED);
    }

}
