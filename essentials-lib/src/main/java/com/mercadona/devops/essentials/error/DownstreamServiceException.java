package com.mercadona.devops.essentials.error;

import org.springframework.http.HttpStatus;

public class DownstreamServiceException extends BusinessException {

    public DownstreamServiceException(String message) {
        super(HttpStatus.BAD_GATEWAY, message);
    }
}
