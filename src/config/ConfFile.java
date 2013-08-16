package config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

public class ConfFile extends File {
	
	private static final long serialVersionUID = 8597689180808269012L;
	
	Scanner parseScan;
	
	HashMap<String, HashMap<String, String>> configMap;
	
	String currentKey;
	

	public ConfFile(String path) throws FileNotFoundException{
		super(path);
		parseScan = new Scanner(new FileInputStream(this));
		configMap = new HashMap<String, HashMap<String, String>>();
		currentKey = null;
	}
	
	public ConfFile(File file) throws FileNotFoundException{
		super(file.getAbsolutePath());
		parseScan = new Scanner(new FileInputStream(this));
		configMap = new HashMap<String, HashMap<String, String>>();
		currentKey = null;
	}
	
	public void parseText() throws ParseException {
		// Clear config map
		configMap.clear();
		String currentLine = "";
		int lineCount = 1;
		parseScan.reset();
		
		// Scan through entire file
		while(parseScan.hasNextLine()) {
			currentLine = parseScan.nextLine().trim();
			
			// Empty Line Handling
			if(currentLine.length() == 0) {
				lineCount++;
				continue;
			}
			
			// Comment Handling
			if(currentLine.contains("#")) {
				if(currentLine.startsWith("#")) {
					lineCount++;
					continue;
				}
				else currentLine = currentLine.substring(0, currentLine.indexOf('#')).trim();
			}
			
			if(currentLine.startsWith("[")) {
				// New Map Key Handling
				if(currentLine.indexOf(']') < 0) throw new ParseException(lineCount, 
						"Expected ']' to terminate key declaration.");
				else if(currentLine.indexOf(']') < currentLine.length()-1) throw new ParseException(lineCount, 
						"Unexpected String '" + currentLine.substring(currentLine.indexOf(']')+1) + "' in key declaration.");
				else {
					currentKey = currentLine.substring(currentLine.indexOf('[')+1, currentLine.indexOf(']'));
					configMap.put(currentKey, new HashMap<String, String>());
				}
				
			} else {
				// Check for current map key
				if(currentKey == null) throw new ParseException(lineCount, "No key for configuration attribute.");
				else if(!currentLine.contains(":")) throw new ParseException(lineCount, "No attribute-value pair present.");
				else if(currentLine.lastIndexOf(':') != currentLine.indexOf(':'))
					throw new ParseException(lineCount, "Only one attribute delimiter ':' expected per line.");
				configMap.get(currentKey).put(
						currentLine.substring(0, currentLine.indexOf(':')).trim(), 
						currentLine.substring(currentLine.indexOf(':')+1).trim()
						);
			}
			
			// Increment Line Count
			lineCount++;
		}
		currentKey = null;
	}
	
	public boolean selectKey(String key) {
		if(configMap.keySet().contains(key)) {
			currentKey = key;
			return true;
		} else {
			currentKey = null;
			return false;
		}
	}
	
	public Integer parseInt(String attribute) throws ParseException{
		if(currentKey == null) return null;
		else if(!configMap.get(currentKey).keySet().contains(attribute)) return null;
		else {
			try {
				return Integer.parseInt(configMap.get(currentKey).get(attribute));
			} catch(NumberFormatException e) {
				throw new ParseException(-1, "Value for attribute '" + attribute + "' for key '" + currentKey + "' not an integer.");
			}
		}
	}
	
	public Float parseFloat(String attribute) throws ParseException {
		return Float.valueOf(parseDouble(attribute).toString());
	}
	
	public Double parseDouble(String attribute) throws ParseException{
		if(currentKey == null) return null;
		else if(!configMap.get(currentKey).keySet().contains(attribute)) return null;
		else {
			try {
				return Double.parseDouble(configMap.get(currentKey).get(attribute));
			} catch(NumberFormatException e) {
				throw new ParseException(-1, "Value for attribute '" + attribute + "' for key '" + currentKey + "' not a number.");
			}
		}
	}
	
	public String parseString(String attribute) {
		if(currentKey == null) return null;
		else if(!configMap.get(currentKey).keySet().contains(attribute)) return null;
		else {
			return configMap.get(currentKey).get(attribute);
		}
	}
	
	public Float[] parseFloatList(String attribute) throws ParseException {
		Float[] returnList;
		try {
			Double[] list = this.parseNumList(attribute);
			returnList = new Float[list.length];
			for(int i=0; i<returnList.length; i++) {
				returnList[i] = Float.valueOf(list[i].toString());
			}
		} catch(Exception e) {
			throw new ParseException(-1, "Cannot parse attribute '" + attribute + "' for key '" + currentKey + "' as a number list.");
		}
		return returnList;
	}
	
	public Double[] parseNumList(String attribute) throws ParseException{
		if(currentKey == null) return null;
		else if(!configMap.get(currentKey).keySet().contains(attribute)) return null;
		else {
			String param = configMap.get(currentKey).get(attribute);
			if(!param.startsWith("(") || !param.endsWith(")"))
				throw new ParseException(-1, "Cannot parse attribute '" + attribute + "' for key '" + currentKey + "' as a list.");
			String[] params = param.substring(1, param.length()-1).split(",");
			Double[] returnParams = new Double[params.length];
			try {
				for(int i=0; i<params.length; i++) {
					returnParams[i] = Double.parseDouble(params[i].trim());
				}
			} catch(NumberFormatException e) {
				throw new ParseException(-1, "Cannot parse attribute '" + attribute + "' for key '" + currentKey + "' as a number list.");
			}
			return returnParams;
		}
	}
	
	public Set<String> getKeys() {
		return configMap.keySet();
	}
	
	public Set<String> getAttributes() {
		return configMap.get(currentKey).keySet();
	}

}
