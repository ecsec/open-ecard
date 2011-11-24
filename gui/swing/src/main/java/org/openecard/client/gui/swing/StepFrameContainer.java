package org.openecard.client.gui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;
import javax.swing.JButton;
import org.openecard.ws.gui.v1.OutputInfoUnitType;
import org.openecard.ws.gui.v1.Step;


/**
 * Container for StepFrames which can switch which frame is active.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class StepFrameContainer {

    private final Container stepContainer;

    private final String dialogType;
    private final Sidebar sidebar;
    private final ArrayList<StepFrame> stepFrames;
    private final int numSteps;

    private int curStep = 0;
    private boolean cancelled = false;
    private boolean finished = false;
    private Exchanger syncPoint = new Exchanger();


    public StepFrameContainer(String dialogType, List<Step> steps, Container stepContainer, Sidebar sidebar) {
        this.stepContainer = stepContainer;
        this.dialogType = dialogType;
        this.sidebar = sidebar;
        this.numSteps = steps.size(); // separate field, otherwise selectIdx fails on first invocation

        this.stepFrames = createStepFrames(steps);
        selectIdx(0);
    }

    /**
     * Create StepFrame instances and configure buttons.
     * @param steps List of steps for which to create the frames.
     * @return List of frames.
     */
    private ArrayList<StepFrame> createStepFrames(List<Step> steps) {
        ArrayList<StepFrame> frames = new ArrayList<StepFrame>(steps.size());
        for (int idx=0; idx < steps.size(); idx++) {
            Step s = steps.get(idx);
            StepFrame sf = new StepFrame(s, dialogType, idx);
            frames.add(sf);
            // configure buttons
            JButton back = sf.getBackButton();
            JButton forw = sf.getForwardButton();
            JButton canc = sf.getCancelButton();
            // back
            back.setText("Back");
            if (isFirstIdx(idx)) {
                back.setEnabled(false);
            } else {
                back.addActionListener(new BackEvent(this));
            }
            // forw
            if (isLastIdx(idx)) {
                forw.setText("Finish");
                forw.addActionListener(new FinishEvent(this, sf));
            } else {
                forw.setText("Next");
                forw.addActionListener(new ForwardEvent(this, sf));
            }
            // cancel
            canc.removeAll();
            canc.setText("Cancel");
            canc.addActionListener(new CancelEvent(this));
        }
        return frames;
    }

    private synchronized void selectIdx(int idx) {
        // sidebar
        sidebar.selectIdx(idx);
        // content replacement
        stepContainer.removeAll();
        BorderLayout layout = new BorderLayout();
        stepContainer.setLayout(layout);
        StepFrame nextStep = stepFrames.get(idx);
        Container nextPanel = nextStep.getPanel();
        stepContainer.add(nextPanel, BorderLayout.CENTER);
        curStep = idx;
        stepContainer.validate();
        stepContainer.repaint();
    }

    /**
     * Event class for cancel button clicks.
     */
    private static class CancelEvent implements ActionListener {
        private final StepFrameContainer outer;
        public CancelEvent(StepFrameContainer outer) {
            this.outer = outer;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            synchronized (outer) {
                if (!outer.finished && !outer.cancelled) {
                    outer.cancelled = true;
                    try {
                        outer.syncPoint.exchange(null);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
    }
    /**
     * Event class for forward button clicks.
     */
    private static class ForwardEvent implements ActionListener {
        private final StepFrameContainer outer;
        private final StepFrame frame;
        public ForwardEvent(StepFrameContainer outer, StepFrame frame) {
            this.outer = outer;
            this.frame = frame;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (frame.validate()) {
                outer.selectIdx(outer.curStep+1);
            }
        }
    }
    /**
     * Event class for finish button clicks.
     */
    private static class FinishEvent implements ActionListener {
        private final StepFrameContainer outer;
        private final StepFrame frame;
        public FinishEvent(StepFrameContainer outer, StepFrame frame) {
            this.outer = outer;
            this.frame = frame;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            synchronized (outer) {
                if (!outer.finished && frame.validate()) {
                    outer.finished = true;
                    try {
                        outer.syncPoint.exchange(null);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
    }
    /**
     * Event class for back button clicks.
     */
    private static class BackEvent implements ActionListener {
        private final StepFrameContainer outer;
        public BackEvent(StepFrameContainer outer) {
            this.outer = outer;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            outer.selectIdx(outer.curStep-1);
        }
    }

    
    private boolean isFirstIdx(int idx) {
        return idx == 0;
    }
    private boolean isLastIdx(int idx) {
        return idx == (this.numSteps-1);
    }

    /**
     * Get result elements for all components on all frames which have a value.<br/>
     * The call blocks until the user either presses the cancel or finish button.
     * @return List of component results (may be empty depending on components) or null if dialog has been cancelled.
     */
    public List<OutputInfoUnitType> getResult() {
        try {
            syncPoint.exchange(null);
        } catch (InterruptedException ex) {
        }
        if (isCancelled()) {
            return null;
        }
        // create return value
        ArrayList<OutputInfoUnitType> result = new ArrayList<OutputInfoUnitType>();
        for (StepFrame next : this.stepFrames) {
            result.addAll(next.getResultContent());
        }
        return result;
    }

    /**
     * Indicated cancelled status of the container.
     * @return True when cancelled, false otherwise.
     */
    public synchronized boolean isCancelled() {
        return this.cancelled;
    }
    
}
