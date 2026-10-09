package com.muxcraft.controller;

import com.muxcraft.common.PageResult;
import com.muxcraft.common.Result;
import com.muxcraft.dto.ContentDTO;
import com.muxcraft.security.AuthInterceptor;
import com.muxcraft.security.LoginUser;
import com.muxcraft.service.CommentService;
import com.muxcraft.vo.CommentVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论控制器
 * 接口：GET /projects/{projectId}/comments、POST /projects/{projectId}/comments
 */
@RestController
@RequestMapping("/projects/{projectId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public Result<PageResult<CommentVO>> page(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 50);
        return Result.success(commentService.page(projectId, page, size));
    }

    @PostMapping
    public Result<CommentVO> create(@PathVariable Long projectId, @Valid @RequestBody ContentDTO dto) {
        LoginUser loginUser = AuthInterceptor.currentUser();
        return Result.success(commentService.create(projectId, loginUser.getId(), dto.getContent()));
    }
}