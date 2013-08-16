package ui;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import ui.MainFrame.UI;

import config.ConfFile;
import config.ObjectFile;

public class UIStatic implements PropertyChangeListener {
	
	// Container Class for UI components to be used by simulator
	
	public static MainFrame userFrame;
	public static ProgressMonitor progressMonitor;
	public static ConfFile configFile;
	public static ObjectFile worldLineFile;
	public static SwingWorker<Void, Void> simulation;
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName() ) {
            int progress = (Integer) evt.getNewValue();
            progressMonitor.setProgress(progress);
            String message =
                String.format("Completed %d%%.\n", progress);
            progressMonitor.setNote(message);
            if (progressMonitor.isCanceled() || simulation.isDone()) {
                Toolkit.getDefaultToolkit().beep();
                if (progressMonitor.isCanceled() || simulation.isCancelled()) {
                    simulation.cancel(true);
                    progressMonitor.close();
                    JOptionPane.showMessageDialog(userFrame, "World Line Generation Canceled");
                } else {
                    JOptionPane.showMessageDialog(userFrame, "World Line Generation Complete! Saved to: " + worldLineFile.getAbsolutePath());
                    progressMonitor.close();
                }
                userFrame.ResetFiles();
    			userFrame.ChangeUI(UI.kGenerate);
            }
        }
	}

}
