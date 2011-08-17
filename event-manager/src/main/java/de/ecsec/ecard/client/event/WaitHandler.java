package de.ecsec.ecard.client.event;

import de.ecsec.core.common.interfaces.Environment;
import de.ecsec.core.common.logging.LogManager;
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.Wait;
import iso.std.iso_iec._24727.tech.schema.WaitResponse;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class WaitHandler implements Runnable {

    private static final Logger _logger = LogManager.getLogger(WaitHandler.class.getPackage().getName());
    private EventManager manager;
    private Environment env;

    public WaitHandler(EventManager manager, Environment env) {
        this.manager = manager;
        this.env = env;
    }

    @Override
    public void run() {
        try {
            Wait wRequest = new Wait();
            ChannelHandleType cHandle = new ChannelHandleType();
            cHandle.setSessionIdentifier(manager.getSessionId()); // session needed for cancel
            wRequest.setCallback(cHandle);
            wRequest.setContextHandle(manager.getContext());
            WaitResponse wResponse = env.getIFD().wait(wRequest);
            manager.checkResult(wResponse.getResult());
            manager.process();
        } catch (EventException ex) {
            if (_logger.isLoggable(Level.WARNING)) {
                _logger.logp(Level.WARNING, this.getClass().getName(), "run()", ex.getResultMessage(), ex);
            }
            manager.process();
        }
    }
}
