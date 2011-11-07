package org.openecard.client.recognition;

import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.logging.LogManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class Conf {
    
    private static final Logger _logger = LogManager.getLogger(Conf.class.getName());
    
    private String action;
    private String serviceName;
    private String serviceAddr;
    
    public Conf() {
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "Conf()");
        }
        // set default values
        this.action = ECardConstants.CIF.GET_OTHER;
        this.serviceName = "GetRecognitionTree";
        this.serviceAddr = "http://repository.cardinfo.eu/services/GetRecognitionTree";
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "Conf()");
        }
    }
    
    public String getAction() {
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "getAction()");
            _logger.exiting(this.getClass().getName(), "getAction()", action);
        }
        return action;
    }
    
    public String getServiceName() {
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "getServiceName()");
            _logger.exiting(this.getClass().getName(), "getServiceName()", serviceName);
        }
        return serviceName;
    }
    
    public String getServiceAddr() {
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "getServiceAddr()");
            _logger.exiting(this.getClass().getName(), "getServiceAddr()", serviceAddr);
        }
        return serviceAddr;
    }

    public synchronized void readConfiguration() {
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "readConfiguration()");
        }
        readConfiguration(null);
        if (_logger.isLoggable(Level.FINER)) {
            _logger.exiting(this.getClass().getName(), "readConfiguration()");
        }
    }
    
    public synchronized void readConfiguration(InputStream is) {
        if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(this.getClass().getName(), "readConfiguration(InputStream is)");
        }
        Properties prop = new Properties();
        try {
            if (is != null) {
                prop.load(is);
            } else {
                prop.load(this.getClass().getResourceAsStream("/cardrecognition.properties"));
            }
            action = prop.getProperty("action", action);
            serviceName = prop.getProperty("serviceName", serviceName);
            serviceAddr = prop.getProperty("serviceAddr", serviceAddr);
            if (_logger.isLoggable(Level.FINER)) {
                _logger.exiting(this.getClass().getName(), "readConfiguration(InputStream is)");
            }
        } catch (IOException ex) {
            if (_logger.isLoggable(Level.WARNING)) {
                _logger.logp(Level.WARNING, this.getClass().getName(), "readConfiguration(InputStream is)", ex.getMessage(), ex);
                _logger.logp(Level.WARNING, this.getClass().getName(), "readConfiguration(InputStream is)", "Unable to read configuration. Using default values.", new Object[]{action, serviceName, serviceAddr});
            }
        }
    }

    public boolean isEnabled() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
