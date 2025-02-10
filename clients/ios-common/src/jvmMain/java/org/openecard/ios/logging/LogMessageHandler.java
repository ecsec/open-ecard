package org.openecard.ios.logging;

import org.openecard.robovm.annotations.FrameworkInterface;

@FrameworkInterface
public interface LogMessageHandler {
	void log(String msg);
}
