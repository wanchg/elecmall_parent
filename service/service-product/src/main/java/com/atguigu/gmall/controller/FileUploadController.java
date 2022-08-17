package com.atguigu.gmall.controller;


import com.atguigu.gmall.common.result.R;
import org.apache.commons.io.FilenameUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/admin/product")
public class FileUploadController {


    @Value("fileServer.url")
    private String url;
    //上传文件

    /**
     * 1.加栽配置文件tracker.conf
     * 2.初始化当前配置文件
     * 3.创建TrackerClient
     * 4.创建TrackerServer
     * 5.创建StorageServer
     * 6. 文件上传
     * @param file
     * @return
     */
    @PostMapping("/fileUpload")
    public R fileUpload(MultipartFile file) throws IOException, MyException {
        //1.加栽配置文件tracker.conf
        String configFile = this.getClass().getResource("/tracker.conf").getFile();
        //获取到的文件保存路径
        String path = null;
        //2.初始化当前配置文件
        if (configFile != null){
            ClientGlobal.init(configFile);
            //3.创建TrackerClient
            TrackerClient trackerClient = new TrackerClient();
            //4.创建TrackerServer
            TrackerServer trackerServer = trackerClient.getTrackerServer();
            //5.创建StorageServer
            StorageClient1 storageClient1 = new StorageClient1();
            // 6. 文件上传
            //获取后缀名
            String extName = FilenameUtils.getExtension(file.getOriginalFilename());
            path = storageClient1.upload_appender_file1(file.getBytes(), extName, null);
           // System.out.println(path);
        }
        return R.ok(url+path).code(200).message("成功");
    }
}
