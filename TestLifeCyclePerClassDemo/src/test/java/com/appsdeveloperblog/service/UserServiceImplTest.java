package com.appsdeveloperblog.service;

import com.appsdeveloperblog.io.UsersDatabase;
import com.appsdeveloperblog.io.UsersDatabaseMapImpl;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceImplTest {
    private UsersDatabase usersDatabase;
    private UserService userService;
    private String createdUserId = "";

    @BeforeAll
    void setup() {
        // Create & initialize database
        usersDatabase = new UsersDatabaseMapImpl();
        usersDatabase.init();
        userService = new UserServiceImpl(usersDatabase);
    }

    @AfterAll
    void cleanup() {
        // Close connection
        // Delete database
        usersDatabase.close();
    }

    @Test
    @Order(1)
    @DisplayName("Create User works")
    void testCreateUser_whenProvidedWithValidDetails_returnsUserId() {
        Map<String, String> user = new HashMap<>();
        user.put("firstName","John");
        user.put("lastName","Doe");

        createdUserId = userService.createUser(user);
        assertNotNull(createdUserId, "User id should not be null");
    }


    @Test
    @Order(2)
    @DisplayName("Update user works")
    void testUpdateUser_whenProvidedWithValidDetails_returnsUpdatedUserDetails() {
        Map<String, String> newUser = new HashMap<>();
        newUser.put("firstName","Mike");
        newUser.put("lastName","Doe");

        Map updateUser = userService.updateUser(createdUserId, newUser);

        assertEquals(newUser.get("firstName"), updateUser.get("firstName"),
                "Return value of user's firstName is incorrect");

        assertEquals(newUser.get("lastName"), updateUser.get("lastName"),
                "Return value of user's lastName is incorrect");
    }

    @Test
    @Order(3)
    @DisplayName("Find user works")
    void testGetUserDetails_whenProvidedWithValidUserId_returnsUserDetails() {
        Map userDetails = userService.getUserDetails(createdUserId);
        assertNotNull(userDetails);
        assertEquals(createdUserId, userDetails.get("userId"),
                "Returned User details contains incorrect user id ");
    }

    @Test
    @Order(4)
    @DisplayName("Delete user works")
    void testDeleteUser_whenProvidedWithValidUserId_returnsUserDetails() {
        userService.deleteUser(createdUserId);
        assertNull(userService.getUserDetails(createdUserId), "User not found");
    }

}