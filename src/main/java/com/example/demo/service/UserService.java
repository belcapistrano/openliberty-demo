package com.example.demo.service;

import com.example.demo.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class UserService {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public UserService() {
        // Initialize with sample data
        createUser(new User(null, "john_doe", "john@example.com", "John Doe"));
        createUser(new User(null, "jane_smith", "jane@example.com", "Jane Smith"));
        createUser(new User(null, "bob_wilson", "bob@example.com", "Bob Wilson"));
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public User createUser(User user) {
        Long id = idCounter.incrementAndGet();
        user.setId(id);
        users.put(id, user);
        return user;
    }

    public Optional<User> updateUser(Long id, User updatedUser) {
        if (users.containsKey(id)) {
            updatedUser.setId(id);
            users.put(id, updatedUser);
            return Optional.of(updatedUser);
        }
        return Optional.empty();
    }

    public boolean deleteUser(Long id) {
        return users.remove(id) != null;
    }

    public Optional<User> findByUsername(String username) {
        return users.values().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }
}