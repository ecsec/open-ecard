package org.openecard.client.gui;

import org.openecard.client.gui.definition.Step;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface UserConsentNavigator {

    public boolean hasNext();

    public StepResult current();
    public StepResult next();
    public StepResult previous();

    public StepResult replaceCurrent(Step step);
    public StepResult replaceNext(Step step);
    public StepResult replacePrevious(Step step);

    public void close();

}
