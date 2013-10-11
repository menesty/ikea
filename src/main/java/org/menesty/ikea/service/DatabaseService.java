package org.menesty.ikea.service;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;

public class DatabaseService {

    private static final ObjectContainer db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "data/db/data.db");


    public static  ObjectContainer get(){
        return db;
    }
}
