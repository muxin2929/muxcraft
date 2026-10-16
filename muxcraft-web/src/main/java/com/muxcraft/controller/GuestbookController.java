package com.muxcraft.controller;

import com.muxcraft.common.PageResult;
import com.muxcraft.common.Result;
import com.muxcraft.dto.ContentDTO;
import com.muxcraft.security.AuthInterceptor;
import com.muxcraft.security.LoginUser;
import com.muxcraft.service.GuestbookService;
import com.muxcraft.vo.GuestbookVO;
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
 * 留言板控制器
 * 接口：GET /guestbook、POST /guestbook、DELETE /guestbook/{id}
 */
@RestController
@RequestMapping("/guestbook")
public class GuestbookController {

    private final GuestbookService guestbookService;

    public GuestbookController(GuestbookService guestbookService) {
        this.guestbookService = guestbookService;
    }

    @GetMapping
    public Result<PageResult<GuestbookVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        LoginUser loginUser = AuthInterceptor.currentUser();
        Long currentUserId = loginUser != null ? loginUser.getId() : null;
        return Result.success(guestbookService.page(page, size, currentUserId));
    }

    @PostMapping
    public Result<GuestbookVO> create(@Valid @RequestBody ContentDTO dto) {
        LoginUser loginUser = AuthInterceptor.currentUser();
        return Result.success(guestbookService.create(loginUser.getId(), dto.getContent(), dto.getReplyToId()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        LoginUser loginUser = AuthInterceptor.currentUser();
        guestbookService.deleteByUser(id, loginUser.getId());
        return Result.success();
    }
}