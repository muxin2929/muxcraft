package com.muxcraft.controller;

import com.muxcraft.common.Result;
import com.muxcraft.security.AuthInterceptor;
import com.muxcraft.security.LoginUser;
import com.muxcraft.service.FileService;
import com.muxcraft.vo.UploadVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 * 接口：POST /files/avatar、POST /files/upload
 */
@RestController
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/avatar")
    public Result<UploadVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        LoginUser loginUser = AuthInterceptor.currentUser();
        return Result.success("upload success", fileService.uploadAvatar(loginUser.getId(), file));
    }

    @PostMapping("/upload")
    public Result<UploadVO> upload(@RequestParam("file") MultipartFile file,
                                   @RequestParam(defaultValue = "common") String category) {
        return Result.success("upload success", fileService.upload(file, category));
    }
}