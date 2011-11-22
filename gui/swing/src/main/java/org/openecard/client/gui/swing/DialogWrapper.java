package org.openecard.client.gui.swing;

import java.awt.Container;


/**
 * Interface to give the swing gui the ability to set a title, show and hide the dialog and get a drawing pane.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface DialogWrapper {

    /**
     * Set title of the user consent dialog.
     * @param title Title to set in the dialog.
     */
    public void setTitle(String title);

    /**
     * A root panel is needed so the user consent can be embedded in the actual application.
     * @return Container the GUI can draw its content on.
     */
    public Container getRootPanel();

    /**
     * This function is executed after the root panel has been set up with the contents of the user conset.
     */
    public void showDialog();
    /**
     * This function is executed after the user consent is finished or cancelled.
     */
    public void hideDialog();

}
