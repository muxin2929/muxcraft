package com.muxcraft.service;

import com.muxcraft.vo.UploadVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务
 */
public interface FileService {

    UploadVO uploadAvatar(Long userId, MultipartFile file);

    UploadVO uploadAdmin(MultipartFile file, String type);

    UploadVO upload(MultipartFile file, String category);
}