package config;

import java.io.File;

public class ObjectFile extends File {

	private static final long serialVersionUID = 4613730197608037693L;

	public ObjectFile(String pathname) {
		super(pathname);
	}
	
	public ObjectFile(File file) {
		super(file.getAbsolutePath());
	}

}
