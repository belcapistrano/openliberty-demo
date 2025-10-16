package com.example.demo;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService();
    }

    @Test
    public void testGetAllUsers() {
        List<User> users = userService.getAllUsers();
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertEquals(3, users.size());
    }

    @Test
    public void testCreateUser() {
        User newUser = new User(null, "test_user", "test@example.com", "Test User");
        User createdUser = userService.createUser(newUser);

        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("test_user", createdUser.getUsername());
        assertEquals("test@example.com", createdUser.getEmail());
    }

    @Test
    public void testGetUserById() {
        User newUser = new User(null, "find_me", "findme@example.com", "Find Me");
        User createdUser = userService.createUser(newUser);

        Optional<User> foundUser = userService.getUserById(createdUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals(createdUser.getId(), foundUser.get().getId());
        assertEquals("find_me", foundUser.get().getUsername());
    }

    @Test
    public void testUpdateUser() {
        User newUser = new User(null, "update_me", "update@example.com", "Update Me");
        User createdUser = userService.createUser(newUser);

        User updatedUser = new User(null, "updated", "updated@example.com", "Updated User");
        Optional<User> result = userService.updateUser(createdUser.getId(), updatedUser);

        assertTrue(result.isPresent());
        assertEquals("updated", result.get().getUsername());
        assertEquals("updated@example.com", result.get().getEmail());
    }

    @Test
    public void testDeleteUser() {
        User newUser = new User(null, "delete_me", "delete@example.com", "Delete Me");
        User createdUser = userService.createUser(newUser);

        boolean deleted = userService.deleteUser(createdUser.getId());
        assertTrue(deleted);

        Optional<User> findDeleted = userService.getUserById(createdUser.getId());
        assertFalse(findDeleted.isPresent());
    }

    @Test
    public void testFindByUsername() {
        Optional<User> user = userService.findByUsername("john_doe");
        assertTrue(user.isPresent());
        assertEquals("john_doe", user.get().getUsername());

        Optional<User> notFound = userService.findByUsername("non_existent");
        assertFalse(notFound.isPresent());
    }
}