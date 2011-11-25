package org.openecard.client.applet;

import java.awt.Container;
import java.awt.Frame;
import javax.swing.JDialog;
import org.openecard.client.gui.swing.DialogWrapper;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SwingDialogWrapper implements DialogWrapper {

    private JDialog dialog;

    public SwingDialogWrapper(Frame parent) { // parent should propably be a RootPaneContainer (the applet instance itself)
        // ATTENTION: a modal dialog blocks the rest of the application, be really sure that this is the intended behaviour
        // modality is disabled for that reason, enable it with the boolean switch if needed
        // but maybe the glass pane from the applet can be used to shield the website from user input
        dialog = new JDialog(parent, false);
        dialog.setSize(640, 480);
        dialog.setLocationRelativeTo(null); // center on screen
        dialog.setVisible(false);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }


    @Override
    public void setTitle(String title) {
        dialog.setTitle(title);
    }

    @Override
    public Container getRootPanel() {
        return dialog.getContentPane();
    }

    @Override
    public void showDialog() {
        dialog.setVisible(true);
    }

    @Override
    public void hideDialog() {
        dialog.setVisible(false);
    }

}
