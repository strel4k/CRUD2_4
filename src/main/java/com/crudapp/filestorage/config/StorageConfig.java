package com.crudapp.filestorage.config;

public class StorageConfig {
    private static final String DEFAULT_ROOT = "/tmp/filestorage";

    public static String storageRoot() {
        String p = System.getProperty("storage.root");
        if (p != null && p.isBlank()) return p;
        String e = System.getenv("STORAGE_ROOT");
        if (e != null && !e.isBlank()) return e;
        return DEFAULT_ROOT;
    }
    private StorageConfig(){}
}
