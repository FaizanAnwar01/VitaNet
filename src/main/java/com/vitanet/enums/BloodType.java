package com.vitanet.enums;

/**
 * Enumeration of all supported ABO/Rh blood types in VitaNet.
 * Matches the original MERN system's blood type values.
 */
public enum BloodType {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-");

    private final String label;

    BloodType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Parses a display string (e.g. "O+") into the corresponding enum constant.
     *
     * @param text the display label such as "A+", "O-", etc.
     * @return the matching {@link BloodType}
     * @throws IllegalArgumentException if no match is found
     */
    public static BloodType fromLabel(String text) {
        for (BloodType bt : values()) {
            if (bt.label.equalsIgnoreCase(text)) {
                return bt;
            }
        }
        throw new IllegalArgumentException("Unknown blood type: " + text);
    }

    @Override
    public String toString() {
        return label;
    }
}
