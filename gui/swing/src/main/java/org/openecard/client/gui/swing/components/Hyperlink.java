package org.openecard.client.gui.swing.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JLabel;
import org.openecard.client.gui.swing.StepComponent;
import org.openecard.ws.gui.v1.HyperLink;
import org.openecard.ws.gui.v1.InfoUnitType;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Hyperlink implements StepComponent {

    private final String href;
    private final String text;
    private final String underlineText;
    private final JLabel label;

    public Hyperlink(HyperLink link) {
        this.href = link.getHref();
        this.text = link.getText() != null ? link.getText() : this.href;
        this.underlineText = "<html><u>" + this.text + "</u></html>";
        this.label = new JLabel(text);
        this.label.setDoubleBuffered(true);
        this.label.setForeground(Color.blue);
        this.label.setToolTipText(link.getHref());
        this.label.addMouseListener(new BrowserLauncher());
    }

    @Override
    public Component getComponent() {
        return label;
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public boolean isValueType() {
        return false;
    }

    @Override
    public InfoUnitType getValue() {
        return null;
    }


    private class BrowserLauncher implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            try {
                boolean browserOpened = false;
                URI uri = new URI(href);
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(uri);
                        browserOpened = true;
                    } catch (IOException ex) {
                        // opening browser failed
                    }
                }
                // there is a bug which prevents this from working under linux without gnome. big up oracle you guys rock
                // in a standard linux desktop (freedesktop.org or lsb conforming) there is the xdg-open untility which can open the default browser
                if (! browserOpened) {
                    ProcessBuilder pb = new ProcessBuilder("xdg-open", uri.toString());
                    try {
                        pb.start();
                    } catch (IOException ex) {
                        // failed to execute command
                    }
                }
            } catch (URISyntaxException ex) {
                // silently fail, its just no use
            }
        }
        @Override
        public void mousePressed(MouseEvent e) {
        }
        @Override
        public void mouseReleased(MouseEvent e) {
        }
        @Override
        public void mouseEntered(MouseEvent e) {
            label.setText(underlineText);
        }
        @Override
        public void mouseExited(MouseEvent e) {
            label.setText(text);
        }
    }

}
