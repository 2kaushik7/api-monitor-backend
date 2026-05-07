package com.apimonitor.ambackend.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.apimonitor.ambackend.model.User;
import com.apimonitor.ambackend.repository.UserRepository;
import com.apimonitor.ambackend.service.UserService;

import java.util.Optional;

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

    @Override
    public User upgradeToPro(String email, String stripeCustomerId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        user.setPlan("PRO");
        user.setStripeCustomerId(stripeCustomerId);
        User saved = userRepository.save(user);
        log.info("User upgraded to PRO: {}", email);
        return saved;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
