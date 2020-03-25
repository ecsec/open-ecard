package org.openecard.ios.activation;

import org.openecard.mobile.activation.common.NFCDialogMsgSetter;
import org.openecard.scio.CachingTerminalFactoryBuilder;
import org.openecard.scio.IOSNFCFactory;

/**
 *
 * @author Florian Otto
 */
public class IOSNFCDialogMsgSetter implements NFCDialogMsgSetter {

    private final CachingTerminalFactoryBuilder<IOSNFCFactory> builder;

    IOSNFCDialogMsgSetter(CachingTerminalFactoryBuilder<IOSNFCFactory> builder) {
	this.builder = builder;
    }

    @Override
    public void setText(String msg) {
	IOSNFCFactory currentFactory = builder.getPreviousInstance();
	if (currentFactory != null) {
	    currentFactory.setDialogMsg(msg);
	}
    }

    @Override
    public boolean isSupported() {
	return true;
    }

}
