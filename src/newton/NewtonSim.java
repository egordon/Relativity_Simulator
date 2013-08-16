package newton;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import newton.services.NewtonGenerator;

import org.lwjgl.util.vector.Vector3f;

import ui.UIStatic;
import config.ParseException;

public class NewtonSim extends SwingWorker<Void, Void> {
	
	Integer numFrames;
	
	public NewtonSim(Integer newNumFrames) {
		numFrames = newNumFrames;
	}

	@Override
	protected Void doInBackground() throws Exception {
		// Initialize Local Variables
		NewtonGenerator ng = null;
		setProgress(0);
		        
		// Parse Config File
		try {
			// Get Global Settings
			UIStatic.configFile.selectKey("global");
			ng = new NewtonGenerator(UIStatic.configFile.parseFloat("G"), numFrames);
			setProgress(20);
			for(String key : UIStatic.configFile.getKeys()) {
				if(key.equals("global")) continue;
				UIStatic.configFile.selectKey(key);
				Float[] newVelocity = UIStatic.configFile.parseFloatList("velocity");
				Float[] newPosition = UIStatic.configFile.parseFloatList("position");
				Float[] newColor;
				if(UIStatic.configFile.getAttributes().contains("color")) {
					newColor = UIStatic.configFile.parseFloatList("color");
				} else {
					newColor = new Float[] {0.0f, 0.0f, 0.0f};
				}
				
				NewtonGLWorldLine wl = new NewtonGLWorldLine(
						UIStatic.configFile.parseFloat("mass"),
						new Vector3f(newVelocity[0].floatValue(), newVelocity[1].floatValue(), newVelocity[2].floatValue()),
						new Vector3f(newPosition[0].floatValue(), newPosition[1].floatValue(), newPosition[2].floatValue()),
						UIStatic.configFile.parseFloat("size"),
						new Vector3f(newColor[0], newColor[1], newColor[2]),
						key
						);
				ng.addWL(key, wl);
			}
			setProgress(1);
			
		} catch(ParseException e) {
        	JOptionPane.showMessageDialog(UIStatic.userFrame, e.getInformation(), "Error Parsing Config for Newtonian Gravity!", JOptionPane.ERROR_MESSAGE);
        	this.cancel(true);
        }
		
		for(int i=1; i<=numFrames; i++) {
			ng.update(i);
			setProgress((int)((i*100)/numFrames * .9)); // Increments to 90 percent
			Thread.yield();
		}
		
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(UIStatic.worldLineFile));
		oos.writeObject(ng.getMap());
		oos.close();
		setProgress(100);
		
		return null;
	}
	
	

}
