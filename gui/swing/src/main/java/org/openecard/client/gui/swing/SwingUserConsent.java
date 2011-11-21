package org.openecard.client.gui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.interfaces.UserConsent;
import org.openecard.ws.gui.v1.InfoUnitType;
import org.openecard.ws.gui.v1.ObtainUserConsentResponse;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SwingUserConsent implements UserConsent {

    private final DialogWrapper dialogWrapper;

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

        StepFrameContainer container = new StepFrameContainer(parameters.getStep(), stepContainer, sidebarPanel);
        rootPanel.validate();
        rootPanel.repaint();
        dialogWrapper.showDialog();
        List<InfoUnitType> outputInfo = container.getResult();
        dialogWrapper.hideDialog();

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

}
