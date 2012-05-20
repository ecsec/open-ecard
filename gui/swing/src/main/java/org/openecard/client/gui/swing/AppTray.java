package org.openecard.client.gui.swing;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.openecard.client.common.I18n;
import org.openecard.client.common.logging.LoggingConstants;
import org.openecard.client.gui.swing.common.GUIDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class AppTray {

    private static final Logger logger = LoggerFactory.getLogger(Navigation.class);
    private final I18n lang = I18n.getTranslation("gui");
    private final SystemTray tray = SystemTray.getSystemTray();

    public AppTray() {
    }

    void initialize() {
	try {
	    final PopupMenu popup = new PopupMenu();
	    final TrayIcon trayIcon = new TrayIcon(GUIDefaults.getImage("Frame.icon", 45, 45).getImage());

	    trayIcon.setImageAutoSize(true);
	    trayIcon.setPopupMenu(popup);
	    trayIcon.setToolTip(lang.translationForKey("tray.title"));

	    tray.add(trayIcon);

	    // Create a popup menu components
	    MenuItem aboutItem = new MenuItem(lang.translationForKey("tray.about"));
	    MenuItem configItem = new MenuItem(lang.translationForKey("tray.config"));
	    MenuItem helpItem = new MenuItem(lang.translationForKey("tray.help"));
	    MenuItem exitItem = new MenuItem(lang.translationForKey("tray.exit"));

	    // testing
	    Menu displayMenu = new Menu("Display");
	    MenuItem errorItem = new MenuItem("Error");
	    MenuItem warningItem = new MenuItem("Warning");
	    MenuItem infoItem = new MenuItem("Info");
	    MenuItem noneItem = new MenuItem("None");


	    //Add components to popup menu
	    popup.add(displayMenu);
	    displayMenu.add(errorItem);
	    displayMenu.add(warningItem);
	    displayMenu.add(infoItem);
	    displayMenu.add(noneItem);
	    popup.addSeparator();


	    popup.add(configItem);
	    popup.add(helpItem);
	    popup.add(aboutItem);
	    popup.addSeparator();
	    popup.add(exitItem);

	    trayIcon.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    JOptionPane.showMessageDialog(null,
			    "Run that ...");
		}
	    });

	    aboutItem.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    JOptionPane.showMessageDialog(null,
			    "Implement me");
		}
	    });

	    ActionListener listener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    MenuItem item = (MenuItem) e.getSource();
		    if ("Error".equals(item.getLabel())) {
			trayIcon.displayMessage(lang.translationForKey("tray.title"), "Error message", TrayIcon.MessageType.ERROR);

		    } else if ("Warning".equals(item.getLabel())) {
			trayIcon.displayMessage(lang.translationForKey("tray.title"), "Warning message", TrayIcon.MessageType.WARNING);

		    } else if ("Info".equals(item.getLabel())) {
			trayIcon.displayMessage(lang.translationForKey("tray.title"), "Info message", TrayIcon.MessageType.INFO);

		    } else if ("None".equals(item.getLabel())) {
			trayIcon.displayMessage(lang.translationForKey("tray.title"), "Some message", TrayIcon.MessageType.NONE);
		    }
		}
	    };

	    errorItem.addActionListener(listener);
	    warningItem.addActionListener(listener);
	    infoItem.addActionListener(listener);
	    noneItem.addActionListener(listener);

	    exitItem.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    tray.remove(trayIcon);
		    // to SAL runterfahren ....
		    System.exit(0);
		}
	    });
	} catch (Exception e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error(LoggingConstants.THROWING, "Exception", e);
	    // </editor-fold>
	}
    }
}
