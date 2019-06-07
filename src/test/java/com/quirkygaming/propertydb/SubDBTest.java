package com.quirkygaming.propertydb;

import java.io.File;

import com.quirkygaming.errorlib.ErrorHandler;
import com.quirkygaming.propertydb.InitializationToken;
import com.quirkygaming.propertydb.PropertyDB;
import com.quirkygaming.propertydb.sublayer.SubDB;
import com.quirkygaming.propertylib.MutableProperty;

public class SubDBTest {
	static MutableProperty<String> object1;
	
	static final File DIR = new File("/tmp/test_db/");
	
	public static void main(String[] args) {
		InitializationToken token = PropertyDB.initializeDB(4000);
		
		try {
			SubDB<RuntimeException> db = new SubDB<RuntimeException>("Sub1", DIR, ErrorHandler.throwAll());
			
			object1 = db.initiateProperty("object1", 1, "Object1");
			System.out.println(object1.get());
			object1.set("Loaded_set");
			db.unloadProperty("object1");
			object1.set("Unloaded_set");
			object1 = db.initiateProperty("object1", 1, "Object1");
			System.out.println(object1.get());
			db.deleteProperty(object1);
			object1 = db.initiateProperty("object1", 1, "Object1_2");
			System.out.println(object1.get());
			object1.set("xyz");
			db.deleteProperty("object1");
			System.out.println(object1.get());
			object1 = db.initiateProperty("object1", 1, "Object1_3");
			System.out.println(db.getLoadedProperty("object1"));
			System.out.println(db.getPropertyList().size());
			db.unloadProperty("object1");
			System.out.println(db.getPropertyList().size());
			System.out.println(db.isLoaded("object1"));
			System.out.println(db.propertyExists("object1"));
			db.deleteProperty("object1");
			System.out.println(db.isLoaded("object1"));
			System.out.println(db.propertyExists("object1"));
			
		} catch (Throwable t) {
			t.printStackTrace();
		} finally{
			PropertyDB.closeDatabase(token);
		}
	}
}
