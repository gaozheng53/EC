package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file, String path) {
        String fileName = file.getOriginalFilename();
        // 得到扩展名  i.e: jpg png
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1);
        // 解决万一上传的文件名相同的问题，粗暴的方法就是加random字符串
        String uploadFilename = UUID.randomUUID().toString()+"."+fileExtensionName;
        logger.info("开始上传文件，上传文件名为：{}，上传的路径：{}， 新文件名：{}",fileName,path,uploadFilename);

        File fileDir = new File(path);
        if (!fileDir.exists()){  //文件夹不存在
            fileDir.setWritable(true); //设置可写权限
            fileDir.mkdirs();
        }

        File targetFile = new File(path,uploadFilename);

        try {
            file.transferTo(targetFile);
            // 文件上传成功
            // 把文件上传到FTP服务器上
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            // 把本地upload下面的文件删除
            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件异常",e);
            return null;
        }
        return targetFile.getName();
    }
}
