package com.scd.filesdk.engine;

import com.scd.filesdk.common.PoolType;
import com.scd.filesdk.config.Fdfs;
import com.scd.filesdk.exception.DataException;
import com.scd.filesdk.model.param.BreakParam;
import com.scd.filesdk.model.param.UploadParam;
import com.scd.filesdk.model.vo.BreakResult;
import com.scd.filesdk.tools.SingletonPoolTool;
import com.scd.filesdk.util.FdfsUtil;
import com.scd.filesdk.util.FileUtil;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.csource.fastdfs.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author chengdu
 * @date 2019/7/14.
 */
@Component
public class FdfsEngine extends BaseEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(FdfsEngine.class);

    @Autowired
    private Fdfs fdfs;

    @Override
    public String upload(String filePath) throws Exception {
        String filename = FileUtil.getFileName(filePath);
        InputStream inputStream = new FileInputStream(filePath);
        return upload(inputStream, filename);
    }

    @Override
    public String upload(InputStream inputStream, String filename) throws Exception {
        StorageClient storageClient = FdfsUtil.connectFdfs(fdfs.getConfig());
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        String[] result = FdfsUtil.upload(storageClient, bytes, fdfs.getGroup(),filename);
        return result[0] + "," + result[1] + "," + filename;
    }

    @Override
    public String upload(InputStream inputStream, UploadParam uploadParam) throws Exception {
//        StorageClient storageClient = FdfsUtil.connectFdfs(fdfs.getConfig());
//        byte[] bytes = new byte[inputStream.available()];
//        inputStream.read(bytes);
//        String groupName = fdfs.getGroup();
//        if(!StringUtils.isEmpty(uploadParam.getGroupName())){
//            groupName = uploadParam.getGroupName();
//        }
//        String[] result = FdfsUtil.upload(storageClient, bytes, groupName,uploadParam.getFileName());
//        return result[0] + "," + result[1] + "," + uploadParam.getFileName();
         return uploadFile(inputStream, uploadParam);
    }

    @Override
    public String upload(byte[] fbyte, String filename) throws Exception {
        String ftptemp = "fdfstemp" + "/" + filename;
        FileUtil.writeByteToFile(fbyte, ftptemp);
        InputStream inputStream = new FileInputStream(ftptemp);
        String remotepath = upload(inputStream, filename);
        FileUtil.deleteFile(ftptemp);
        return remotepath;
    }

    @Override
    public InputStream download(String remotePath) throws Exception {
//        StorageClient storageClient = FdfsUtil.connectFdfs(fdfs.getConfig());
//        InputStream inputStream = null;
//        if(remotePath.indexOf(",") != -1){
//            String[] storeRes = remotePath.split(",");
//            String group = storeRes[0];
//            String fileId = storeRes[1];
//            String fileName = storeRes[2];
//            byte[] bytes = FdfsUtil.download(storageClient, group, fileId);
//            String ftptemp = "fdfstemp" + "/" + fileName;
//            FileUtil.writeByteToFile(bytes, ftptemp);
//            inputStream = new FileInputStream(ftptemp);
//        }else{
//            throw new DataException("store path data exception " + remotePath);
//        }
        return downloadFile(remotePath);
    }

    private String uploadFile(InputStream inputStream, UploadParam uploadParam) throws Exception {
        GenericObjectPool fdfsClientPool = null;
        StorageClient storageClient = null;
        String remotePath;
        try {
            fdfsClientPool = SingletonPoolTool.createPool(PoolType.FDFS);
            storageClient = (StorageClient) fdfsClientPool.borrowObject();
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            String groupName = fdfs.getGroup();
            if (!StringUtils.isEmpty(uploadParam.getGroupName())) {
                groupName = uploadParam.getGroupName();
            }
            String[] result = FdfsUtil.upload(storageClient, bytes, groupName, uploadParam.getFileName());
            remotePath = result[0] + "," + result[1] + "," + uploadParam.getFileName();
        }catch (Exception e){
            throw new RuntimeException("upload file to fdfs error", e);
        }finally {
            if(fdfsClientPool != null && storageClient != null){
                SingletonPoolTool.showPoolInfo(PoolType.FDFS);
                fdfsClientPool.returnObject(storageClient);
            }
        }
        return remotePath;
    }

    private InputStream downloadFile(String remotePath){
        GenericObjectPool fdfsClientPool = null;
        StorageClient storageClient = null;
        InputStream inputStream;
        try {
            fdfsClientPool = SingletonPoolTool.createPool(PoolType.FDFS);
            storageClient = (StorageClient) fdfsClientPool.borrowObject();
            if(remotePath.indexOf(",") != -1){
                String[] storeRes = remotePath.split(",");
                String group = storeRes[0];
                String fileId = storeRes[1];
                String fileName = storeRes[2];
                byte[] bytes = FdfsUtil.download(storageClient, group, fileId);
                String ftptemp = "fdfstemp" + "/" + fileName;
                FileUtil.writeByteToFile(bytes, ftptemp);
                inputStream = new FileInputStream(ftptemp);
            }else{
                throw new DataException("store path data exception " + remotePath);
            }
        }catch (Exception e){
            throw new RuntimeException("download file from fdfs error", e);
        }finally {
            if(fdfsClientPool != null && storageClient != null){
                SingletonPoolTool.showPoolInfo(PoolType.FDFS);
                fdfsClientPool.returnObject(storageClient);
            }
        }
        return inputStream;
    }

    @Override
    public BreakResult upload(BreakParam breakParam) {
        BreakResult breakResult = new BreakResult();
        String originFileName = breakParam.getName();
        int curChunk = breakParam.getChunk();
        long chunkSize = breakParam.getChunkSize();
        try {
            LOGGER.info("【FDFS】 filename : {}, chunk : {}, chunksize : {}", originFileName, curChunk, chunkSize);
            InputStream inputStream = breakParam.getFile().getInputStream();
            String fileName =  curChunk + "_" + breakParam.getChunkSize() + "_" + originFileName;
            UploadParam uploadParam = new UploadParam(fileName, breakParam.getGroupName());
            String storePath = uploadFile(inputStream, uploadParam);
            breakResult.setWriteSuccess(true);
            breakResult.setFilePath(storePath);
        }catch (Exception e){
            LOGGER.error("upload chunk file to FDFS error filename : {} chunk : {}", originFileName, curChunk);
            breakResult.setWriteSuccess(false);
        }
        return breakResult;
    }
}
