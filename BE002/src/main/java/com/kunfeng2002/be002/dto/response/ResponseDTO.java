package com.kunfeng2002.be002.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseDTO {

    private int status;

    private String message;

    private Object data;
}