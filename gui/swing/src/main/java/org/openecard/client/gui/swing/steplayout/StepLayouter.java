package org.openecard.client.gui.swing.steplayout;

import java.awt.Container;
import java.util.List;
import org.openecard.client.gui.swing.components.StepComponent;
import org.openecard.ws.gui.v1.InfoUnitType;


/**
 * Abstract base class to retrieve layouted components.<br/>
 * This class is also used to instantiate an implementation of a layouter
 * depending on the parameters (see static create function).
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public abstract class StepLayouter {

    /**
     * Create a layouter instance deping on the dialog type and/or the individual step name..
     * The newly created instance deals with the layouting of the components described in infoUnits.
     * @param infoUnits Abstract description of the components in the step.
     * @param dialogType URI describing the type of the dialog. Empty string when none is given.
     * @param stepName Name of the step. This can be used to have a different layouter for disclaimer and pin entry step for example.
     * @return Layouter which can return panel and components list.
     */
    public static StepLayouter create(List<InfoUnitType> infoUnits, String dialogType, String stepName) {
        StepLayouter layouter = null;

        // select method to create components
        // it is even possible to use different layouters for the individual steps (see stepName)
        if (dialogType.equals("somefancy dialog type like nPa-eID")) {
            // TODO: create and return
        }

        // default type if nothing happened so far
        if (layouter == null) {
            layouter = new DefaultStepLayouter(infoUnits);
        }

        return layouter;
    }


    /**
     * Get the list of components which have been placed onto the container.
     * @return
     */
    public abstract List<StepComponent> getComponents();

    /**
     * Get panel with layouted components, so it can be embedded in the frame.
     * @return Container panel
     */
    public abstract Container getPanel();

}
