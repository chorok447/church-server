package com.church.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + "을(를) 찾을 수 없습니다. (ID: " + id + ")");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
