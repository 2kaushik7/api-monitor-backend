package com.apimonitor.ambackend.service;

import com.apimonitor.ambackend.model.User;
import java.util.Optional;

public interface UserService {
    User registerUser(String email, String name);
    User upgradeToPro(String email, String stripeCustomerId);
    Optional<User> findByEmail(String email);
}
