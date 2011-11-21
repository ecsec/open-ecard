package org.openecard.client.gui.swing;

import java.awt.Container;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface DialogWrapper {

    public void setTitle(String title);
    public String getTitle();

    public Container getRootPanel();

    public void showDialog();
    public void hideDialog();

}
