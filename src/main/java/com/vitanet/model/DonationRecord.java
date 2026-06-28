package com.vitanet.model;

import com.vitanet.enums.BloodType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * An <b>immutable</b> record of a completed blood donation transaction.
 *
 * <p>This class is designed following Lecture 3 &amp; 5 principles:</p>
 * <ul>
 *   <li><b>Declared {@code final}</b> — cannot be subclassed to break invariants</li>
 *   <li><b>All fields {@code private final}</b> — set once at construction, never modified</li>
 *   <li><b>Zero setter methods</b> — no mutation path exists after construction</li>
 *   <li><b>Defensive Copying</b> — mutable date objects passed to the constructor
 *       are deep-copied so the caller cannot retroactively corrupt internal state</li>
 *   <li><b>Defensive Copying on getters</b> — mutable date objects returned from
 *       getters are also deep-copied to prevent Representation Exposure</li>
 * </ul>
 *
 * <p>Maps to the Mongoose {@code DonationHistory} schema from the original MERN system.</p>
 *
 * <h3>Class Invariant</h3>
 * <ul>
 *   <li>{@code quantity >= 1}</li>
 *   <li>{@code donorId != null && !donorId.isEmpty()}</li>
 *   <li>{@code bloodType != null}</li>
 *   <li>{@code recordId != null && !recordId.isEmpty()}</li>
 *   <li>All fields are immutable after construction (guaranteed by design)</li>
 * </ul>
 */
public final class DonationRecord {

    // ══════════════════════════════════════════════════════════
    //  ALL FIELDS ARE private final — TRUE IMMUTABILITY
    // ══════════════════════════════════════════════════════════

    private final String        recordId;
    private final String        donorId;
    private final String        receiverId;       // null if expired without allocation
    private final BloodType     bloodType;
    private final String        hospital;
    private final LocalDate     donationDate;     // mutable type → defensively copied
    private final int           quantity;
    private final String        status;           // "completed" or "expired"
    private final String        originalPostId;
    private final LocalDateTime createdAt;        // mutable type → defensively copied

    // ══════════════════════════════════════════════════════════
    //  CONSTRUCTOR — with Defensive Copying of mutable objects
    // ══════════════════════════════════════════════════════════

    /**
     * Constructs a fully immutable DonationRecord.
     *
     * <p><b>Defensive Copying:</b> The {@code donationDate} parameter is a
     * {@link LocalDate} object. Although {@code LocalDate} is itself immutable
     * in Java (unlike {@code java.util.Date}), we demonstrate the defensive
     * copy principle explicitly via {@link LocalDate#from(java.time.temporal.TemporalAccessor)}
     * to satisfy the course requirement. The {@code createdAt} timestamp is
     * captured internally and never exposed mutably.</p>
     *
     * <p><b>Pre-conditions:</b></p>
     * <ul>
     *   <li>{@code recordId != null && !recordId.isEmpty()}</li>
     *   <li>{@code donorId != null && !donorId.isEmpty()}</li>
     *   <li>{@code bloodType != null}</li>
     *   <li>{@code hospital != null && !hospital.isEmpty()}</li>
     *   <li>{@code donationDate != null}</li>
     *   <li>{@code quantity >= 1}</li>
     *   <li>{@code status != null && !status.isEmpty()}</li>
     * </ul>
     *
     * @param recordId       unique record identifier
     * @param donorId        ID of the donor
     * @param receiverId     ID of the receiver (may be null if no receiver yet)
     * @param bloodType      the blood type of the donated blood
     * @param hospital       the hospital where the donation took place
     * @param donationDate   the date of donation (defensively copied)
     * @param quantity       number of units donated (must be &ge; 1)
     * @param status         "completed" or "expired"
     * @param originalPostId the ID of the original donation post
     * @throws IllegalArgumentException if any pre-condition is violated
     */
    public DonationRecord(String recordId, String donorId, String receiverId,
                          BloodType bloodType, String hospital, LocalDate donationDate,
                          int quantity, String status, String originalPostId) {

        // ── Pre-condition enforcement ──
        if (recordId == null || recordId.isEmpty()) {
            throw new IllegalArgumentException(
                    "Contract violation [Pre-condition]: recordId must not be null or empty.");
        }
        if (donorId == null || donorId.isEmpty()) {
            throw new IllegalArgumentException(
                    "Contract violation [Pre-condition]: donorId must not be null or empty.");
        }
        if (bloodType == null) {
            throw new IllegalArgumentException(
                    "Contract violation [Pre-condition]: bloodType must not be null.");
        }
        if (hospital == null || hospital.isEmpty()) {
            throw new IllegalArgumentException(
                    "Contract violation [Pre-condition]: hospital must not be null or empty.");
        }
        if (donationDate == null) {
            throw new IllegalArgumentException(
                    "Contract violation [Pre-condition]: donationDate must not be null.");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "Contract violation [Class Invariant]: quantity must be >= 1, got: " + quantity);
        }
        if (status == null || status.isEmpty()) {
            throw new IllegalArgumentException(
                    "Contract violation [Pre-condition]: status must not be null or empty.");
        }

        // ── Assignment with DEFENSIVE COPYING of mutable date objects ──
        this.recordId       = recordId;
        this.donorId        = donorId;
        this.receiverId     = receiverId;                // String is immutable, no copy needed
        this.bloodType      = bloodType;                 // Enum is immutable, no copy needed
        this.hospital       = hospital;                  // String is immutable, no copy needed
        this.donationDate   = LocalDate.from(donationDate);   // ★ DEFENSIVE COPY
        this.quantity       = quantity;                   // primitive, no copy needed
        this.status         = status;                    // String is immutable, no copy needed
        this.originalPostId = originalPostId;             // String is immutable (or null)
        this.createdAt      = LocalDateTime.now();        // internally generated, not from caller
    }

    // ══════════════════════════════════════════════════════════
    //  GETTERS ONLY — NO SETTERS (immutability guarantee)
    //  Mutable return types use DEFENSIVE COPYING
    // ══════════════════════════════════════════════════════════

    /** @return the unique record ID */
    public String getRecordId() {
        return recordId;
    }

    /** @return the donor's user ID */
    public String getDonorId() {
        return donorId;
    }

    /** @return the receiver's user ID, or {@code null} if no receiver (e.g., expired) */
    public String getReceiverId() {
        return receiverId;
    }

    /** @return the blood type of the donation */
    public BloodType getBloodType() {
        return bloodType;
    }

    /** @return the hospital where the donation took place */
    public String getHospital() {
        return hospital;
    }

    /**
     * Returns the donation date.
     *
     * <p><b>Defensive Copy:</b> Returns a copy of the internal date to prevent
     * Representation Exposure. Although {@link LocalDate} is immutable in Java,
     * we apply the pattern explicitly as required by the SCD course.</p>
     *
     * @return a defensive copy of the donation date
     */
    public LocalDate getDonationDate() {
        return LocalDate.from(donationDate);  // ★ DEFENSIVE COPY on getter
    }

    /** @return the number of units donated */
    public int getQuantity() {
        return quantity;
    }

    /** @return the status string ("completed" or "expired") */
    public String getStatus() {
        return status;
    }

    /** @return the original donation post ID */
    public String getOriginalPostId() {
        return originalPostId;
    }

    /**
     * Returns the creation timestamp.
     *
     * <p><b>Defensive Copy:</b> Returns a copy of the internal timestamp.</p>
     *
     * @return a defensive copy of the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return LocalDateTime.from(createdAt);  // ★ DEFENSIVE COPY on getter
    }

    // ══════════════════════════════════════════════════════════
    //  PURE QUERY METHODS (no side effects)
    // ══════════════════════════════════════════════════════════

    /**
     * Generates a formatted receipt ID from the record ID.
     *
     * <p>This is a <b>pure query</b> — it computes and returns a value
     * without modifying any state (Explicit Inquiry principle).</p>
     *
     * <p>Format: {@code BDR-{last 8 chars of recordId in uppercase}}</p>
     *
     * @return the formatted receipt string
     */
    public String generateReceiptId() {
        if (recordId.length() < 8) {
            return "BDR-" + recordId.toUpperCase();
        }
        return "BDR-" + recordId.substring(recordId.length() - 8).toUpperCase();
    }

    /**
     * Checks if this record represents a successfully completed donation
     * (as opposed to an expired one).
     *
     * <p>This is a <b>pure query</b> — no mutation.</p>
     *
     * @return {@code true} if status is "completed"
     */
    public boolean isCompleted() {
        return "completed".equalsIgnoreCase(status);
    }

    /**
     * Checks if this record has an associated receiver.
     *
     * <p>This is a <b>pure query</b> — no mutation.</p>
     *
     * @return {@code true} if a receiver is linked to this donation
     */
    public boolean hasReceiver() {
        return receiverId != null && !receiverId.isEmpty();
    }

    @Override
    public String toString() {
        return String.format(
                "DonationRecord[id=%s, donor=%s, receiver=%s, type=%s, status=%s, date=%s, qty=%d]",
                recordId, donorId,
                receiverId != null ? receiverId : "N/A",
                bloodType, status, donationDate, quantity);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DonationRecord that = (DonationRecord) obj;
        return recordId.equals(that.recordId);
    }

    @Override
    public int hashCode() {
        return recordId.hashCode();
    }
}
