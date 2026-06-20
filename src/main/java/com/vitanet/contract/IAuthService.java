package com.vitanet.contract;

import com.vitanet.model.User;

/**
 * Service interface for authentication operations in VitaNet.
 */
public interface IAuthService {

    /**
     * Registers a new user in the system.
     *
     * <h4>PRE-CONDITIONS:</h4>
     * <ol>
     *   <li>{@code user != null}</li>
     *   <li>{@code user.getEmail()} is not already registered</li>
     *   <li>{@code user.getPassword().length() >= 6}</li>
     *   <li>{@code user.getRole() != ADMIN} — admins are seeded, not registered</li>
     * </ol>
     *
     * <h4>POST-CONDITIONS:</h4>
     * <ol>
     *   <li>User is persisted with a hashed password</li>
     *   <li>A JWT token string is returned</li>
     * </ol>
     *
     * @param user the user to register
     * @return a JWT token for the new session
     * @throws IllegalArgumentException if pre-conditions are violated
     * @throws IllegalStateException    if email is already taken
     */
    String register(User user);

    /**
     * Authenticates a user by email and password.
     *
     * <h4>PRE-CONDITIONS:</h4>
     * <ol>
     *   <li>{@code email != null && !email.isEmpty()}</li>
     *   <li>{@code password != null && !password.isEmpty()}</li>
     * </ol>
     *
     * <h4>POST-CONDITIONS:</h4>
     * <ol>
     *   <li>If credentials match, a JWT token string is returned</li>
     *   <li>If credentials do not match, an exception is thrown</li>
     * </ol>
     *
     * @param email    the user's email address
     * @param password the plaintext password
     * @return a JWT token for the authenticated session
     * @throws IllegalArgumentException if email or password is null/empty
     * @throws SecurityException        if credentials are invalid
     */
    String login(String email, String password);

    /**
     * Validates a JWT token and returns the associated user.
     *
     * @param token the JWT token to validate
     * @return the authenticated {@link User}
     * @throws SecurityException if token is invalid or expired
     */
    User validateToken(String token);
}
