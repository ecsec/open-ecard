package org.openecard.client.ifd.protocol.pace.gui;

import java.util.Map;
import org.openecard.client.common.I18n;
import org.openecard.client.gui.definition.OutputInfoUnit;
import org.openecard.client.gui.definition.PasswordField;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.definition.Text;
import org.openecard.client.gui.executor.ExecutionResults;
import org.openecard.client.ifd.protocol.pace.common.PasswordID;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PINStep {

    // GUI translation constants
    private static final String TITLE = "step_pace_title";
    private static final String DESCRIPTION = "step_pace_description";
    //
    private I18n lang = I18n.getTranslation("ifd");
    private Step step = new Step(lang.translationForKey(TITLE));
    private GUIContentMap content;
    private String passwordType;

    public PINStep(GUIContentMap content) {
	this.content = content;

	passwordType = PasswordID.parse((Byte) (content.get(GUIContentMap.ELEMENT.PIN_ID))).getString();
    }

    public Step create() {
	Text description = new Text();
	description.setText(lang.translationForKey(DESCRIPTION));
	step.getInputInfoUnits().add(description);

	PasswordField pinInputField = new PasswordField();
	pinInputField.setID(passwordType);
	pinInputField.setDescription(lang.translationForKey(passwordType));
	step.getInputInfoUnits().add(pinInputField);

	return step;
    }

    public void processResult(Map<String, ExecutionResults> results) {
	ExecutionResults executionResults = results.get(step.getID());

	if (executionResults == null) {
	    return;
	}

	for (OutputInfoUnit output : executionResults.getResults()) {
	    if (output instanceof PasswordField) {
		PasswordField p = (PasswordField) output;
		if (p.getID().equals(passwordType)) {
		    content.add(GUIContentMap.ELEMENT.PIN, p.getValue());
		}
	    }
	}
    }
}
