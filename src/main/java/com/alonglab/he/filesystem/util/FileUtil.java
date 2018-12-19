package com.alonglab.he.filesystem.util;

import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtil {

    public static void main0(String[] args) {
        String f1 = "D:\\My Documents\\桌面\\test\\IMG_0668.JPG";
//        String f2 = "/Users/victor/Documents/private/[备份]VictorPhone/相册/2016年01月/IMG_0484.MOV";
        String f2 = "E:\\20181205\\照片\\我的照片\\2018年04月\\IMG_0698.JPG";
        long start = System.currentTimeMillis();
        System.out.println(checkTotallySame(new File(f1), new File(f2)));
        long end = System.currentTimeMillis();
        System.out.println("used " + (end - start) + "ms.");
    }

    public static void main1(String args[]) {
        Pattern pattern = Pattern.compile("[0-9]{4}[-][0-9]{1,2}[-][0-9]{1,2}");
        Matcher matcher = pattern.matcher("2015-11-21 175756.jpg");
//        Matcher matcher = pattern.matcher("2、开标时间：2018-08-02 14:00:00。2、开标时间：2018-08-02 16:00:00。2、开标时间：2018-08-02 15:00:00。");
        while (matcher.find()) {
            System.out.println(matcher.group());
        }

    }

    public static void main(String[] args) {
        String[] f1 = {"IMG_5651.PNG", "2015-11-21 175756.jpg", "2015-11-21 175756(1).jpg", "IMG_4567.jpg", "DSC00019.jpg", "BLTB7292.mp4", "3705.mp4"
                , "IMG_20110519_230824.jpg", "IMG_20110519_230824 (2).jpg"};
        for (String s : f1) {
            System.out.println(s + "\t\t" + getNameScore(s));
        }
    }

    public static boolean checkTotallySame(File file1, File file2) {
        if (file1.getAbsolutePath().equalsIgnoreCase(file2.getAbsolutePath())) {
            String error = "Something is wrong! They are the same file!!"
                    + file1.getAbsolutePath() + "," + file2.getAbsolutePath();
            throw new RuntimeException(error);
        }
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

    /**
     * 给名字算分
     * 普遍使用减去 mainFileName.length()的原因是很多重复文件的名字带有(1)，这样字符多，减去后分数就低，原始文件就会分数高
     * <p>
     * 2015-11-21 175756.jpg
     * 2015-11-21 175756(1).jpg
     * IMG_4567.jpg
     * DSC00019.jpg
     * <p>
     * BLTB7292.mp4
     * 3705.mp4
     *
     * @param name
     * @return
     */
    public static int getNameScore(String name) {
        int score = 0;
        String mainFileName = name.substring(0, name.lastIndexOf("."));
        Pattern pattern1 = Pattern.compile("[0-9]{4}[-][0-9]{1,2}[-][0-9]{1,2}");
        Matcher matcher1 = pattern1.matcher(mainFileName);
        if (matcher1.find()) {
            score = 500 + (100 - mainFileName.length());//2015-11-21 175756.jpg/2015-11-21 175756(1).jpg
        }
        if (score == 0) {
            Pattern pattern2 = Pattern.compile("[0-9]{4}[0-9]{1,2}[0-9]{1,2}");
            Matcher matcher2 = pattern2.matcher(mainFileName);
            if (matcher2.find()) {
                score = 400 + (100 - mainFileName.length());//2015-11-21 175756.jpg/2015-11-21 175756(1).jpg
            }
        }
        if (score == 0 && mainFileName.length() > 10) {
            score = 100 - mainFileName.length();//01f35cfda89f6078ff41a125bac90af1.jpg 一般是微信缓存图片
        }
        if (score == 0) {
            score = 200 - mainFileName.length();//IMG_4567.jpg/DSC00019.jpg、BLTB7292.mp4\3705.mp4
        }
        return score;
    }

    private static boolean isValidDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            // 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
            format.setLenient(false);
            format.parse(str);
        } catch (ParseException e) {
            // e.printStackTrace();
            // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
            return false;
        }
        return true;
    }
}