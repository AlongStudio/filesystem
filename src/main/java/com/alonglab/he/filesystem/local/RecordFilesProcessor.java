package com.alonglab.he.filesystem.local;

import com.alonglab.he.filesystem.util.MD5Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class RecordFilesProcessor {
    private static String path = "/Users/victor/Documents/private/iPhone/DCIM";

    public static void main(String[] args) {
        File folder = new File(path);
        handleFile(folder);
    }

    private static void handleFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                handleFile(f);
            }
        } else if (file.isFile()) {
            if (true) {
                fileProcessor(file);
            }
        } else {
            System.out.println("++++++++++++++++++++++++++");
            System.out.println("++ERROR FILE:" + file.getAbsolutePath() + "+++++");
            System.out.println("++++++++++++++++++++++++++");
        }

    }

    private static void fileProcessor(File file) {
        String name = file.getName();
        String path = file.getAbsolutePath();
        long size = file.length();
        try {
            String md5 = MD5Util.getMD5(size, new FileInputStream(file));
            System.out.println(name + "==" + size + "==" + md5 + "==" + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
