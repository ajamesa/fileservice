package com.scd.filesdk.engine;

import com.scd.filesdk.config.Ftp;
import com.scd.filesdk.model.param.BreakParam;
import com.scd.filesdk.model.vo.BreakResult;
import com.scd.filesdk.util.FileUtil;
import com.scd.filesdk.util.FtpUtilMulti;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author chengdu
 * @date 2019/6/20.
 */
@Component
public class FtpEngine extends BaseEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(FtpEngine.class);

    @Autowired
    private Ftp ftp;

    @Override
    public String upload(String filePath) throws Exception {
        String filename = FileUtil.getFileName(filePath);
        InputStream inputStream = new FileInputStream(filePath);
        return upload(inputStream, filename);
    }

    @Override
    public String upload(InputStream inputStream, String filename) throws Exception {
        // 连接远程客户端
        FTPClient ftpClient = FtpUtilMulti.connectFtp(ftp.getHost(), ftp.getPort(),
                ftp.getUsername(), ftp.getPassword());
        String destPath = FileUtil.getDestPath(ftp.getPath());
        // 上传文件
        return FtpUtilMulti.upload(ftpClient, inputStream, destPath, filename);
    }

    @Override
    public String upload(byte[] fbyte, String filename) throws Exception {
        String ftptemp = "ftptemp" + "/" + filename;
        FileUtil.writeByteToFile(fbyte, ftptemp);
        InputStream inputStream = new FileInputStream(ftptemp);
        String remotepath = upload(inputStream, filename);
        FileUtil.deleteFile(ftptemp);
        return remotepath;
    }

    @Override
    public InputStream download(String remotePath) throws IOException {
        // 连接远程客户端
        FTPClient ftpClient = FtpUtilMulti.connectFtp(ftp.getHost(), ftp.getPort(),
                ftp.getUsername(), ftp.getPassword());
        return FtpUtilMulti.download(ftpClient, remotePath);
    }

    @Override
    public BreakResult upload(BreakParam breakParam) {
        BreakResult breakResult = new BreakResult();
        String originFileName = breakParam.getName();
        int chunk = breakParam.getChunk();
        try {
            // 连接远程客户端
            FTPClient ftpClient = FtpUtilMulti.connectFtp(ftp.getHost(), ftp.getPort(),
                    ftp.getUsername(), ftp.getPassword());
            InputStream inputStream = breakParam.getFile().getInputStream();
            String destPath = FileUtil.getDestPath(ftp.getPath());
            String fileName =  chunk + "_" + breakParam.getChunkSize() + "_" + originFileName;
            String storePath = FtpUtilMulti.upload(ftpClient, inputStream, destPath, fileName);
            breakResult.setFilePath(storePath);
            breakResult.setWriteSuccess(true);
        }catch (Exception e){
            LOGGER.error("upload chunk file to Ftp error filename : {} chunk : {}", originFileName, breakParam.getChunk());
            breakResult.setWriteSuccess(false);
        }
        return breakResult;
    }
}