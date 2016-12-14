package com.indra.sofia2.ssap.testutils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestProperties {
	
	private static TestProperties instance;
	private Properties properties = new Properties();
	
    
    private TestProperties() throws IOException {
    	InputStream stream = getClass().getClassLoader().getResourceAsStream("test.properties");
    	this.properties.load(stream);
    }
    
    public static synchronized TestProperties getInstance() {
        if(instance == null){
            try {
				instance = new TestProperties();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return instance;
    }
    
    public String get(String property, String defaultValue) {
    	return this.properties.getProperty(property, defaultValue);
    }
    
    public String get(String property) {
    	return this.properties.getProperty(property);
    }

}
