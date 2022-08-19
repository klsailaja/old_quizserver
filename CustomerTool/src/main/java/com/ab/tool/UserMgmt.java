package com.ab.tool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;


public class UserMgmt {
	
	private static String rootUserName = "rajasekhar";
	private static String rootPasswd = "Raj@12345$";
	
	private static UserMgmt instance;
	private Properties props = new Properties();
	private final String USER_KEY = "UD-";
	
	private final String UD_NAME = "NAME";
	private final String UD_PASSWD = "PASSWD";
	private final String UD_ROLE = "ROLE";
	
	private boolean isAdmin = false;
	
	private String path = "D:\\Tool\\users.txt";
	
	
	private UserMgmt() {
	}
	
	public static UserMgmt getInstance() {
		if (instance == null) {
			instance = new UserMgmt();
			//instance.initialize();
			instance.load();
		}
		return instance;
	}
	
	private void store() {
		System.out.println("This is in store");
	    FileOutputStream outputStrem;
		try {
			outputStrem = new FileOutputStream(path);
			props.store(outputStrem, "This is a sample properties file");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void load() {
		try {
			FileInputStream reader = new FileInputStream(path);
			props.load(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<String> userKeys = new ArrayList<>();
		userKeys.add(UD_NAME);
		userKeys.add(UD_PASSWD);
		userKeys.add(UD_ROLE);
		
		List<String> userValues = new ArrayList<>();
		userValues.add(rootUserName);
		userValues.add(rootPasswd);
		userValues.add("ADMIN");
		
		//String newUserKey = USER_KEY + "0";
		//String newUserValue = formStringSyntax(userKeys, userValues);
		//props.setProperty(newUserKey, newUserValue);
	}
	
	public void addUser(String userName, String passwd) throws NotAllowedException {
		@SuppressWarnings("unchecked")
		Enumeration<String> enums = (Enumeration<String>) props.propertyNames();
		int userIndex = 0;
		while (enums.hasMoreElements()) {
			String key = enums.nextElement();
		    String value = props.getProperty(key);
		    
		    Properties props = parseValues(value);
		    String propUserName = props.getProperty(UD_NAME, "");
		    if (propUserName.equals(userName)) {
		    	throw new NotAllowedException("User already exists");
		    }
		    int pos = key.lastIndexOf("-");
		    String userIndexStr = key.substring(pos + 1).trim();
		    if (Integer.parseInt(userIndexStr) > userIndex) {
		    	userIndex = Integer.parseInt(userIndexStr);
		    }
		}
		userIndex++;
		
		List<String> userKeys = new ArrayList<>();
		userKeys.add(UD_NAME);
		userKeys.add(UD_PASSWD);
		userKeys.add(UD_ROLE);
		
		List<String> userValues = new ArrayList<>();
		userValues.add(userName);
		userValues.add(passwd);
		userValues.add("user");
		
		String newUserKey = USER_KEY + userIndex;
		String newUserValue = formStringSyntax(userKeys, userValues);
		props.setProperty(newUserKey, newUserValue);
		store();
	}
	
	public boolean authenticate(String userName, String password) {
		if ((userName.equals(rootUserName)) && (password.equals(rootPasswd))) {
			isAdmin = true;
			return true;
		}
		@SuppressWarnings("unchecked")
		Enumeration<String> enums = (Enumeration<String>) props.propertyNames();
		while (enums.hasMoreElements()) {
			String key = enums.nextElement();
		    String value = props.getProperty(key);
		    
		    Properties props = parseValues(value);
		    String propUserName = props.getProperty(UD_NAME, "");
		    String propPasswd = props.getProperty(UD_PASSWD, "");
		    String propRole = props.getProperty(UD_ROLE, "");
		    
		    if (propUserName.equals(userName)) {
		    	if (propPasswd.equals(propPasswd)) {
		    		if (propRole.equalsIgnoreCase("ADMIN")) {
		    			isAdmin = true;
		    		}
		    		return true;
		    	}
		    }
		}
		return false;
	}
	
	private Properties parseValues(String fullValues) {
		Properties props = new Properties();
		StringTokenizer strTokenizer = new StringTokenizer(fullValues, ";");
		while(strTokenizer.hasMoreTokens()) {
			String keyValue = strTokenizer.nextToken().trim();
			StringTokenizer strTokenizer1 = new StringTokenizer(keyValue, ":");
			props.setProperty(strTokenizer1.nextToken().trim(), strTokenizer1.nextToken().trim());
		}
		return props;
	}
	
	private String formStringSyntax(List<String> keys, List<String> values) {
		StringBuffer strBuffer = new StringBuffer();
		for (int index = 0; index < keys.size(); index ++) {
			strBuffer.append(keys.get(index));
			strBuffer.append(":");
			strBuffer.append(values.get(index));
			strBuffer.append(";");
		}
		return strBuffer.toString();
	}
	
	public boolean isAdmin() {
		return isAdmin;
	}
	
	private void printAll() {
		System.out.println(props);
	}
	
	public static void main(String[] args) {
		UserMgmt.getInstance().addUser("test1", "testing");
		UserMgmt.getInstance().printAll();
	}
}
