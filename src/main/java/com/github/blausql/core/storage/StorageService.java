package com.github.blausql.core.storage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public final class StorageService {

    // TODO: move all storage-related operations to this service

    private static final File USER_HOME = new File(System.getProperty("user.home"));

    private static final File BLAU_SQL_DIR = new File(USER_HOME, ".blauSQL");

    private static final StorageService INSTANCE = new StorageService();

    private static final FileFilter FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.isFile();
        }
    };

    private static final Comparator<File> FILE_NAME_COMPARATOR = new Comparator<File>() {
        @Override
        public int compare(File leftFile, File rightFile) {
            return leftFile.getName().compareTo(rightFile.getName());
        }
    };

    public static StorageService getInstance() {
        return INSTANCE;
    }

    public File getApplicationSettingsDirectory() {
        return BLAU_SQL_DIR;
    }


    public void saveFileToApplicationDirectory(String subDirectory, String filename, String content) {

        File directory = getApplicationSubDirectory(subDirectory);
        if (!directory.exists()) {
            boolean couldCreate = directory.mkdirs();
            if (!couldCreate) {
                throw new RuntimeException("Could not create directory: " + directory.getAbsolutePath());
            }
        }

        File file = new File(directory, filename);
        if (file.exists()) {
            throw new RuntimeException("File exists already: " + file.getAbsolutePath());
        }

        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            pw.write(content);

        } catch (IOException e) {
            throw new RuntimeException("Failed to write: " + file.getAbsolutePath(), e);
        }
    }

    private File getApplicationSubDirectory(String subDirectory) {
        return new File(BLAU_SQL_DIR, subDirectory);
    }

    public String getFileFromApplicationDirectory(String sqlFileDir, String fileName) {


        File directory = getApplicationSubDirectory(sqlFileDir);
        if (!directory.exists()) {
            throw new RuntimeException("Directory not found: " + directory.getAbsolutePath());
        }

        File file = new File(directory, fileName);
        if (!file.exists()) {
            throw new RuntimeException("File not found: " + file.getAbsolutePath());
        }

        try {
            byte[] encoded = Files.readAllBytes(file.toPath());
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not read file: " + file.getAbsolutePath());
        }
    }

    public List<String> listFilesInFromApplicationDirectory(String sqlFileDir) {

        File directory = getApplicationSubDirectory(sqlFileDir);
        if (!directory.exists()) {
            return Collections.emptyList();
        }

        File[] files = directory.listFiles(FILE_FILTER);

        if (files == null) {
            return Collections.emptyList();
        }

        List<File> filesList = Arrays.asList(files);
        Collections.sort(filesList, FILE_NAME_COMPARATOR);

        LinkedList<String> result = new LinkedList<>();
        for (File f : filesList) {
            result.add(f.getName());
        }

        return result;
    }

    public void deleteFileFromApplicationDirectory(String sqlFileDir, String fileName) {
        File directory = getApplicationSubDirectory(sqlFileDir);
        File file = new File(directory, fileName);

        boolean couldDelete = file.delete();
        if (!couldDelete) {
            throw new RuntimeException("Could not delete: " + file.getAbsolutePath());
        }
    }
}
