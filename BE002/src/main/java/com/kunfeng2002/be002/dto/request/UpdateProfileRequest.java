package com.kunfeng2002.be002.dto.request;

import lombok.Getter;

@Getter
public class UpdateProfileRequest {
    private String address;
    private String username;
    private String email;
    private String avatarUrl;
    private String bio;
}
