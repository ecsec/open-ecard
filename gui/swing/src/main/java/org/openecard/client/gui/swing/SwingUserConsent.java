package org.openecard.client.gui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.interfaces.UserConsent;
import org.openecard.ws.gui.v1.InfoUnitType;
import org.openecard.ws.gui.v1.ObtainUserConsentResponse;
import org.openecard.ws.gui.v1.Step;


/**
 * Swing implementation of the UserConsent interface.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SwingUserConsent implements UserConsent {

    private final DialogWrapper dialogWrapper;

    /**
     * Instantiate SwingUserConsent. The implementation encapsulates a DialogWrapper which is needed to supply a root pane for all draw operations.
     * @param dialogWrapper
     */
    public SwingUserConsent(DialogWrapper dialogWrapper) {
        this.dialogWrapper = dialogWrapper;
    }


    @Override
    public ObtainUserConsentResponse obtainUserConsent(org.openecard.ws.gui.v1.ObtainUserConsent parameters) {
        dialogWrapper.setTitle(parameters.getTitle());
        // prepare root panel for display
        Container rootPanel = dialogWrapper.getRootPanel();
        rootPanel.removeAll();
        BorderLayout layout = new BorderLayout(3, 3);
        rootPanel.setLayout(layout);
        JPanel stepContainer = new JPanel();
        JScrollPane scrollpane = new JScrollPane(stepContainer, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JPanel sidebarPanel = new JPanel();
        stepContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        rootPanel.add(scrollpane, BorderLayout.CENTER);
        rootPanel.add(sidebarPanel, BorderLayout.EAST);

        // create the step container and display UC
        String dialogType = parameters.getDialogueType();
        dialogType = dialogType==null ? "" : dialogType;
        List<Step> steps = parameters.getStep();
        Sidebar sidebar = new Sidebar(sidebarPanel, stepNames(steps));
        StepFrameContainer container = new StepFrameContainer(dialogType, steps, stepContainer, sidebar);
        rootPanel.validate();
        rootPanel.repaint();
        dialogWrapper.showDialog();
        List<InfoUnitType> outputInfo = container.getResult();
        dialogWrapper.hideDialog();

        // create result element depending on outcome
        Result result = new Result();
        ObtainUserConsentResponse res = new ObtainUserConsentResponse();
        if (container.isCancelled()) {
            result.setResultMajor(ECardConstants.Major.ERROR);
            // TODO: discuss what these urls are
            //result.setResultMinor(ECardConstants.Minor.GUI.USER_CANCEL);
        } else {
            result.setResultMajor(ECardConstants.Major.OK);
            res.getOutput().addAll(outputInfo);
        }

        res.setProfile(ECardConstants.Profile.ECARD_1_1);
        res.setResult(result);
        return res;
    }


    /**
     * Create a list of step names. This is a helper function to factor out code from the actual call.
     * @param steps Step list as contained in ObtainUserConsent.
     * @return Array containing the names of all steps.
     */
    private static String[] stepNames(List<Step> steps) {
        ArrayList<String> stepNames = new ArrayList<String>(steps.size());
        for (Step s : steps) {
            stepNames.add(s.getName());
        }
        return stepNames.toArray(new String[steps.size()]);
    }

}
