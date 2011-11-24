package org.openecard.client.gui.swing;

import java.awt.Container;
import javax.swing.JDialog;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openecard.ws.gui.v1.BoxItem;
import org.openecard.ws.gui.v1.CheckBox;
import org.openecard.ws.gui.v1.HyperLink;
import org.openecard.ws.gui.v1.InputInfoUnitType;
import org.openecard.ws.gui.v1.ObtainUserConsent;
import org.openecard.ws.gui.v1.PasswordInput;
import org.openecard.ws.gui.v1.Radio;
import org.openecard.ws.gui.v1.Step;
import org.openecard.ws.gui.v1.TextInput;
import static org.junit.Assert.*;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class RunGUI {

    private ObtainUserConsent uc;

    @Before
    public void setUp() throws Exception {
        uc = new ObtainUserConsent();
        uc.setTitle("My Title");

        Step s1 = new Step();
        uc.getStep().add(s1);
        s1.setName("Step 1");
        InputInfoUnitType i1 = new InputInfoUnitType();
        s1.getInfoUnit().add(i1);
        i1.setText("Some Text\nwith a newline.");
        InputInfoUnitType h1 = new InputInfoUnitType();
        HyperLink hl1 = new HyperLink();
        hl1.setHref("http://www.cardinfo.eu");
        h1.setHyperLink(hl1);
        s1.getInfoUnit().add(h1);
        InputInfoUnitType t1 = new InputInfoUnitType();
        s1.getInfoUnit().add(t1);
        TextInput ti1 = new TextInput();
        t1.setTextInput(ti1);
        ti1.setName("text input1");
        ti1.setText("Hello World input.");
        InputInfoUnitType p1 = new InputInfoUnitType();
        s1.getInfoUnit().add(p1);
        PasswordInput pi1 = new PasswordInput();
        p1.setPasswordInput(pi1);
        pi1.setName("pass input1");
        pi1.setText("PIN:");

        Step s2 = new Step();
        uc.getStep().add(s2);
        s2.setName("Step 2");
        InputInfoUnitType i2 = new InputInfoUnitType();
        s2.getInfoUnit().add(i2);
        CheckBox cb1 = new CheckBox();
        i2.setCheckBox(cb1);
        BoxItem bi1 = new BoxItem();
        cb1.getBoxItem().add(bi1);
        bi1.setName("box1");
        bi1.setChecked(false);
        bi1.setDisabled(false);
        bi1.setText("Box 1");
        BoxItem bi2 = new BoxItem();
        cb1.getBoxItem().add(bi2);
        bi2.setName("box2");
        bi2.setChecked(true);
        bi2.setDisabled(true);
        bi2.setText("Box 2");
        // add also to step 1
        s1.getInfoUnit().add(i2);

        Step s3 = new Step();
        uc.getStep().add(s3);
        s3.setName("Radio Step");
        InputInfoUnitType i3 = new InputInfoUnitType();
        s3.getInfoUnit().add(i3);
        Radio r1 = new Radio();
        i3.setRadio(r1);
        BoxItem bi3 = new BoxItem();
        r1.getBoxItem().add(bi3);
        bi3.setName("box1");
        bi3.setChecked(true);
        bi3.setDisabled(false);
        bi3.setText("Box 1");
        BoxItem bi4 = new BoxItem();
        r1.getBoxItem().add(bi4);
        bi4.setName("box2");
        bi4.setChecked(true);
        bi4.setDisabled(false);
        bi4.setText("Box 2");
    }

    private class TestDialog implements DialogWrapper {

        private JDialog dialog;

        public TestDialog() {
            this.dialog = new JDialog();
            this.dialog.setSize(800, 600);
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

    /**
     * Uncomment the <code>@Ignore</code> line to run a demo gui so you can debug it.
     */
    @Ignore
    @Test
    public void runUC() {
        TestDialog dialog = new TestDialog();
        SwingUserConsent ucEngine = new SwingUserConsent(dialog);
        ucEngine.obtainUserConsent(uc);
    }

}
