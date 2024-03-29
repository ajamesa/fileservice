package com.scd.filesdk.config;

import com.scd.filesdk.common.ServiceInfo;
import com.scd.filesdk.engine.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chengdu
 */
@Component
public class InitConfig implements InitializingBean{

    @Autowired
    private LocalEngine localEngine;

    @Autowired
    private SftpEngine sftpEngine;

    @Autowired
    private FtpEngine ftpEngine;

    @Autowired
    private MongoEngine mongoEngine;

    @Autowired
    private FdfsEngine fdfsEngine;

    public Map<String, BaseEngine> getEngineMap() {
        return engineMap;
    }


    private Map<String, BaseEngine> engineMap = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        engineMap.put(ServiceInfo.ENGINE.LOCAL,localEngine);
        engineMap.put(ServiceInfo.ENGINE.SFTP, sftpEngine);
        engineMap.put(ServiceInfo.ENGINE.FTP, ftpEngine);
        engineMap.put(ServiceInfo.ENGINE.MONGO, mongoEngine);
        engineMap.put(ServiceInfo.ENGINE.FDSF, fdfsEngine);
    }
}
