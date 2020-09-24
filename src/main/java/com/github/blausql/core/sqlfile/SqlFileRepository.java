package com.github.blausql.core.sqlfile;

import com.github.blausql.core.storage.StorageService;

import java.util.List;

public final class SqlFileRepository {

    private static final String SQL_FILE_DIR = "sqlfiles";

    public static SqlFileRepository getInstance() {
        return INSTANCE;
    }

    private static final SqlFileRepository INSTANCE = new SqlFileRepository();

    private final StorageService storageService = StorageService.getInstance();

    public void saveSqlFile(SqlFile sqlFile) {

        storageService.saveFileToApplicationDirectory(SQL_FILE_DIR, sqlFile.getFilename(), sqlFile.getContent());
    }

    public List<String> listSqlFileNames() {
        return storageService.listFilesInFromApplicationDirectory(SQL_FILE_DIR);
    }

    public String getFileContentBySqlFileName(String fileName) {
        return storageService.getFileFromApplicationDirectory(SQL_FILE_DIR, fileName);
    }

    public void deleteByFileName(String fileName) {
        storageService.deleteFileFromApplicationDirectory(SQL_FILE_DIR, fileName);
    }
}
