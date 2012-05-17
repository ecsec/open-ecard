package org.openecard.client.sal.protocol.eac.gui;

import org.openecard.client.common.I18n;
import org.openecard.client.gui.definition.Step;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PINStep {

    // GUI translation constants
    private static final String TITLE = "service_providers_statements_title";
    private static final String SUBJECT_NAME = "service_providers_name";
    private static final String SUBJECT_URL = "service_providers_internetaddress";
    private static final String TERMS_OF_USAGE = "service_providers_termsofusage";
    private static final String VALIDITY = "service_providers_validity";
    private static final String VALIDITY_FORMAT = "service_providers_validity_format";
    private static final String VALIDITY_FROM = "service_providers_validity_from";
    private static final String VALIDITY_TO = "service_providers_validity_to";
    private static final String ISSUER_NAME = "service_providers_validity";
    private static final String ISSUER_URL = "service_providers_validity";
    //
    private I18n lang = I18n.getTranslation("ifd");
    private Step step = new Step(lang.translationForKey(TITLE));
    private GUIContentMap content;

    public PINStep(GUIContentMap content) {
	this.content = content;
    }

    public Step create() {

	return step;
    }
}
