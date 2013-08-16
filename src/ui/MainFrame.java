package ui;
import javax.swing.*;

//import com.apple.eawt.Application;

import config.ConfFile;
import config.ObjectFile;


import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;

public class MainFrame extends JFrame implements ActionListener{

	private static final long serialVersionUID = 6223249036014348137L;
	
	JRadioButton[] uiselection;
	
	JPanel generateUI;
	JPanel playbackUI;
	JPanel emptyUI;
	
	JTextField inputFile;
	JTextField outputFile;
	JTextField worldFile;
	
	ConfFile inFile;
	ObjectFile outFile;
	ObjectFile wlFile;
	
	UI current;
	Dimension startSize;
	Dimension generateSize;
	Dimension playbackSize;

	enum UI {
		kNone,
		kGenerate,
		kPlayback,
		kGenProgress
	}
	
	public void ResetFiles() {
		inFile = null;
		outFile = null;
		wlFile = null;
		inputFile.setText("Input Config File Here");
		outputFile.setText("Output WorldLine File Here");
		worldFile.setText("Input World Line File Here");
	}
	
	public void SetFile(String type, File file) {
		if(type == "input") {
			try {
				inFile = new ConfFile(file.getAbsolutePath());	
			} catch(FileNotFoundException e) {
				e.printStackTrace();
			}
			inputFile.setText(inFile.getAbsolutePath());
		}
		else if (type == "inputWL") {
			wlFile = new ObjectFile(file.getAbsolutePath());	
			worldFile.setText(wlFile.getAbsolutePath());
		}
		else if (type == "output") {
			outFile = new ObjectFile(file.getAbsolutePath());
			outputFile.setText(outFile.getAbsolutePath());
		}
	}
	
	public ConfFile GetConfig() {
		return inFile;
	}
	
	public ObjectFile GetObjectFile() {
		return outFile;
	}
	
	public ObjectFile GetWLFile() {
		return wlFile;
	}
	
	public MainFrame(String title) {
		super(title);
		
		// Set Dimensions
		startSize = new Dimension(800, 50);
		generateSize = new Dimension(800, 120);
		
		
		// Setup JFrame
		SetSystemLookAndFeel();
		this.setMinimumSize(new Dimension(800, 50));
		setResizable(false);
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		current = UI.kNone;
		
		// File IO
		inFile = null;
		outFile = null;
		
		// Setup North Border Layout
		JPanel northLayout = new JPanel();
		uiselection = new JRadioButton[2];
		uiselection[0] = new JRadioButton("Generate WorldLine");
		uiselection[1] = new JRadioButton("Simulation Playback");
		uiselection[0].setActionCommand("generate");
		uiselection[1].setActionCommand("playback");
		
		JLabel buttonLabel = new JLabel("Select program:");
		buttonLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
		
		northLayout.setLayout(new GridLayout(1, 2));
		ButtonGroup uiselect = new ButtonGroup();
		
		
		northLayout.add(buttonLabel);
		for(int i=0; i<2; i++) {
			uiselect.add(uiselection[i]);
			northLayout.add(uiselection[i]);
			uiselection[i].addActionListener(this);
		}
		
		
		
		
		/////////////// Create 3 Center Layouts //////////////
		
		// Generation Layout
		generateUI = new JPanel();
		
		// Setup Text Fields
		inputFile  = new JTextField("Input Config File Here               ");
		outputFile = new JTextField("Output WorldLine File Here           ");
		
		Dimension textSize = new Dimension(200, 20);
		
		inputFile.setMaximumSize(textSize);
		inputFile.setPreferredSize(textSize);
		inputFile.setMinimumSize(textSize);
		
		outputFile.setMaximumSize(textSize);
		outputFile.setPreferredSize(textSize);
		outputFile.setMinimumSize(textSize);
		
		inputFile.setEditable(false);
		outputFile.setEditable(false);
		
		// Setup Buttons
		Button infButton = new Button("Browse Config File");
		Button outButton = new Button("Save WorldLine File");
		infButton.addActionListener(new ButtonAction("browse", "input", this));
		outButton.addActionListener(new ButtonAction("browse", "output", this));
		Button submitButton = new Button("Generate WorldLine");
		submitButton.addActionListener(new ButtonAction("submit", "generate", this));
		submitButton.setName("button");
		infButton.setName("button");
		outButton.setName("button");
		
		// Add everything to JPanel with spacers
		generateUI.add(inputFile);
		generateUI.add(infButton);
		generateUI.add(new JPanel());
		generateUI.add(outputFile);
		generateUI.add(outButton);
		generateUI.add(new JPanel());
		generateUI.add(submitButton);
		
		// None Layout
		emptyUI = new JPanel();
		
		// Playback Layout
		playbackUI = new JPanel();
		
		// Setup Text Fields
		worldFile  = new JTextField("Input World Line File Here                   ");
		
		worldFile.setMaximumSize(textSize);
		worldFile.setPreferredSize(textSize);
		worldFile.setMinimumSize(textSize);
		
		worldFile.setEditable(false);
		
		// Setup Buttons
		Button wlButton = new Button("Browse WL File");
		wlButton.addActionListener(new ButtonAction("browse", "inputWL", this));
		
		submitButton = new Button("Playback Simulation");
		submitButton.addActionListener(new ButtonAction("submit", "playback", this));
		submitButton.setName("button");
		wlButton.setName("button");
		
		// Add everything to UI
		playbackUI.add(worldFile);
		playbackUI.add(wlButton);
		playbackUI.add(new JPanel());
		playbackUI.add(submitButton);
		
		// Add Layouts to BorderLayout
		this.add(northLayout, BorderLayout.NORTH);
		this.add(emptyUI, BorderLayout.CENTER);
		pack();
		setVisible(true);
	}
	
	public void ChangeUI(UI newLayout) {
		switch(current) {
		case kNone:
			remove(emptyUI);
			break;
		case kPlayback:
			remove(playbackUI);
			for(Component cmp : playbackUI.getComponents()) {
				cmp.setVisible(false);
				cmp.repaint();
			}
			break;
		case kGenerate:
			if(newLayout != UI.kGenProgress) {
				remove(generateUI);
				for(Component cmp : generateUI.getComponents()) {
					cmp.setVisible(false);
					cmp.repaint();
				}
			}
			break;
		case kGenProgress:
			for(Component cmp : generateUI.getComponents()) {
				if(cmp.getName() == "button") cmp.setEnabled(true);
			}
			remove(generateUI);
			break;
		}
		current = newLayout;
		switch(current) {
		case kNone:
			add(emptyUI, BorderLayout.CENTER);
			this.setPreferredSize(startSize);
			break;
		case kPlayback:
			add(playbackUI, BorderLayout.CENTER);
			for(Component cmp : playbackUI.getComponents()) {
				cmp.setVisible(true);
				cmp.repaint();
			}
			break;
		case kGenerate:
			add(generateUI, BorderLayout.CENTER);
			for(Component cmp : generateUI.getComponents()) {
				cmp.setVisible(true);
				cmp.repaint();
			}
			this.setPreferredSize(generateSize);
			break;
		case kGenProgress:
			for(Component cmp : generateUI.getComponents()) {
				if(cmp.getName() == "button") cmp.setEnabled(false);
			}
			break;
		}
		pack();
	}
	
	public void SetSystemLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		if(OSValidator.isMac()) {
			//Application application = Application.getApplication();
			Image image = Toolkit.getDefaultToolkit().getImage("res/icon.png");
			//application.setDockIconImage(image);
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name","Physics Simulator");
		}
		
		MainFrame mainFrame = new MainFrame("Ethan's Relativity Simulator");
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String ac = arg0.getActionCommand();
		if(ac == "generate") {
			this.ChangeUI(UI.kGenerate);
		} else {
			this.ChangeUI(UI.kPlayback);
		}
		
	}

}
