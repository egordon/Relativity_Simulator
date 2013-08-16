package special;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import config.ParseException;

import ui.UIStatic;
import simulator.SpecialGLWorldLine;
import special.util.FourVector;
import special.util.Util;

public class SpecialSim extends SwingWorker<Void, Void>{
	@Override
	protected Void doInBackground() {
		
		// Initialize Local Variables
		HashMap<String, SpecialGLWorldLine> lineMap;
        setProgress(0);
        int progress = 0;
        
        // Parse Config File
        try {
        	
        	// Get Global Settings
        	UIStatic.configFile.selectKey("global");
        	Util.SetC(UIStatic.configFile.parseDouble("cspeed"));
        	progress = 20;
        	setProgress(progress);
        	
        	Float newVelocity[];
        	Float camVelocity[];
        	camVelocity = UIStatic.configFile.parseFloatList("cameraVelocity");
        	
        	ArrayList<FourVector> startPoints = new ArrayList<FourVector>();
        	
        	if(UIStatic.configFile.getKeys().size() < 1) throw new ParseException(0, "File has no World Lines!");
        	
        	for(String key : UIStatic.configFile.getKeys()) {
        		
        		// One World Line per Key
        		startPoints.clear();
        		UIStatic.configFile.selectKey(key);
        		newVelocity = UIStatic.configFile.parseFloatList("velocity");
        		for(int i=1; i<UIStatic.configFile.getAttributes().size(); i++) {
        			Double point[] = UIStatic.configFile.parseNumList("t"+(i/3 + 1)+"p"+(i%3));
        			if(point.length != 3) throw new ParseException(-1, "Point attribute '" + "t"+(i/3 + 1)+"p"+(i%3) + "' must have 3 values!");
        			else {
        				startPoints.add(new FourVector(Float.parseFloat(point[0].toString()), 
        						Float.parseFloat(point[1].toString()), 
        						Float.parseFloat(point[2].toString())));
        			}
        		}
        		
        		// Add Start Points to WorldLine
        		
        		
        		// Set Progress
        		if(progress < 100) {
        			progress += 5;
        			setProgress(progress);
        		}
        	}
        	
        	
        	
        } catch(ParseException e) {
        	JOptionPane.showMessageDialog(UIStatic.userFrame, e.getInformation(), "Error Parsing Config for Special Relativity!", JOptionPane.ERROR_MESSAGE);
        	this.cancel(true);
        }
		return null;
    }
	

}
