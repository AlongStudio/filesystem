package com.alonglab.he.filesystem.util;

import java.io.File;
import java.io.FileInputStream;

public class FileUtil {

    public static void main(String[] args) {
        String f1 = "/Users/victor/Downloads/AlongLab/[备份]VictorPhone/相册/2016年01月/IMG_0484.MOV";
//        String f2 = "/Users/victor/Documents/private/[备份]VictorPhone/相册/2016年01月/IMG_0484.MOV";
        String f2 = "/Users/victor/Documents/private/iPhone/DCIM/100APPLE/IMG_0085.MOV";
        long start = System.currentTimeMillis();
        System.out.println(checkTotallySame(new File(f1), new File(f2)));
        long end = System.currentTimeMillis();
        System.out.println("used " + (end - start) + "ms.");
    }

    public static boolean checkTotallySame(File file1, File file2) {
        try (FileInputStream inputStream1 = new FileInputStream(file1);
             FileInputStream inputStream2 = new FileInputStream(file2)) {
            if (inputStream1.available() != inputStream2.available()) {
                //长度不同内容肯定不同
                return false;
            } else {
                byte[] bytes1 = new byte[10240];
                byte[] bytes2 = new byte[10240];

                while (true) {
                    int read1 = inputStream1.read(bytes1);
                    int read2 = inputStream2.read(bytes2);
                    if (read1 != read2) {
                        return false;
                    }
                    if (read1 == -1) {
                        break;
                    }
                    boolean isSame = checkArraySame(bytes1, bytes2, 0, read1 - 1);
                    if (!isSame) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkArraySame(byte[] b1, byte[] b2, int start, int end) {
        for (int i = start; i <= end; i++) {
            if (b1[i] != b2[i]) {
                return false;
            }
        }
        return true;
    }
}