package general.services;

import general.GeneralGLWorldLine;
import general.util.Manifold;

import java.util.ArrayList;
import java.util.HashMap;

public class GeneralGenerator {
	
	HashMap<String, GeneralGLWorldLine> dots;
	Manifold mani;
	
	private static final int MONTE_CARLO_PASSES = 3;
	private static final int MONTE_CARLO_SIZE = 100;
	
	public GeneralGenerator(int manifoldSize, HashMap<String, GeneralGLWorldLine> newDots) {
		dots = newDots;
		mani = new Manifold(manifoldSize);
		for(GeneralGLWorldLine wl : dots.values()) {
			mani.addPoint(wl);
		}
	}
	
	public void update() {
		mani.recalculateAllEnergies();
		mani.calculateMetric(MONTE_CARLO_PASSES, MONTE_CARLO_SIZE);
		mani.recalculateAllEinsteins();
		mani.recalculateAllChristoffels();
		mani.recalculateAllPoints();
	}
	
	public void updateRawLines() {
		ArrayList<GeneralGLWorldLine> points = mani.getAllPoints();
		dots.clear();
		for(GeneralGLWorldLine wl : points) {
			dots.put(wl.getName(), wl);
		}
	}
	
	public HashMap<String, GeneralGLWorldLine> getRawLines() {
		return dots;
	}

}
