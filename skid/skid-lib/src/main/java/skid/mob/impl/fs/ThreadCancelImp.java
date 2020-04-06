package skid.mob.impl.fs;

import skid.mob.lib.Cancellable;

/**
 *
 * @author Florian Otto
 */
public class ThreadCancelImp implements Cancellable {

    private final Thread t;

    public ThreadCancelImp(Thread t) {
	this.t = t;
    }

    @Override
    public void cancel() {
	t.interrupt();
    }

}
