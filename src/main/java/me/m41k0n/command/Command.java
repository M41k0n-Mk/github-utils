package me.m41k0n.command;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface Command<T> {
    T execute() throws JsonProcessingException;
}