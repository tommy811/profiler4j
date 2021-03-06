/*
 * Copyright 2006 Antonio S. R. Gomes
 * Copyright 2009 Murat Knecht
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package net.sf.profiler4j.console.util.task;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LongTaskExecutorDialog extends JDialog {

    private static final Log log = LogFactory.getLog(LongTaskExecutorDialog.class);

    private static final long serialVersionUID = 1L;
    private JPanel jContentPane = null;
    private JPanel jPanel = null;
    private JProgressBar progressBar = null;
    private JLabel label = null;
    private JScrollPane statusScrollPane = null;
    private JTextArea statusTextArea = null;
    /**
     * @param owner
     */
    public LongTaskExecutorDialog(Frame owner) {
        super(owner);
        initialize();
        setLocationRelativeTo(owner);
    }
    /**
     * This method initializes this
     */
    private void initialize() {
        this.setSize(486, 107);
        this.setResizable(false);
        this.setModal(true);
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.setTitle("Remote Command Monitor");
        this.setContentPane(getJContentPane());
    }
    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJPanel(), BorderLayout.CENTER);
        }
        return jContentPane;
    }
    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            label = new JLabel();
            label.setBounds(new java.awt.Rectangle(15, 16, 451, 15));
            label.setText("Long running task...");
            jPanel = new JPanel();
            jPanel.setLayout(null);
            jPanel.add(getProgressBar(), null);
            jPanel.add(label, null);
            jPanel.add(getStatusScrollPane(), null);
        }
        return jPanel;
    }
    /**
     * This method initializes progressBar
     * 
     * @return javax.swing.JProgressBar
     */
    public JProgressBar getProgressBar() {
        if (progressBar == null) {
            progressBar = new JProgressBar();
            progressBar.setBounds(new java.awt.Rectangle(15, 45, 451, 18));
            progressBar.setIndeterminate(true);
        }
        return progressBar;
    }

    /**
     * This method initializes statusScrollPane
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getStatusScrollPane() {
        if (statusScrollPane == null) {
            statusScrollPane = new JScrollPane();
            statusScrollPane.setBounds(new java.awt.Rectangle(15, 75, 451, 256));
            statusScrollPane.setViewportView(getStatusTextArea());
        }
        return statusScrollPane;
    }
    /**
     * This method initializes statusTextArea
     * 
     * @return javax.swing.JTextArea
     */
    private JTextArea getStatusTextArea() {
        if (statusTextArea == null) {
            statusTextArea = new JTextArea();
            statusTextArea.setBackground(java.awt.SystemColor.info);
            statusTextArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN,
                    11));
            statusTextArea.setEditable(false);
        }
        return statusTextArea;
    }

    public void setMessage(String s) {
        label.setText(s);
    }

    private Throwable error;

    public void runTask(final LongTask task) {        
        label.setText("Executing long-running task...");
        task.setDialog(this);
        Thread t = new Thread("PROFILER4J_TASK") {
            public void run() {
                log.debug("TASK STARTED");
                try {
                    task.executeInBackground();
                } catch (final Exception e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            log.error("Caught task error", e);
                            error = e;
                        };
                    });
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            log.debug("TASK COMPLETED");
                            if (error != null) {
                                setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                                progressBar.setIndeterminate(false);
                                setSize(486, 379);
                                setVisible(true);
                                toFront();
                                ((JComponent) statusTextArea.getParent()).revalidate();
                                StringWriter sw = new StringWriter();
                                PrintWriter pw = new PrintWriter(sw);
                                error.printStackTrace(pw);
                                statusTextArea.setText(sw.toString());
                                statusTextArea.setCaretPosition(0);
                                task.setError(error);
                            } else {
                                setVisible(false);
                                dispose();
                            }
                        }
                    });
                }
            };
        };
        t.setName("LongTaskRunner");
        t.setPriority(Thread.MIN_PRIORITY);
        t.setDaemon(false);
        t.start();

        setLocation(getLocation().x, getLocation().y - 120);
        setVisible(true);
    }

} // @jve:decl-index=0:visual-constraint="10,10"
