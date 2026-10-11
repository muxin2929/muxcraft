package com.muxcraft.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ChatMessageDeleteDTO {

    @NotEmpty(message = "ids 不能为空")
    private List<Long> ids;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}