package org.openecard.ios.activation;

import org.openecard.mobile.activation.common.NFCDialogMsgSetter;
import org.openecard.scio.IOSNFCFactory;

/**
 *
 * @author Florian Otto
 */
public class IOSNFCDialogMsgSetter implements NFCDialogMsgSetter {

    @Override
    public void setText(String msg) {
	IOSNFCFactory.setDialogMsg(msg);
    }

    @Override
    public boolean isSupported() {
	return true;
    }

}
