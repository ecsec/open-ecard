package org.openecard.client.ifd.protocol.pace.gui;

import java.util.Map;
import org.openecard.client.common.I18n;
import org.openecard.client.gui.definition.OutputInfoUnit;
import org.openecard.client.gui.definition.Passwordfield;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.definition.Text;
import org.openecard.client.gui.executor.ExecutionResults;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PINStep {

    // GUI translation constants
    private static final String TITLE = "step_pace_title";
    private static final String DESCRIPTION = "step_pace_description";
    private static final String PIN = "pin";
    //
    private I18n lang = I18n.getTranslation("ifd");
    private Step step = new Step(lang.translationForKey(TITLE));
    private GUIContentMap content;

    public PINStep(GUIContentMap content) {
	this.content = content;
    }

    public Step create() {
	Text description = new Text();
	description.setText(lang.translationForKey(DESCRIPTION));
	step.getInputInfoUnits().add(description);

	//TODO Der step sollte so den pin type berücksichtigen.
	Passwordfield pinInputField = new Passwordfield();
	pinInputField.setID(PIN);
	pinInputField.setDescription(lang.translationForKey(PIN));
	step.getInputInfoUnits().add(pinInputField);

	return step;
    }

    public void processResult(Map<String, ExecutionResults> results) {
	ExecutionResults executionResults = results.get(step.getID());

	if (executionResults == null) {
	    return;
	}

	for (OutputInfoUnit output : executionResults.getResults()) {
	    if (output instanceof Passwordfield) {
		Passwordfield p = (Passwordfield) output;
		if (p.getID().equals(PIN)) {
		    content.add(GUIContentMap.ELEMENT.PIN, p.getValue());
		}
	    }
	}
    }
}
