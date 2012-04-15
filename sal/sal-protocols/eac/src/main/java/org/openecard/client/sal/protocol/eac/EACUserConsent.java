/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.sal.protocol.eac;

import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.I18n;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.crypto.common.asn1.cvc.CHAT;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificate;
import org.openecard.client.crypto.common.asn1.cvc.CertificateDescription;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.*;
import org.openecard.client.gui.executor.ExecutionEngine;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class EACUserConsent {

    private static final Logger logger = LogManager.getLogger(EACUserConsent.class.getName());
    private I18n lang = I18n.getTranslation("sal");
    private UserConsent gui;

    public EACUserConsent(UserConsent gui) {
	this.gui = gui;
    }

    public CHAT show(CardVerifiableCertificate certificate, CertificateDescription description, CHAT requiredCHAT, CHAT optionalCHAT) {
//        // <editor-fold defaultstate="collapsed" desc="log trace">
//        if (logger.isLoggable(Level.FINER)) {
//            logger.entering(
//                    this.getClass().getName(),
//                    "showUserConsentAndRetrieveCHAT(CertificateDescription description, CHAT requiredCHAT, CHAT optionalCHAT, CardVerifiableCertificate cvc)",
//                    new Object[]{description, requiredCHAT, optionalCHAT, certificate});
//        } // </editor-fold>
//        UserConsentDescription ucd = new UserConsentDescription("test");
//
//        Step s1 = new Step(lang.translationForKey("service_providers_statements_title"));
//        ucd.getSteps().add(s1);
//        Text i1 = new Text();
//        i1.setText(lang.translationForKey("service_providers_name") + "\n" + description.getSubjectName() + "\n");
//        s1.getInputInfoUnits().add(i1);
//
//        Text i2 = new Text();
//        i2.setText(lang.translationForKey("service_providers_internetaddress"));
//        s1.getInputInfoUnits().add(i2);
//
//        try {
//            Hyperlink h1 = new Hyperlink();
//            h1.setHref(description.getSubjectURL());
//            s1.getInputInfoUnits().add(h1);
//        } catch (MalformedURLException e1) {
//            // Fallback to Textoutput
//            Text h1 = new Text();
//            h1.setText(description.getSubjectURL() + "\n");
//            s1.getInputInfoUnits().add(h1);
//        }
//
//        Text i3 = new Text();
//        i3.setText("\n" + lang.translationForKey("service_providers_statements") + "\n" + description.getTermsOfUsage() + "\n");
//        s1.getInputInfoUnits().add(i3);
//
//        Text i6 = new Text();
//        i6.setText(lang.translationForKey("certificate_effective_date") + "\n" + lang.translationForKey("from") + " "
//                + certificate.getEffectiveDate() + "\n" + lang.translationForKey("to") + " " + certificate.getExpirationDate() + "\n");
//        s1.getInputInfoUnits().add(i6);
//
//        Text i7 = new Text();
//        i7.setText(lang.translationForKey("issuer") + "\n" + description.getIssuerName() + "\n");
//        s1.getInputInfoUnits().add(i7);
//
//        Text i8 = new Text();
//        i8.setText(lang.translationForKey("issuers_url"));
//        s1.getInputInfoUnits().add(i8);
//
//        try {
//            Hyperlink h2 = new Hyperlink();
//            h2.setHref(description.getIssuerURL());
//            s1.getInputInfoUnits().add(h2);
//        } catch (MalformedURLException e1) {
//            // Fallback to Textoutput
//            Text h2 = new Text();
//            h2.setText(description.getIssuerURL() + "\n");
//            s1.getInputInfoUnits().add(h2);
//        }
//
//        Step s2 = new Step(lang.translationForKey("requested_data"));
//        ucd.getSteps().add(s2);
//
//        Text i9 = new Text();
//        i9.setText(lang.translationForKey("purpose") + "\n");
//        s2.getInputInfoUnits().add(i9);
//
//        Checkbox i4 = new Checkbox();
//        s2.getInputInfoUnits().add(i4);
//
//        for (int i = 0; i < requiredCHAT.getReadAccess().length; i++) {
//            if (requiredCHAT.getReadAccess()[i]) {
//                BoxItem bi1 = new BoxItem();
//                i4.getBoxItems().add(bi1);
//                bi1.setName(CHAT.DataGroups.values()[i].name());
//                bi1.setChecked(true);
//                bi1.setDisabled(false);
//                bi1.setText(CHAT.DataGroups.values()[i].toString());
//            }
//        }
//
//        for (int i = 0; i < requiredCHAT.getSpecialFunctions().length; i++) {
//
//            if (requiredCHAT.getSpecialFunctions()[i]) {
//                BoxItem bi1 = new BoxItem();
//                i4.getBoxItems().add(bi1);
//                bi1.setName(CHAT.SpecialFunction.values()[i].name());
//                bi1.setChecked(true);
//                bi1.setDisabled(false);
//                bi1.setText(CHAT.SpecialFunction.values()[i].toString());
//            }
//        }
//
//        UserConsentNavigator ucn = this.gui.obtainNavigator(ucd);
//        ExecutionEngine exec = new ExecutionEngine(ucn);
//        exec.process();
//
//        CHAT chat = new CHAT();
//        boolean[] specialFunctions = new boolean[8];
//        boolean[] readAccess = new boolean[21];
//
//        for (OutputInfoUnit out : exec.getResults().get(lang.translationForKey("requested_data")).getResults()) {
//            if (out.type().equals(InfoUnitElementType.Checkbox)) {
//                Checkbox c = (Checkbox) out;
//                for (BoxItem bi : c.getBoxItems()) {
//                    if (bi.isChecked()) {
//                        if (bi.getName().startsWith("DG")) {
//                            readAccess[CHAT.DataGroups.valueOf(bi.getName()).ordinal()] = true;
//                        } else {
//                            specialFunctions[CHAT.SpecialFunction.valueOf(bi.getName()).ordinal()] = true;
//                        }
//                    }
//                    // else do nothing, false is default
//                }
//            }
//            // else ignore
//        }
//
//        chat.setSpecialFunctions(specialFunctions);
//        chat.setReadAccess(readAccess);
//
//        // <editor-fold defaultstate="collapsed" desc="log trace">
//        if (logger.isLoggable(Level.FINER)) {
//            logger.exiting(
//                    this.getClass().getName(),
//                    "showUserConsentAndRetrieveCHAT(CertificateDescription description, CHAT requiredCHAT, CHAT optionalCHAT, CardVerifiableCertificate cvc)",
//                    chat);
//        } // </editor-fold>
	return null;
    }

}
