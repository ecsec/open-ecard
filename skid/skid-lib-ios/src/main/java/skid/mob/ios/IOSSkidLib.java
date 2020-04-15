/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skid.mob.ios;

import org.openecard.ios.activation.NFCConfig;
import org.openecard.mobile.activation.PinManagementControllerFactory;
import org.openecard.robovm.annotations.FrameworkInterface;
import skid.mob.lib.SamlClient;
import skid.mob.lib.SkidLib;

/**
 *
 * @author Florian Otto
 */
@FrameworkInterface
public interface IOSSkidLib extends SkidLib {

    public PinManagementControllerFactory pinManagementFactory(NFCConfig nfcConfig);

    public SamlClient createSamlClient(NFCConfig nfcConfig);
}
