package newton.services;

import java.util.ArrayList;
import java.util.HashMap;

import simulator.AbstractGLWorldLine;

import newton.NewtonGLWorldLine;


public class NewtonGenerator {
	
	ArrayList<NewtonGLWorldLine> objectMap;
	public static float G_CONST;
	Integer numFrames;
	
	public NewtonGenerator(float newG, Integer newNumFrames) {
		objectMap = new ArrayList<NewtonGLWorldLine>();
		G_CONST = newG;
		numFrames = newNumFrames;
	}
	
	public void addWL(String name, NewtonGLWorldLine newLine) {
		objectMap.add(newLine);
	}
	
	public void update(Integer frame) {
		for(int i=0; i<objectMap.size(); i++) {
			for(int ii=i+1; ii<objectMap.size(); ii++) {
				objectMap.get(i).ApplyGravity(objectMap.get(ii));
			}
			objectMap.get(i).setPosition(frame);
		}
	}
	
	public HashMap<String, AbstractGLWorldLine> getMap() {
		HashMap<String, AbstractGLWorldLine> retMap = new HashMap<String, AbstractGLWorldLine>();
		for(AbstractGLWorldLine wl : objectMap) {
			retMap.put(wl.getName(), wl);
		}
		return retMap;
	}

}
