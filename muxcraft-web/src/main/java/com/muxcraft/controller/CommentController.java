package com.muxcraft.controller;

import com.muxcraft.common.PageResult;
import com.muxcraft.common.Result;
import com.muxcraft.dto.ContentDTO;
import com.muxcraft.security.AuthInterceptor;
import com.muxcraft.security.LoginUser;
import com.muxcraft.service.CommentService;
import com.muxcraft.vo.CommentVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论控制器
 */
@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/projects/{projectId}/comments")
    public Result<PageResult<CommentVO>> page(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        LoginUser loginUser = AuthInterceptor.currentUser();
        Long currentUserId = loginUser != null ? loginUser.getId() : null;
        return Result.success(commentService.page(projectId, page, size, currentUserId));
    }

    @PostMapping("/projects/{projectId}/comments")
    public Result<CommentVO> create(@PathVariable Long projectId, @Valid @RequestBody ContentDTO dto) {
        LoginUser loginUser = AuthInterceptor.currentUser();
        return Result.success(commentService.create(projectId, loginUser.getId(), dto.getContent(), dto.getReplyToId()));
    }

    @DeleteMapping("/comments/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        LoginUser loginUser = AuthInterceptor.currentUser();
        commentService.deleteByUser(id, loginUser.getId());
        return Result.success();
    }
}