package com.vitanet.data;

import com.vitanet.enums.BloodType;
import com.vitanet.enums.UserRole;
import com.vitanet.model.Admin;
import com.vitanet.model.Donor;
import com.vitanet.model.Receiver;
import com.vitanet.model.User;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe, singleton in-memory user store for VitaNet.
 *
 * <p>Replaces a database with a {@link ConcurrentHashMap} keyed by email.
 * Pre-seeds a default System Administrator account on first access.</p>
 *
 * <h3>Thread Safety</h3>
 * <ul>
 *   <li>Uses {@link ConcurrentHashMap} for lock-free concurrent reads</li>
 *   <li>Singleton created via static holder idiom (lazy, thread-safe)</li>
 * </ul>
 */
public final class UserStore {

    // ── Singleton Holder (Bill Pugh idiom — lazy & thread-safe) ──
    private static final class Holder {
        static final UserStore INSTANCE = new UserStore();
    }

    /**
     * Returns the singleton UserStore instance.
     *
     * @return the shared UserStore
     */
    public static UserStore getInstance() {
        return Holder.INSTANCE;
    }

    // ── Internal Storage ──────────────────────────────────────
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    // ── Private Constructor (seeds admin) ─────────────────────
    private UserStore() {
        seedAdmin();
    }

    /**
     * Pre-seeds the default System Administrator account.
     */
    private void seedAdmin() {
        Admin admin = new Admin(
                "ADM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                "admin@vitanet.com",
                "Admin123",
                "System Admin",
                BloodType.O_POSITIVE,
                "+92-300-0000000"
        );
        users.put(admin.getEmail().toLowerCase(), admin);
    }

    // ══════════════════════════════════════════════════════════
    //  PUBLIC API
    // ══════════════════════════════════════════════════════════

    /**
     * Registers a new user in the store.
     *
     * @param user the user to register (must not be null, email must be unique)
     * @throws IllegalArgumentException if user is null or email is already taken
     */
    public void register(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null.");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("User email must not be null or empty.");
        }

        String key = user.getEmail().toLowerCase().trim();
        User existing = users.putIfAbsent(key, user);
        if (existing != null) {
            throw new IllegalArgumentException(
                    "Email already registered: " + user.getEmail());
        }
    }

    /**
     * Authenticates a user by email and password.
     *
     * @param email    the user's email
     * @param password the user's password
     * @return the authenticated {@link User}
     * @throws SecurityException if credentials are invalid
     */
    public User authenticate(String email, String password) {
        if (email == null || password == null) {
            throw new SecurityException("Email and password must not be null.");
        }

        String key = email.toLowerCase().trim();
        User user = users.get(key);

        if (user == null) {
            throw new SecurityException("No account found with email: " + email);
        }
        if (!user.getPassword().equals(password)) {
            throw new SecurityException("Invalid password for: " + email);
        }

        return user;
    }

    /**
     * Returns an unmodifiable view of all registered users.
     *
     * @return unmodifiable collection of users
     */
    public Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    /**
     * Returns all users that match the given role.
     *
     * @param role the role to filter by
     * @return collection of users with the specified role
     */
    public Collection<User> getUsersByRole(UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null.");
        }
        return users.values().stream()
                .filter(u -> u.getRole() == role)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Removes a user by email.
     *
     * @param email the email of the user to remove
     * @return the removed user, or null if not found
     */
    public User removeUser(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email must not be null.");
        }
        return users.remove(email.toLowerCase().trim());
    }

    /**
     * Returns the total number of registered users.
     *
     * @return user count
     */
    public int getUserCount() {
        return users.size();
    }

    /**
     * Looks up a user by email.
     *
     * @param email the email to search for
     * @return the user, or null if not found
     */
    public User getUserByEmail(String email) {
        if (email == null) {
            return null;
        }
        return users.get(email.toLowerCase().trim());
    }
}
