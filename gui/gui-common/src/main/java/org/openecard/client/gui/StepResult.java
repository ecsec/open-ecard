package org.openecard.client.gui;

import java.util.List;
import org.openecard.client.gui.definition.OutputInfoUnit;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface StepResult {

    public String stepName();

    public ResultStatus status();
    public boolean isOK();
    public boolean isBack();
    public boolean isCancelled();

    public List<OutputInfoUnit> results();

}
