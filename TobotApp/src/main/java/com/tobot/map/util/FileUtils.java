package com.tobot.map.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author houdeming
 * @date 2018/4/13
 */
public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * 保存文件
     *
     * @param bitmap
     * @param fileDir
     * @param fileName
     */
    public static void saveFile(Bitmap bitmap, String fileDir, String fileName) {
        OutputStream stream;
        try {
            File src = new File(fileDir);
            if (!src.exists()) {
                src.mkdirs();
            }

            File file = new File(src, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }

            stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除文件
     *
     * @param fileDir
     * @param fileName
     */
    public static void deleteFile(String fileDir, String fileName) {
        try {
            File file = new File(fileDir, fileName);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFiles(String fileDir) {
        try {
            File dir = new File(fileDir);
            if (dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        if (file.isFile()) {
                            file.delete();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFile(String[] filePath) {
        try {
            if (filePath != null && filePath.length > 0) {
                File file;
                for (String path : filePath) {
                    file = new File(path);
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void renameFile(String oldFilePath, String newFilePath) {
        try {
            File file = new File(oldFilePath);
            if (file.exists()) {
                file.renameTo(new File(newFilePath));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String[] getFilePath(String fileDir) {
        String[] array = null;
        try {
            File dir = new File(fileDir);
            if (dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null && files.length > 0) {
                    int length = files.length;
                    array = new String[length];
                    for (int i = 0; i < length; i++) {
                        array[i] = files[i].getAbsolutePath();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return array;
    }

    /**
     * 获取文件夹
     *
     * @param context
     * @param directory
     * @return
     */
    public static String getFolder(Context context, String directory) {
        String path;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            path = context.getFilesDir().getAbsolutePath();
        }

        if (!TextUtils.isEmpty(directory)) {
            path = path.concat(File.separator).concat(directory);
        }
        return path;
    }

    public static String readTxtFile(String filePath) {
        String text = "";
        File file = new File(filePath);
        if (file.exists()) {
            BufferedReader reader;
            try {
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream in = new BufferedInputStream(fis);
                in.mark(4);
                byte[] first3bytes = new byte[3];
                // 找到文档的前三个字节并自动判断文档类型。
                in.read(first3bytes);
                in.reset();

                if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB && first3bytes[2] == (byte) 0xBF) {
                    reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                } else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFE) {
                    reader = new BufferedReader(new InputStreamReader(in, "unicode"));
                } else if (first3bytes[0] == (byte) 0xFE && first3bytes[1] == (byte) 0xFF) {
                    reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_16BE));
                } else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFF) {
                    reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_16LE));
                } else {
                    reader = new BufferedReader(new InputStreamReader(in, "GBK"));
                }

                String str = reader.readLine();
                String sign = "/n";

                while (str != null) {
                    text = text.concat(str).concat(sign);
                    str = reader.readLine();
                }

                if (!TextUtils.isEmpty(text) && text.endsWith(sign)) {
                    int index = text.lastIndexOf(sign);
                    text = text.substring(0, index);
                }
                reader.close();
                fis.close();
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return text;
    }

    public static List<String> getFileNameList(String dir) {
        try {
            File file = new File(dir);
            if (file.exists()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    List<String> data = new ArrayList<>();
                    for (File childFile : files) {
                        if (childFile.isFile()) {
                            data.add(childFile.getName());
                        }
                    }

                    return data;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<String> getFileNameList(String dir, String fileSuffix) {
        try {
            File file = new File(dir);
            if (file.exists()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    List<String> data = new ArrayList<>();
                    for (File childFile : files) {
                        if (childFile.isFile()) {
                            String fileName = childFile.getName();
                            String name = fileName.substring(fileName.lastIndexOf("."));
                            if (name.equalsIgnoreCase(fileSuffix)) {
                                data.add(fileName);
                            }
                        }
                    }

                    return data;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<String> getWordPath(String dir) {
        try {
            File file = new File(dir);
            if (file.exists()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    List<String> data = new ArrayList<>();
                    for (File childFile : files) {
                        if (childFile.isFile()) {
                            if (isWord(childFile.getName())) {
                                data.add(childFile.getAbsolutePath());
                            }
                        }
                    }

                    return data;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<String> getTxtPath(String dir) {
        try {
            File file = new File(dir);
            if (file.exists()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    List<String> data = new ArrayList<>();
                    for (File childFile : files) {
                        if (childFile.isFile()) {
                            if (isTxt(childFile.getName())) {
                                data.add(childFile.getAbsolutePath());
                            }
                        }
                    }

                    return data;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<String> getPhotoPath(String dir) {
        File file = new File(dir);
        if (file.exists()) {
            List<String> paths = new ArrayList<>();
            return getChildPhotoPath(file, paths);
        }

        return null;
    }

    private static List<String> getChildPhotoPath(File file, List<String> data) {
        File[] files = file.listFiles();
        if (files != null && files.length > 0) {
            for (File childFile : files) {
                if (childFile.isDirectory()) {
                    getChildPhotoPath(childFile, data);
                } else {
                    if (isPicture(childFile.getName())) {
                        data.add(childFile.getAbsolutePath());
                    }
                }
            }
        }

        return data;
    }

    public static List<String> getVideoPath(String dir) {
        File file = new File(dir);
        if (file.exists()) {
            List<String> paths = new ArrayList<>();
            return getChildVideoPath(file, paths);
        }

        return null;
    }

    private static List<String> getChildVideoPath(File file, List<String> data) {
        try {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File childFile : files) {
                    if (childFile.isDirectory()) {
                        getChildVideoPath(childFile, data);
                    } else {
                        if (isVideo(childFile.getName())) {
                            data.add(childFile.getAbsolutePath());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public static boolean isVideo(String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            try {
                String name = fileName.substring(fileName.lastIndexOf("."));
                return ".mp4".equalsIgnoreCase(name) || ".3gp".equalsIgnoreCase(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static boolean isMusic(String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            try {
                String name = fileName.substring(fileName.lastIndexOf("."));
                return ".mp3".equalsIgnoreCase(name) || ".wav".equalsIgnoreCase(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static boolean isPicture(String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            try {
                String name = fileName.substring(fileName.lastIndexOf("."));
                return ".png".equalsIgnoreCase(name) || ".jpg".equalsIgnoreCase(name) || ".jpeg".equalsIgnoreCase(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static boolean isWord(String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            try {
                String name = fileName.substring(fileName.lastIndexOf("."));
                return ".doc".equalsIgnoreCase(name) || ".docx".equalsIgnoreCase(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static boolean isTxt(String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            try {
                String name = fileName.substring(fileName.lastIndexOf("."));
                return ".txt".equalsIgnoreCase(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
