package com.scd.filesdk.util;

import com.scd.filesdk.engine.LocalEngine;
import com.scd.fileservice.common.CommonConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * @author chengdu
 * @date 2019/6/17.
 */
public class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 创建文件夹
     * @param filepath
     */
    public static void mkdirs(String filepath){
        File file = new File(filepath);
        if(!file.exists()){
            if(file.mkdirs()){
                LOGGER.info("makedir success, dir path {}", filepath);
            }else{
                LOGGER.error("makedir error, dir path {}", filepath);
            }
        }
    }

    /**
     * 根据文件路径获取文件名字
     * @param filepath
     * @return
     */
    public static String getFileName(String filepath){
        filepath = filepath.replace("\\","/");
        int lastSeparator = filepath.lastIndexOf("/");
        if(lastSeparator != -1){
            return filepath.substring(lastSeparator + 1);
        }else{
            throw new RuntimeException("can not find filename [filepath] :" + filepath);
        }
    }

    /**
     * 根据文件路径获取文件目录
     * @param filepath
     * @return
     */
    public static String getFileDir(String filepath){
        filepath = filepath.replace("\\","/");
        int lastSeparator = filepath.lastIndexOf("/");
        if(lastSeparator != -1){
            return filepath.substring(0, lastSeparator);
        }else{
            throw new RuntimeException("can not find filepath [filepath] :" + filepath);
        }
    }

    /**
     * 根据文件路径复制文件
     * @param sourcePath
     * @param targetPath
     * @throws IOException
     */
    public static void copyFile(String sourcePath, String targetPath) throws IOException {
        // 创建文件夹
        String targetDir = getFileDir(targetPath);
        mkdirs(targetDir);
        // 创建目标文件
        File targetFile = new File(targetPath);
        if(!targetFile.exists()){
            targetFile.createNewFile();
        }
        copyFileInStream(new File(sourcePath), targetFile);
    }

    /**
     * 复制文件
     * @param sourceFile
     * @param targetFile
     * @throws IOException
     */
    public static void copyFileInStream(File sourceFile, File targetFile) throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(sourceFile);
            output = new FileOutputStream(targetFile);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) != -1) {
                output.write(buf, 0, bytesRead);
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if(input != null){
                input.close();
            }
            if(output != null){
                output.close();
            }
        }
    }

    /**
     * 将文件输入流写入指定路径
     * @param inputStream
     * @param targetpath
     * @throws IOException
     */
    public static void writeInputStreamToLocal(InputStream inputStream, String targetpath) throws IOException{
        FileOutputStream fileOutputStream = null;
        // 创建文件目录
        String filedir = getFileDir(targetpath);
        mkdirs(filedir);
        try {
            fileOutputStream = new FileOutputStream(targetpath);
//            int size = inputStream.available();
//            byte[] filebytes = new byte[size];
//            fileOutputStream.write(filebytes);
            int index;
            byte[] filebytes = new byte[1024];
            while ((index = inputStream.read(filebytes)) != -1){
                fileOutputStream.write(filebytes, 0, index);
                fileOutputStream.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(fileOutputStream != null){
                fileOutputStream.close();
            }
        }
    }

    /**\
     * 根据文件路径获取文件byte流
     * @param filePath
     * @return
     * @throws IOException
     */
    private byte[] getBytes(String filePath) throws IOException {
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            fis = new FileInputStream(file);
            bos = new ByteArrayOutputStream(1024);
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fis != null){
                fis.close();
            }
            if(bos != null){
                bos.close();
            }
        }
        return buffer;
    }

    /**
     * 将byte文件流写入文件
     * @param fbyte
     * @param filepath
     * @throws IOException
     */
    public static void writeByteToFile(byte[] fbyte, String filepath) throws IOException {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        // 创建文件夹
        String dirpath = getFileDir(filepath);
        mkdirs(dirpath);
        File file = new File(filepath);
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(fbyte);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                bos.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    public static void getFilePaths(String filepath, List<String> pathList) throws FileNotFoundException{
        File file = new File(filepath);
        if(!file.exists()){
            throw new FileNotFoundException("not find file "+filepath);
        }
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for(File f : files){
                if(f.isDirectory()){
                    getFilePaths(f.getAbsolutePath(), pathList);
                }else{
                    pathList.add(f.getAbsolutePath());
                }
            }
        }else{
            pathList.add(filepath);
        }
    }

    public static void deleteFile(String filepath) {
        File file = new File(filepath);
        if(file.exists() && file.isFile()){
            file.delete();
        }
    }

    public static void main(String[] args) throws IOException {
        String sourcePath = "E:\\Github\\fileservice\\src\\main\\java\\com\\scd\\filesdk\\engine\\BaseEngine.java";
        String targetPath = "/home/scd/upload/aaa" + File.separator + "BaseEngine.java";
//        String filename = getFileName(testPath);
//        System.out.println(filename);
//        copyFile(sourcePath, targetPath);
        String basePath = "C:\\Users\\chengdu\\Desktop\\filetype";
        List<String> filePaths = new ArrayList<>(10);
        getFilePaths(basePath, filePaths);
        System.out.println(filePaths);
        Set<String> filesets = new HashSet<>();
        for(int i = 0; i < 2; i++) {
            String uuid = UUID.randomUUID().toString();
            filesets.add(uuid);
        }
        System.out.println(filesets);
        System.out.println(System.currentTimeMillis());
        String key = String.format(CommonConstant.FILE_INFO, "2434");
        System.out.println(key);
    }
}
