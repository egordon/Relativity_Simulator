package ui;

import graphics.DisplayInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import newton.NewtonSim;

import simulator.AbstractGLWorldLine;
import special.SpecialSim;
import ui.MainFrame.UI;
import config.ParseException;

public class ButtonAction extends AbstractAction implements ActionListener {

	private static final long serialVersionUID = 2343984662316211028L;
	
	String actionString;
	String actionArgs;
	MainFrame sourceFrame;
	JFileChooser fc;
	
	public ButtonAction(String action, String args, MainFrame newFrame) {
		super();
		actionString = action;
		actionArgs = args;
		sourceFrame = newFrame;
		fc = new JFileChooser("./examples");
	}

	@SuppressWarnings({ "unchecked", "resource" })
	@Override
	public void actionPerformed(ActionEvent ae) {
		
		/// File Actions ///
		if(actionString == "browse") {
			if(actionArgs == "input") {
				// Actions for Inputing a Configuration File
				fc.setFileFilter(new ExtensionFileFilter("Generation Config", new String[] { "genconf", "GENCONF" }));
				if(fc.showOpenDialog(sourceFrame) == JFileChooser.APPROVE_OPTION) {
					sourceFrame.SetFile(actionArgs, fc.getSelectedFile());
				}
			} else if(actionArgs == "inputWL") {
				// Actions for Inputing a World Line File
				fc.setFileFilter(new ExtensionFileFilter("Serialized World Line", new String[] { "wl", "WL" }));
				if(fc.showOpenDialog(sourceFrame) == JFileChooser.APPROVE_OPTION) {
					sourceFrame.SetFile(actionArgs, fc.getSelectedFile());
				}
			} else {
				// Actions for saving a World Line File
				fc.setFileFilter(new ExtensionFileFilter("Serialized World Line", new String[] { "wl", "WL" }));
				if(fc.showSaveDialog(sourceFrame) == JFileChooser.APPROVE_OPTION) {
					if(!fc.getSelectedFile().getAbsolutePath().endsWith(".wl"))
						sourceFrame.SetFile(actionArgs, new File(fc.getSelectedFile().getAbsolutePath()+ ".wl"));
					else
						sourceFrame.SetFile(actionArgs, fc.getSelectedFile());
				}
			}
		} else if(actionString == "submit") {
			if(actionArgs == "generate") {
				
				/// Actions for Generating a World Line
				if(sourceFrame.GetConfig() == null || sourceFrame.GetObjectFile() == null) return;
				
				// Disable Generation UI
				sourceFrame.ChangeUI(UI.kGenProgress);
				UIStatic.configFile = sourceFrame.GetConfig();
				UIStatic.userFrame = sourceFrame;
				UIStatic.worldLineFile = sourceFrame.GetObjectFile();
				
				// Create a new SwingWorker to track progress
				SwingWorker<Void, Void> simulation = null;
				
				String algorithm = null;
				Integer frames = 0;
				
				// Validate Config File
				try {
					UIStatic.configFile.parseText();
					UIStatic.configFile.selectKey("global");
					algorithm = UIStatic.configFile.parseString("algorithm");
					frames = UIStatic.configFile.parseInt("seconds")*60;
				} catch (ParseException e) {
					JOptionPane.showMessageDialog(sourceFrame, e.getInformation(), "Error Parsing Config!", JOptionPane.ERROR_MESSAGE);
					sourceFrame.ResetFiles();
					sourceFrame.ChangeUI(UI.kGenerate);
					return;
				}
				
				if(algorithm.equals("special")) {
					simulation = new SpecialSim();
					UIStatic.progressMonitor = new ProgressMonitor(sourceFrame, "Generating Special Relativity WorldLine...", 
							"Generating " + frames + " Frames", 0, 100);
				} else if(algorithm.equals("newton")){
					simulation = new NewtonSim(frames);
					UIStatic.progressMonitor = new ProgressMonitor(sourceFrame, "Generating Newtonian WorldLine...", 
							"Generating " + frames + " Frames", 0, 100);
				} else {
					JOptionPane.showMessageDialog(sourceFrame, "Algorithm " + algorithm + " does not exist.", "Error Parsing Config!", JOptionPane.ERROR_MESSAGE);
					sourceFrame.ResetFiles();
					sourceFrame.ChangeUI(UI.kGenerate);
					return;
				}
				
				// Execute WordLine Generation
				UIStatic.progressMonitor.setMillisToDecideToPopup(10);
				UIStatic.progressMonitor.setMillisToPopup(10);
				UIStatic.simulation = simulation;
				simulation.addPropertyChangeListener(new UIStatic());
				simulation.execute();
			} else if(actionArgs == "playback") {
				HashMap<String, AbstractGLWorldLine> objMap;
				try {
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(sourceFrame.GetWLFile()));
					objMap = (HashMap<String, AbstractGLWorldLine>)ois.readObject();
				} catch (Exception e) {
					e.printStackTrace();
					sourceFrame.ResetFiles();
					return;
				}
				DisplayInterface di = new DisplayInterface(objMap);
				di.start();
			}
		}

	}

}
