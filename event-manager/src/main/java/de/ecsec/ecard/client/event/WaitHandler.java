package de.ecsec.ecard.client.event;

import de.ecsec.core.common.interfaces.Environment;
import de.ecsec.core.common.logging.LogManager;
import de.ecsec.core.ws.WSMarshaller;
import iso.std.iso_iec._24727.tech.schema.Wait;
import iso.std.iso_iec._24727.tech.schema.WaitResponse;
import java.util.Date;
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
        System.out.println("WaitHandler :: run (" + Thread.currentThread().getName() + ")");
        System.out.println("WaitHandler :: start @ " + new Date(System.currentTimeMillis()));
        try {
            Wait wRequest = new Wait();
            wRequest.setContextHandle(manager.getContext());
            WaitResponse wResponse = env.getIFD().wait(wRequest);
            
            try {
                WSMarshaller m = new WSMarshaller();
                System.out.println("WaitHandler :: " + m.doc2str(m.marshal(wResponse)));
            } catch (Exception ex) {
                // do nothing here...
            }
            
            manager.checkResult(wResponse.getResult());
            manager.process();
        } catch (EventException ex) {
            if (_logger.isLoggable(Level.WARNING)) {
                _logger.logp(Level.WARNING, this.getClass().getName(), "run()", ex.getResultMessage(), ex);
            }
            manager.process();
        }
        System.out.println("WaitHandler :: run (" + Thread.currentThread().getName() + ")");
        System.out.println("WaitHandler :: end @ " + new Date(System.currentTimeMillis()));
    }
}
