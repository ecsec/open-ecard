/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openecard.gui.android.stub;

import org.openecard.gui.MessageDialog;
import org.openecard.gui.message.DialogType;
import org.openecard.gui.message.MessageDialogResult;
import org.openecard.gui.message.OptionType;
import org.openecard.gui.message.ReturnType;

/**
 *
 * @author Mike Prechtl
 */
public class MessageDialogStub implements MessageDialog {

    @Override
    public MessageDialogResult showMessageDialog(String message, String title) {
	return new MessageDialogResult(ReturnType.OK);
    }

    @Override
    public MessageDialogResult showMessageDialog(String message, String title, DialogType messageType) {
	return new MessageDialogResult(ReturnType.OK);
    }

    @Override
    public MessageDialogResult showMessageDialog(String message, String title, DialogType messageType, byte[] iconData) {
	return new MessageDialogResult(ReturnType.OK);
    }

    @Override
    public MessageDialogResult showConfirmDialog(String message, String title) {
	return new MessageDialogResult(ReturnType.OK);
    }

    @Override
    public MessageDialogResult showConfirmDialog(String message, String title, OptionType optionType) {
	return new MessageDialogResult(ReturnType.OK);
    }

    @Override
    public MessageDialogResult showConfirmDialog(String message, String title, OptionType optionType, DialogType messageType) {
	return new MessageDialogResult(ReturnType.OK);
    }

    @Override
    public MessageDialogResult showConfirmDialog(String message, String title, OptionType optionType, DialogType messageType, byte[] iconData) {
	return new MessageDialogResult(ReturnType.OK);
    }

    @Override
    public MessageDialogResult showInputDialog(String message, String title) {
	return new MessageDialogResult(ReturnType.OK);
    }

    @Override
    public MessageDialogResult showInputDialog(String message, String title, String initialValue) {
	return new MessageDialogResult(ReturnType.OK);
    }

    @Override
    public MessageDialogResult showInputDialog(String message, String title, DialogType messageType, String initialValue) {
	return new MessageDialogResult(ReturnType.OK);
    }

    @Override
    public MessageDialogResult showInputDialog(String message, String title, DialogType messageType, byte[] iconData, int initialSelectionIndex, String... selectionValues) {
	return new MessageDialogResult(ReturnType.OK);
    }

    @Override
    public MessageDialogResult showOptionDialog(String message, String title, OptionType optionType, DialogType messageType, byte[] iconData, String... options) {
	return new MessageDialogResult(ReturnType.OK);
    }

}
