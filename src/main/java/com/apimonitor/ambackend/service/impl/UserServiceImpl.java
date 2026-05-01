package com.apimonitor.ambackend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.apimonitor.ambackend.model.User;
import com.apimonitor.ambackend.repository.UserRepository;
import com.apimonitor.ambackend.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User registerUser(String email, String name) {
        if (userRepository.existsByEmail(email)) {
            log.info("User already exists: {}", email);
            return userRepository.findByEmail(email).orElseThrow();
        }

        User user = new User();
        user.setEmail(email);
        user.setName(name != null ? name : "");
        User saved = userRepository.save(user);
        log.info("User registered: {}", email);
        return saved;
    }
}
