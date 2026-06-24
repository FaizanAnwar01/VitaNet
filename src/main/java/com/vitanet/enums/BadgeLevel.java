package com.vitanet.enums;

/**
 * Gamification badge levels awarded to donors based on their
 * cumulative donation count.
 *
 * <p>Badge thresholds (from the original MERN system):
 * <ul>
 *   <li>{@link #NONE} — 0 donations</li>
 *   <li>{@link #FIRST_DONATION} — ≥ 1 donation</li>
 *   <li>{@link #LIFE_SAVER} — ≥ 5 donations</li>
 *   <li>{@link #HERO_DONOR} — ≥ 10 donations</li>
 *   <li>{@link #PLATINUM_DONOR} — ≥ 20 donations</li>
 * </ul>
 */
public enum BadgeLevel {
    NONE("None"),
    FIRST_DONATION("First Donation"),
    LIFE_SAVER("Life Saver"),
    HERO_DONOR("Hero Donor"),
    PLATINUM_DONOR("Platinum Donor");

    private final String displayName;

    BadgeLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Calculates the appropriate badge for the given donation count.
     *
     * @param donationCount the total number of completed donations
     * @return the highest badge the donor qualifies for
     */
    public static BadgeLevel fromDonationCount(int donationCount) {
        if (donationCount >= 20) return PLATINUM_DONOR;
        if (donationCount >= 10) return HERO_DONOR;
        if (donationCount >= 5)  return LIFE_SAVER;
        if (donationCount >= 1)  return FIRST_DONATION;
        return NONE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
