package com.muxcraft.controller;

import com.muxcraft.common.JsonUtils;
import com.muxcraft.common.Result;
import com.muxcraft.dto.PasswordUpdateDTO;
import com.muxcraft.dto.ProfileUpdateDTO;
import com.muxcraft.security.AuthInterceptor;
import com.muxcraft.security.LoginUser;
import com.muxcraft.service.UserService;
import com.muxcraft.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户控制器
 * 接口：PUT /user/profile、PUT /user/password
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<UserVO> updateProfile(@RequestParam("data") String dataJson,
                                        @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        ProfileUpdateDTO dto = JsonUtils.parse(dataJson, ProfileUpdateDTO.class);
        LoginUser loginUser = AuthInterceptor.currentUser();
        return Result.success(userService.updateProfile(loginUser.getId(), dto, avatar));
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto) {
        LoginUser loginUser = AuthInterceptor.currentUser();
        userService.updatePassword(loginUser.getId(), dto);
        return Result.success();
    }
}