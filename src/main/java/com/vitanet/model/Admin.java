package com.vitanet.model;

import com.vitanet.enums.BloodType;
import com.vitanet.enums.UserRole;

/**
 * Represents a system administrator in VitaNet.
 * Extends {@link User} with admin-specific capabilities.
 *
 * <h3>Class Invariant</h3>
 * <ul>
 *   <li>Inherits all invariants from {@link User}</li>
 *   <li>{@code role == UserRole.ADMIN}</li>
 * </ul>
 */
public class Admin extends User {

    // ── Constructors ──────────────────────────────────────────

    public Admin() {
        super();
        setRole(UserRole.ADMIN);
    }

    public Admin(String userId, String email, String password,
                 String fullName, BloodType bloodType, String phone) {
        super(userId, email, password, fullName, bloodType, phone, UserRole.ADMIN);
    }

    @Override
    public String getRoleDisplayName() {
        return "System Administrator";
    }
}
