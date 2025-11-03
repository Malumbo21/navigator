package com.eden.navigatordemo.utils;

import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private static Map<String, User> users = new HashMap<>();
    private static User currentUser = null;

    static {
        // Initialize dummy users
        users.put("user", new User("user", "password", "User Name", false));
        users.put("admin", new User("admin", "adminpass", "Admin Name", true));
    }

    /**
     * Authenticates a user with the provided username and password.
     * @param username The username.
     * @param password The password.
     * @return True if authentication is successful; false otherwise.
     */
    public static boolean authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && user.password().equals(password)) {
            currentUser = user;
            return true;
        }
        return false;
    }

    /**
     * Checks if a user is currently authenticated.
     * @return True if a user is authenticated; false otherwise.
     */
    public static boolean isAuthenticated() {
        return currentUser != null;
    }

    /**
     * Checks if the current user has admin privileges.
     * @return True if the user is an admin; false otherwise.
     */
    public static boolean isAdmin() {
        return isAuthenticated() && currentUser.isAdmin();
    }

    /**
     * Logs out the current user.
     */
    public static void logout() {
        currentUser = null;
    }

    /**
     * Gets the currently authenticated user.
     * @return The current user.
     */
    public static User getCurrentUser() {
        return currentUser;
    }
}
