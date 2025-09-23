package com.kunfeng2002.be002.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
public class LoginRequest {

    @Size(min = 42, max = 42, message = "Address must have length 42")
    private String address;

    @NotBlank(message = "Message must not be null")
    private String message;

    private String signature;
}

