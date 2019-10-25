package org.openecard.mobile.activation.common.anonymous;

import org.openecard.mobile.activation.NFCOverlayMessageHandler;
import org.openecard.mobile.activation.common.NFCDialogMsgSetter;

public class NFCOverlayMessageHandlerImpl implements NFCOverlayMessageHandler {

    private final NFCDialogMsgSetter msgSetter;

    public NFCOverlayMessageHandlerImpl(NFCDialogMsgSetter msgSetter) {
	this.msgSetter = msgSetter;
    }

    @Override
    public void setText(String msg) {
	msgSetter.setText(msg);
    }
}
