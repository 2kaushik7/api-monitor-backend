package com.apimonitor.ambackend.service;

import com.apimonitor.ambackend.model.User;

public interface UserService {
    User registerUser(String email, String name);
}
