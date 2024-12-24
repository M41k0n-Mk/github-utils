package me.m41k0n.actions;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface MenuAction<T> {
    T execute() throws JsonProcessingException;
}