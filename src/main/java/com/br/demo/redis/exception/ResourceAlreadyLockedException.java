package com.br.demo.redis.exception;

public class ResourceAlreadyLockedException extends Exception{

    public ResourceAlreadyLockedException() {
        super("Resource already locked");
    }
}
