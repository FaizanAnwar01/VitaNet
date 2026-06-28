package com.vitanet.model;

import com.vitanet.enums.BadgeLevel;
import com.vitanet.enums.BloodType;
import com.vitanet.enums.UserRole;

import java.time.LocalDate;

/**
 * Represents a blood donor in the VitaNet system.
 * Extends {@link User} with donation-specific fields.
 *
 * <h3>Class Invariant</h3>
 * <ul>
 *   <li>{@code donationCount >= 0}</li>
 *   <li>{@code badge} is always consistent with {@code donationCount}
 *       (enforced by {@link #updateBadge()})</li>
 *   <li>If {@code lastDonation != null}, it must be a date in the past or today</li>
 * </ul>
 */
public class Donor extends User {

    /** Minimum number of days between two consecutive donations. */
    public static final int COOLOFF_DAYS = 90;

    private LocalDate  lastDonation;
    private int        donationCount;
    private BadgeLevel badge;

    // ── Constructors ──────────────────────────────────────────

    public Donor() {
        super();
        setRole(UserRole.DONOR);
        this.donationCount = 0;
        this.badge         = BadgeLevel.NONE;
    }

    public Donor(String userId, String email, String password,
                 String fullName, BloodType bloodType, String phone) {
        super(userId, email, password, fullName, bloodType, phone, UserRole.DONOR);
        this.donationCount = 0;
        this.badge         = BadgeLevel.NONE;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public LocalDate getLastDonation()                  { return lastDonation; }
    public void      setLastDonation(LocalDate date)    { this.lastDonation = date; }

    public int getDonationCount()                       { return donationCount; }
    public void setDonationCount(int count)             { this.donationCount = count; }

    public BadgeLevel getBadge()                        { return badge; }

    // ── Domain Operations ─────────────────────────────────────

    /**
     * Checks whether this donor is eligible to donate blood right now.
     *
     * <p><b>Business Rule:</b> A donor must wait at least {@value #COOLOFF_DAYS}
     * days after their last donation before donating again. First-time
     * donors (lastDonation == null) are always eligible.</p>
     *
     * @return {@code true} if the donor can donate today
     */
    public boolean isEligible() {
        if (lastDonation == null) {
            return true;  // first-time donor
        }
        return !LocalDate.now().isBefore(lastDonation.plusDays(COOLOFF_DAYS));
    }

    /**
     * Returns the earliest date on which this donor will next be eligible.
     *
     * @return the next eligible date, or today if already eligible
     */
    public LocalDate getNextEligibleDate() {
        if (lastDonation == null) {
            return LocalDate.now();
        }
        LocalDate eligible = lastDonation.plusDays(COOLOFF_DAYS);
        return eligible.isAfter(LocalDate.now()) ? eligible : LocalDate.now();
    }

    /**
     * Recalculates the donor's badge based on current {@code donationCount}.
     * Called automatically after every successful donation.
     */
    public void updateBadge() {
        this.badge = BadgeLevel.fromDonationCount(this.donationCount);
    }

    /**
     * Records a successful donation: updates last-donation date,
     * increments count, and recalculates badge.
     *
     * <p><b>Pre-condition:</b> {@code donationDate != null}</p>
     * <p><b>Post-condition:</b>
     *   {@code this.donationCount == old(donationCount) + 1}
     *   AND {@code this.lastDonation == donationDate}
     *   AND badge is consistent with new count.
     * </p>
     *
     * @param donationDate the date on which the donation was made
     * @throws IllegalArgumentException if donationDate is null
     */
    public void recordDonation(LocalDate donationDate) {
        if (donationDate == null) {
            throw new IllegalArgumentException(
                    "Contract violation [Pre-condition]: donationDate must not be null.");
        }
        this.lastDonation = donationDate;
        this.donationCount++;
        updateBadge();
    }

    @Override
    public String getRoleDisplayName() {
        return "Blood Donor";
    }
}
