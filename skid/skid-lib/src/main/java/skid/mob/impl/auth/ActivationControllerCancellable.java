package skid.mob.impl.auth;

import org.openecard.mobile.activation.ActivationController;
import skid.mob.lib.Cancellable;

/**
 *
 * @author Florian Otto
 */
public class ActivationControllerCancellable implements Cancellable {

    private final ActivationController ac;

    public ActivationControllerCancellable(ActivationController ac) {
	this.ac = ac;
    }

    @Override
    public void cancel() {
	ac.cancelOngoingAuthentication();
    }

}
