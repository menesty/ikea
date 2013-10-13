package org.menesty.ikea.service;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.config.EmbeddedConfiguration;

public class DatabaseService {

    private static ObjectContainer instance;


    public static ObjectContainer get() {
        if (instance == null) {
            EmbeddedConfiguration config =Db4oEmbedded.newConfiguration();
            config.common().updateDepth(10);
            config.common().activationDepth(10);
            instance = Db4oEmbedded.openFile(config, "data/db/data.db");
        }
        return instance;
    }
}
