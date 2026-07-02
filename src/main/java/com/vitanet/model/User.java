package com.vitanet.model;

import com.vitanet.enums.BloodType;
import com.vitanet.enums.UserRole;

import java.time.LocalDateTime;

/**
 * Abstract base class for all VitaNet users.
 *
 * <p>Maps to the Mongoose {@code User} schema from the original MERN system.
 * Concrete subclasses: {@link Donor}, {@link Receiver}, {@link Admin}.
 *
 * <h3>Class Invariant</h3>
 * <ul>
 *   <li>{@code userId != null && !userId.isEmpty()}</li>
 *   <li>{@code email != null && email contains "@"}</li>
 *   <li>{@code fullName != null && !fullName.isEmpty()}</li>
 *   <li>{@code role != null}</li>
 * </ul>
 */
public abstract class User {

    private String    userId;
    private String    email;
    private String    password;
    private String    fullName;
    private BloodType bloodType;
    private String    phone;
    private UserRole  role;
    private LocalDateTime createdAt;

    // ── Constructors ──────────────────────────────────────────

    protected User() {
        this.createdAt = LocalDateTime.now();
    }

    protected User(String userId, String email, String password,
                   String fullName, BloodType bloodType, String phone,
                   UserRole role) {
        this.userId    = userId;
        this.email     = email;
        this.password  = password;
        this.fullName  = fullName;
        this.bloodType = bloodType;
        this.phone     = phone;
        this.role      = role;
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────

    public String getUserId()            { return userId; }
    public void   setUserId(String id)   { this.userId = id; }

    public String getEmail()             { return email; }
    public void   setEmail(String email) { this.email = email; }

    public String getPassword()              { return password; }
    public void   setPassword(String password) { this.password = password; }

    public String getFullName()              { return fullName; }
    public void   setFullName(String name)   { this.fullName = name; }

    public BloodType getBloodType()                { return bloodType; }
    public void      setBloodType(BloodType type)  { this.bloodType = type; }

    public String getPhone()               { return phone; }
    public void   setPhone(String phone)   { this.phone = phone; }

    public UserRole getRole()              { return role; }
    protected void  setRole(UserRole role) { this.role = role; }

    public LocalDateTime getCreatedAt()    { return createdAt; }

    // ── Abstract Operations ───────────────────────────────────

    /**
     * Returns the display name for this user's role in the system.
     *
     * @return a human-readable role label
     */
    public abstract String getRoleDisplayName();

    @Override
    public String toString() {
        return String.format("%s[id=%s, name=%s, role=%s]",
                getClass().getSimpleName(), userId, fullName, role);
    }
}
