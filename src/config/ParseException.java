package config;

public class ParseException extends Exception {

	private static final long serialVersionUID = -6154393384666505985L;
	
	int lineNum;
	String problem;
	
	public ParseException(int lineN, String newProblem) {
		lineNum = lineN;
		problem = newProblem;
	}
	
	public String getInformation() {
		return "Error parsing file at line " + lineNum + ".\n" + problem;
	}
	
	public void printStackTrace() {
		System.err.println("Error parsing file at line " + lineNum + ".");
		System.err.println(problem);
		super.printStackTrace();
	}

}
