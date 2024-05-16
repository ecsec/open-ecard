package org.openecard.mobile.activation.common;

import org.openecard.robovm.annotations.FrameworkInterface;

/**
 *
 * @author Florian Otto
 */
@FrameworkInterface
public interface NFCDialogMsgSetter {

    void setText(String msg);

    boolean isSupported();

}
