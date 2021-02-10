package com.chat.Models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;

public class Application {
    private final static Path appStoragePath = FileSystems.getDefault().getPath(".dataApp");
    private final static Path filePath = appStoragePath.resolve(".fileStorage");
    private final static Path historyPath = appStoragePath.resolve(".convHistory");
    private final static Path settingsPath = appStoragePath.resolve("settings");
    private final static String[] imageExtension = {"bmp", "gif", "jpeg", "jpg", "png"};

    public static void createAppDirectory() {
        File f = appStoragePath.toFile();
        if (!f.exists()) f.mkdirs();
    }

    public static void deleteDataDirectory() {
        File d = filePath.toFile();
        if (d.exists()) {
            for (File f : filePath.toFile().listFiles()) {
                f.delete();
            }
        }
        d = historyPath.toFile();
        if (d.exists()) {
            for (File f : historyPath.toFile().listFiles()) {
                f.delete();
            }
        }
    }



    public static void createFileDirectory() {
        createAppDirectory();
        File f = filePath.toFile();
        if (!f.exists()) f.mkdirs();
    }

    public static void downloadFile(File source, File dest) {
        try {
            FileInputStream fis = new FileInputStream(source);
            byte[] byteArray = new byte[fis.available()];
            fis.read(byteArray, 0, byteArray.length);
            fis.close();
            FileOutputStream os = new FileOutputStream(dest);
            os.write(byteArray);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Message uploadFile(byte[] file, boolean received) {
        createFileDirectory();
        try {
            String name = new String(Arrays.copyOfRange(file,1, file[0]+1), StandardCharsets.US_ASCII);
            String extension = new String(Arrays.copyOfRange(file,file[0] + 2, file[file[0] + 1] + file[0] + 2), StandardCharsets.US_ASCII);
            File f = filePath.resolve(new Date().getTime() + "." + extension).toFile();
            FileOutputStream os = new FileOutputStream(f);
            os.write(Arrays.copyOfRange(file,file[file[0] + 1] + file[0] + 2, file.length));
            os.flush();
            os.close();
            return new FileMessage(f, name + "." + extension, received);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void createHistoryDirectory() {
        createAppDirectory();
        File f = historyPath.toFile();
        if (!f.exists()) f.mkdirs();
    }

    public static void createConvHistoryFile(String id) {
        createHistoryDirectory();
        File f = historyPath.resolve(id).toFile();
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Path getHistoryPath() {
        return historyPath;
    }

    public static String getFileName(File file) {
        return file.getName().substring(0, file.getName().lastIndexOf(".")).replace(':','_');
    }

    public static String getFileExtension(File file) {
        return file.getName().substring(file.getName().lastIndexOf(".") + 1);
    }

    public static boolean isImage(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        for (String e : imageExtension) {
            if (extension.equalsIgnoreCase(e)) return true;
        }
        return false;
    }

    public static byte[] fileToByteArray(File file) {
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
            byte[] fileName = Application.getFileName(file).getBytes(StandardCharsets.US_ASCII);
            byte[] extension = Application.getFileExtension(file).getBytes(StandardCharsets.US_ASCII);
            byte[] byteArray = new byte[fis.available() + fileName.length + extension.length + 2];
            byteArray[0] = (byte)fileName.length;
            System.arraycopy(fileName, 0, byteArray, 1, fileName.length);
            byteArray[fileName.length + 1] = (byte)extension.length;
            System.arraycopy(extension, 0, byteArray, fileName.length + 2, extension.length);
            fis.read(byteArray, fileName.length + extension.length + 2, fis.available());
            fis.close();
            return byteArray;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
