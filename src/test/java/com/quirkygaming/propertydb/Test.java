package com.quirkygaming.propertydb;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

import com.quirkygaming.errorlib.ErrorHandler;
import com.quirkygaming.propertydb.InitializationToken;
import com.quirkygaming.propertydb.PropertyDB;
import com.quirkygaming.propertylib.MutableProperty;

public class Test {
	
	// Commands:
	
	// initialize [period]
	// exit
	// interrupt
	// init {propertyName}
	// set {propertyName} {value}
	// get {propertyName}
	// update {propertyName}
	// delete {propertyName}
	// unload {propertyName}
	
	static InitializationToken token;
	static final File DIR = new File("/tmp/test_db/");
	
	public static void main(String[] args) {
		HashMap<String, MutableProperty<Integer>> props = new HashMap<>();
		
		if (!Test.class.desiredAssertionStatus()) {
			System.out.println("ENABLE ASSERTIONS FOR DEBUG OUTPUT");
		}
		
		System.out.println("Enter a command:");
		Scanner in = new Scanner(System.in);
		
		while (true) {
			System.out.print(">");
			String[] cmd = in.nextLine().trim().split(" ");
			if (cmd[0].equals("interrupt")) {
				interruptTest();
			} else if (cmd[0].equals("exit")) {
				if (PropertyDB.initialized()) PropertyDB.closeDatabase(token);
				break;
			} else if (cmd[0].equals("initialize")) {
				int period = 5000;
				if (cmd.length > 1) period = Integer.parseInt(cmd[1]);
				token = PropertyDB.initializeDB(period);
			} else if (cmd[0].equals("close")) {
				PropertyDB.closeDatabase(token);
			} else if (cmd[0].equals("init")) {
				props.put(cmd[1], PropertyDB.initiateProperty(DIR, cmd[1], 1, 0, ErrorHandler.logAll(System.err, true)));
			} else if (cmd[0].equals("set")) {
				props.get(cmd[1]).set(Integer.parseInt(cmd[2]));
			} else if (cmd[0].equals("get")) {
				System.out.println(props.get(cmd[1]).get());
			} else if (cmd[0].equals("update")) {
				props.get(cmd[1]).update();
			} else if (cmd[0].equals("delete")) {
				PropertyDB.deleteProperty(props.get(cmd[1]), ErrorHandler.logAll(System.err, true));
				props.remove(cmd[1]);
			} else if (cmd[0].equals("unload")) {
				PropertyDB.unloadProperty(props.get(cmd[1]), ErrorHandler.logAll(System.err, true));
				props.remove(cmd[1]);
			}
			
		}
		
		System.out.println("Exiting");
		in.close();
	}
	
	public static void interruptTest() {
		token = PropertyDB.initializeDB(1000);
		System.out.println("Initialized: " + PropertyDB.initialized());
		sleep(2110);
		PropertyDB.closeDatabase(token);
		token = PropertyDB.initializeDB(5000);
		sleep(2000);
		PropertyDB.closeDatabase(token);
	}
	
	public static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
