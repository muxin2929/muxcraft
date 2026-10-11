package com.muxcraft.controller;

import com.muxcraft.common.PageResult;
import com.muxcraft.common.Result;
import com.muxcraft.dto.ChatMessageDeleteDTO;
import com.muxcraft.dto.ChatPinDTO;
import com.muxcraft.dto.ChatSendDTO;
import com.muxcraft.security.AuthInterceptor;
import com.muxcraft.security.LoginUser;
import com.muxcraft.service.ChatService;
import com.muxcraft.vo.ChatMessageVO;
import com.muxcraft.vo.ConversationVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/conversations")
    public Result<List<ConversationVO>> conversations() {
        LoginUser loginUser = AuthInterceptor.currentUser();
        return Result.success(chatService.conversations(loginUser.getId()));
    }

    @DeleteMapping("/conversations/{userId}")
    public Result<Void> deleteConversation(@PathVariable Long userId) {
        LoginUser loginUser = AuthInterceptor.currentUser();
        chatService.deleteConversation(loginUser.getId(), userId);
        return Result.success();
    }

    @PostMapping("/conversations/{userId}/pin")
    public Result<Void> pinConversation(@PathVariable Long userId, @RequestBody ChatPinDTO dto) {
        LoginUser loginUser = AuthInterceptor.currentUser();
        boolean pinned = dto.getPinned() != null && dto.getPinned();
        chatService.pinConversation(loginUser.getId(), userId, pinned);
        return Result.success();
    }

    @GetMapping("/messages/{userId}")
    public Result<PageResult<ChatMessageVO>> messages(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        size = Math.min(size, 200);
        LoginUser loginUser = AuthInterceptor.currentUser();
        return Result.success(chatService.messages(loginUser.getId(), userId, page, size));
    }

    @PostMapping("/messages")
    public Result<ChatMessageVO> send(@Valid @RequestBody ChatSendDTO dto) {
        LoginUser loginUser = AuthInterceptor.currentUser();
        return Result.success(chatService.sendMessage(loginUser.getId(), dto.getToUserId(), dto.getContent()));
    }

    @DeleteMapping("/messages")
    public Result<Void> deleteMessages(@Valid @RequestBody ChatMessageDeleteDTO dto) {
        LoginUser loginUser = AuthInterceptor.currentUser();
        chatService.deleteMessages(loginUser.getId(), dto.getIds());
        return Result.success();
    }

    @PostMapping("/block/{userId}")
    public Result<Map<String, Boolean>> block(@PathVariable Long userId) {
        LoginUser loginUser = AuthInterceptor.currentUser();
        boolean blocked = chatService.blockUser(loginUser.getId(), userId);
        return Result.success(Map.of("blocked", blocked));
    }

    @DeleteMapping("/block/{userId}")
    public Result<Void> unblock(@PathVariable Long userId) {
        LoginUser loginUser = AuthInterceptor.currentUser();
        chatService.unblockUser(loginUser.getId(), userId);
        return Result.success();
    }

    @GetMapping("/blocks")
    public Result<List<Long>> blocks() {
        LoginUser loginUser = AuthInterceptor.currentUser();
        return Result.success(chatService.getBlockedIds(loginUser.getId()));
    }
}