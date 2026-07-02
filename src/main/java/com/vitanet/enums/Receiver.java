package com.vitanet.model;

import com.vitanet.enums.BloodType;
import com.vitanet.enums.UserRole;

/**
 * Represents a blood receiver (patient / hospital representative)
 * in the VitaNet system. Extends {@link User}.
 *
 * <h3>Class Invariant</h3>
 * <ul>
 *   <li>Inherits all invariants from {@link User}</li>
 *   <li>{@code role == UserRole.RECEIVER}</li>
 * </ul>
 */
public class Receiver extends User {

    // ── Constructors ──────────────────────────────────────────

    public Receiver() {
        super();
        setRole(UserRole.RECEIVER);
    }

    public Receiver(String userId, String email, String password,
                    String fullName, BloodType bloodType, String phone) {
        super(userId, email, password, fullName, bloodType, phone, UserRole.RECEIVER);
    }

    @Override
    public String getRoleDisplayName() {
        return "Blood Receiver";
    }
}
