package org.openecard.client.ifd.scio;

import org.openecard.client.common.ifd.VirtualPinResult;
import org.openecard.client.common.ifd.VirtualPinResultType;
import org.openecard.client.common.ifd.VirtualTerminal;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SwingTerminal implements VirtualTerminal {

    private final ConcurrentHashMap<Integer, AbstractBox> threads = new ConcurrentHashMap<Integer, AbstractBox>();


    private abstract class AbstractBox extends javax.swing.JDialog {

	private volatile boolean cancelled = false;
	private volatile boolean boolTimeout = false;
	private FutureTask future;
	protected JPanel infoPanel;
	protected JPanel buttonPanel;


	public AbstractBox(final BigInteger timeout) {
	    // add layout manager
	    FlowLayout boarder = new FlowLayout();
	    boarder.setHgap(10);
	    boarder.setVgap(10);
	    this.setLayout(boarder);
	    JPanel contentPanel = new JPanel();
	    BoxLayout bl = new BoxLayout(contentPanel, BoxLayout.Y_AXIS);
	    contentPanel.setLayout(bl);
	    this.add(contentPanel);

	    infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
	    buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
	    contentPanel.add(infoPanel);
	    contentPanel.add(buttonPanel);

	    this.setTitle("Information");
	    this.setUndecorated(true);
	    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	    this.setLocationByPlatform(true); // center on screen normally

	    future = new FutureTask(new Callable() {

		@Override
		public Object call() throws Exception {
		    Thread.sleep(timeout.longValue());
		    boolTimeout = true;
		    return null;
		}

	    });
	}

	public void display() {
	    pack();
	    setVisible(true);
	    future.run();

	    try {
		// wait for timeout to occur or the future is cancelled
		future.get();
	    } catch (CancellationException ex) {
	    } catch (InterruptedException ex) {
	    } catch (ExecutionException ex) {
	    }

	    setVisible(false);
	}

	public void cancel() {
	    this.cancelled = true;
	    future.cancel(true);
	}

	public void addCancelButton() {
	    JButton cancelButton = new JButton("Cancel");
	    buttonPanel.add(cancelButton);
	    cancelButton.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    // close dialog
		    setVisible(false);
		    cancelled = true;
		    future.cancel(true);
		}

	    });
	}

	public void addOKButton() {
	    JButton okButton = new JButton("OK");
	    buttonPanel.add(okButton);
	    okButton.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    // close dialog
		    setVisible(false);
		    future.cancel(true);
		}

	    });
	}

	public synchronized boolean isCancelled() {
	    return cancelled;
	}

	public synchronized boolean isTimeout() {
	    return boolTimeout;
	}

    };

    private class MessageBox extends AbstractBox {

	public MessageBox(String msg, BigInteger timeout) {
	    super(timeout);

	    Icon i = UIManager.getDefaults().getIcon("OptionPane.informationIcon");
	    infoPanel.add(new JLabel(msg, i, SwingConstants.LEADING));

	    addOKButton();
	}

    };

    private class InputBox extends MessageBox {

	private JTextField pin;

	public InputBox(String msg, BigInteger timeout) {
	    super(msg, timeout);

	    pin = new JTextField("", 10);
	    infoPanel.add(pin);

	    addCancelButton();
	}

	public String getPin() {
	    return pin.getText();
	}

    }


    @Override
    public int displayMessage(String msg) {
	return displayMessage(msg, BigInteger.valueOf(60000));
    }

    @Override
    public int displayMessage(String msg, BigInteger timeout) {
	MessageBox b = new MessageBox(msg, timeout);
	threads.put(b.hashCode(), b);
	return b.hashCode();
    }

    @Override
    public void waitForMsg(int procNum) {
	AbstractBox b = threads.get(procNum);
	if (b != null) {
	    b.display();
	}
	threads.remove(procNum);
    }

    @Override
    public void cancel(int procNum) {
	AbstractBox b = threads.get(procNum);
	if (b != null) {
	    b.cancel();
	}
    }

    @Override
    public void beep() {
	Toolkit.getDefaultToolkit().beep();
    }

    @Override
    public void blink() {
	System.out.println("Warning: Blink function not implemented in VirtualTerminal.");
    }

    @Override
    public VirtualPinResult requestPIN(String msg, BigInteger firstTimeout, BigInteger otherTimeout) {
	InputBox i = new InputBox(msg, firstTimeout);
	int procNum = i.hashCode();
	threads.put(procNum, i);
	waitForMsg(procNum);

	if (i.isCancelled()) {
	    return new VirtualPinResult(VirtualPinResultType.CANCELLED);
	} else if (i.isTimeout()) {
	    return new VirtualPinResult(VirtualPinResultType.TIMEOUT);
	} else {
	    return new VirtualPinResult(i.getPin());
	}
    }

}
