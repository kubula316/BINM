package com.example.media.exception;

import lombok.Getter;

@Getter
public class ErrorInfo {
    private final String message;

    public ErrorInfo(String message) {
        this.message = message;
    }

}
