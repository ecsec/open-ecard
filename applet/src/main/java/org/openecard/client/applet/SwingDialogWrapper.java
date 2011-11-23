package org.openecard.client.applet;

import java.awt.Container;
import javax.swing.JDialog;
import org.openecard.client.gui.swing.DialogWrapper;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SwingDialogWrapper implements DialogWrapper {

    private JDialog dialog;

    public SwingDialogWrapper() {
        this.dialog = new JDialog();
        this.dialog.setSize(640, 480);
        this.dialog.setVisible(false);
        this.dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
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
        this.dialog.setVisible(true);
    }

    @Override
    public void hideDialog() {
        this.dialog.setVisible(false);
    }

}
