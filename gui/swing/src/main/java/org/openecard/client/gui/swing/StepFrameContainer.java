package org.openecard.client.gui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;
import javax.swing.JButton;
import org.openecard.ws.gui.v1.InfoUnitType;
import org.openecard.ws.gui.v1.Step;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class StepFrameContainer {

    private final Container stepContainer;
    private final Container sidebarPanel;

    private final Sidebar sidebar;
    private final int numSteps;
    private final ArrayList<StepFrame> stepFrames;

    private int curStep = 0;
    private boolean cancelled = false;
    private boolean finished = false;
    private Exchanger syncPoint = new Exchanger();


    public StepFrameContainer(List<Step> steps, Container stepContainer, Container sidebarPanel) {
        this.stepContainer = stepContainer;
        this.sidebarPanel = sidebarPanel;

        this.sidebar = new Sidebar(this.sidebarPanel, stepNames(steps));
        this.numSteps = steps.size();
        this.stepFrames = createStepFrames(steps);
        selectIdx(0);
    }

    private ArrayList<StepFrame> createStepFrames(List<Step> steps) {
        ArrayList<StepFrame> frames = new ArrayList<StepFrame>(steps.size());
        for (int idx=0; idx < steps.size(); idx++) {
            Step s = steps.get(idx);
            StepFrame sf = new StepFrame(s);
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
                back.addActionListener(new BackEvent(this, sf));
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

    private String[] stepNames(List<Step> steps) {
        ArrayList<String> stepNames = new ArrayList<String>(steps.size());
        for (Step s : steps) {
            stepNames.add(s.getName());
        }
        return stepNames.toArray(new String[steps.size()]);
    }

    private synchronized void selectIdx(int idx) {
        // sidebar
        this.sidebar.selectIdx(idx);
        // content replacement
        this.stepContainer.removeAll();
        BorderLayout layout = new BorderLayout();
        this.stepContainer.setLayout(layout);
        StepFrame nextStep = stepFrames.get(idx);
        Container nextPanel = nextStep.getPanel();
        this.stepContainer.add(nextPanel, BorderLayout.CENTER);
        this.curStep = idx;
        this.stepContainer.validate();
        this.stepContainer.repaint();
    }

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
    private static class BackEvent implements ActionListener {
        private final StepFrameContainer outer;
        private final StepFrame frame;
        public BackEvent(StepFrameContainer outer, StepFrame frame) {
            this.outer = outer;
            this.frame = frame;
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

    public List<InfoUnitType> getResult() {
        try {
            syncPoint.exchange(null);
        } catch (InterruptedException ex) {
        }
        if (isCancelled()) {
            return null;
        }
        // create return value
        ArrayList<InfoUnitType> result = new ArrayList<InfoUnitType>();
        for (StepFrame next : this.stepFrames) {
            result.addAll(next.getResultContent());
        }
        return result;
    }

    public synchronized boolean isCancelled() {
        return this.cancelled;
    }
    
}
