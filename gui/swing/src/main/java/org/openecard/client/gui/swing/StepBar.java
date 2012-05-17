package org.openecard.client.gui.swing;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openecard.client.gui.definition.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @editor Florian Feldmann <florian.feldmann@rub.de>
 */
public final class StepBar extends JPanel implements PropertyChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(StepBar.class);
    private List<Step> steps;

    public StepBar(List<Step> steps) {
	this.steps = steps;
	updateStepBar(steps);
    }

    public void updateStepBar(List<Step> steps) {
	removeAll();
	initializeLayout();
	initializeComponents();
    }

    private void initializeLayout() {
	setLayout(new GridBagLayout());
    }

    private void initializeComponents() {
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.anchor = GridBagConstraints.PAGE_START;
	gbc.ipady = 10;
	gbc.gridwidth = GridBagConstraints.REMAINDER;

	for (String names : getStepNames(steps)) {
	    JLabel l = new JLabel(names);
	    l.setForeground(Color.GRAY);
	    add(l, gbc);
	}

	gbc.weighty = 1.0;
	add(new JLabel(), gbc);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
	logger.info("StepBar event: {} | {} | {} | {}",
		new Object[]{evt.getSource().getClass(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue()});
	if (evt.getPropertyName() != null) {
	    if (evt.getPropertyName().equals(SwingNavigator.PROPERTY_CURRENT_STEP)) {
		Object newIndex = evt.getNewValue();
		Object oldIndex = evt.getOldValue();

		if (newIndex instanceof Integer) {
		    int newIndexValue = ((Integer) newIndex).intValue();
		    if (newIndexValue >= 0 && newIndexValue < getComponentCount()) {
			getComponent(newIndexValue).setForeground(Color.GRAY);
		    }
		}
		if (oldIndex instanceof Integer) {
		    int oldIndexValue = ((Integer) oldIndex).intValue();
		    if (oldIndexValue >= 0 && oldIndexValue < getComponentCount()) {
			// Highlight current element
			getComponent(oldIndexValue).setForeground(Color.BLACK);
		    }
		}
	    }
	}
    }

    private static String[] getStepNames(List<Step> steps) {
	ArrayList<String> stepNames = new ArrayList<String>(steps.size());
	for (Step s : steps) {
	    stepNames.add(s.getTitle());
	}
	return stepNames.toArray(new String[steps.size()]);
    }
}
