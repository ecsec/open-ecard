/*
 * Copyright 2012 Tobias Wich ecsec GmbH
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
    public Container getContentPane() {
        return dialog.getContentPane();
    }

    @Override
    public void show() {
        dialog.setVisible(true);
    }

    @Override
    public void hide() {
        dialog.setVisible(false);
    }

}
