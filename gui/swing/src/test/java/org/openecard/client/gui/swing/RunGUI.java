package org.openecard.client.gui.swing;

import java.awt.Container;
import javax.swing.JDialog;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.BoxItem;
import org.openecard.client.gui.definition.Checkbox;
import org.openecard.client.gui.definition.Hyperlink;
import org.openecard.client.gui.definition.Passwordfield;
import org.openecard.client.gui.definition.Radiobox;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.definition.Text;
import org.openecard.client.gui.definition.Textfield;
import org.openecard.client.gui.definition.UserConsentDescription;
import org.openecard.client.gui.executor.ExecutionEngine;
import static org.junit.Assert.*;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class RunGUI {

    private UserConsentDescription uc;

    @Before
    public void setUp() throws Exception {
	uc = new UserConsentDescription("My Title");

	Step s1 = new Step("Step 1");
	uc.getSteps().add(s1);
	Text i1 = new Text();
	i1.setText("Some Text\nwith a newline.");
	s1.getInputInfoUnits().add(i1);
	Hyperlink h1 = new Hyperlink();
	h1.setHref("http://www.cardinfo.eu");
	s1.getInputInfoUnits().add(h1);
	Textfield t1 = new Textfield();
	s1.getInputInfoUnits().add(t1);
	t1.setName("text input1");
	t1.setText("Hello World input.");
	Passwordfield p1 = new Passwordfield();
	s1.getInputInfoUnits().add(p1);
	p1.setName("pass input1");
	p1.setText("PIN:");

	Step s2 = new Step("Step 2");
	uc.getSteps().add(s2);
	Checkbox i2 = new Checkbox();
	s2.getInputInfoUnits().add(i2);
	BoxItem bi1 = new BoxItem();
	i2.getBoxItems().add(bi1);
	bi1.setName("box1");
	bi1.setChecked(false);
	bi1.setDisabled(false);
	bi1.setText("Box 1");
	BoxItem bi2 = new BoxItem();
	i2.getBoxItems().add(bi2);
	bi2.setName("box2");
	bi2.setChecked(true);
	bi2.setDisabled(true);
	bi2.setText("Box 2");
	// add also to step 1
	s1.getInputInfoUnits().add(i2);

	Step s3 = new Step("Radio Step");
	uc.getSteps().add(s3);
	Radiobox i3 = new Radiobox();
	s3.getInputInfoUnits().add(i3);
	BoxItem bi3 = new BoxItem();
	i3.getBoxItems().add(bi3);
	bi3.setName("box1");
	bi3.setChecked(true);
	bi3.setDisabled(false);
	bi3.setText("Box 1");
	BoxItem bi4 = new BoxItem();
	i3.getBoxItems().add(bi4);
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
	UserConsentNavigator navigator = ucEngine.obtainNavigator(uc);
	ExecutionEngine exec = new ExecutionEngine(navigator);
	exec.process();
    }

}
