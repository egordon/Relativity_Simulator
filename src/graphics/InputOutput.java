package graphics;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class InputOutput {
	
	public static int mouseX, mouseY, mousedX, mousedY;
	public static boolean leftMouse, rightMouse;
	public static boolean[] keyDown;
	public static boolean[] keyPress;
	
	public static void initIO() {
		keyPress = new boolean[256];
		keyDown = new boolean[256];
		clearIO();
	}
	
	public static void clearIO() {
		try {
			for(int i=0; i<keyPress.length; i++) keyPress[i] = false;
			for(int i=0; i<keyDown.length; i++) keyDown[i] = false;
			leftMouse = rightMouse = false;
		} catch(NullPointerException e) {
			System.err.println("Please call InitIO before accessing InputOutput functions!");
			e.printStackTrace();
			System.exit(0);
		}
		
	}
	
	public static void pollIO() {
		// Clear KeyPresses
		for(int i=0; i<keyPress.length; i++) keyPress[i] = false;
		
		// Update Keyboard Table
		while(Keyboard.next()) {
			if(Keyboard.getEventKeyState()) {
				keyDown[Keyboard.getEventKey()] = true;
			} else {
				keyDown[Keyboard.getEventKey()] = false;
				keyPress[Keyboard.getEventKey()] = true;
			}
		}
		
		// Update Mouse Tables
		mouseX = Mouse.getX();
		mouseY = Mouse.getY();
		mousedX = Mouse.getDX();
		mousedY = Mouse.getDY();
		
		while(Mouse.next()) {
			if(Mouse.getEventButtonState()) {
				if(Mouse.getEventButton() == 0) leftMouse = true;
				else if(Mouse.getEventButton() == 1) rightMouse = true;
			} else {
				if(Mouse.getEventButton() == 0) leftMouse = true;
				else if(Mouse.getEventButton() == 1) rightMouse = true;
			}
		}
		
	}

}
