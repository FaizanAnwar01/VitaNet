package com.vitanet.model;

import com.vitanet.enums.BloodPacketStatus;
import com.vitanet.enums.BloodType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Represents a single unit of donated blood in the VitaNet system.
 * This is the central entity whose lifecycle is modeled in the Statechart Diagram.
 *
 * <p>Maps to the Mongoose {@code DonationPost} schema from the original MERN system,
 * enhanced with the screening/testing states required for the SCD course.</p>
 *
 * <h3>Class Invariant</h3>
 * <ul>
 *   <li>{@code quantity >= 1} — a packet must represent at least one unit</li>
 *   <li>{@code expiryDate == collectionDate + SHELF_LIFE_DAYS}</li>
 *   <li>{@code status} must follow the valid state transitions defined in the
 *       Statechart Diagram (see {@link BloodPacketStatus})</li>
 * </ul>
 */
public class BloodPacket {

    /** Maximum shelf life of a blood packet in days before it expires. */
    public static final int SHELF_LIFE_DAYS = 42;

    private String            packetId;
    private String            donorId;
    private BloodType         bloodType;
    private String            hospital;
    private LocalDate         collectionDate;
    private LocalDate         expiryDate;
    private int               quantity;
    private BloodPacketStatus status;
    private LocalDateTime     createdAt;

    // ── Constructors ──────────────────────────────────────────

    public BloodPacket() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Creates a new BloodPacket. Expiry date is calculated automatically.
     *
     * @param packetId       unique identifier
     * @param donorId        ID of the donor who provided this blood
     * @param bloodType      the ABO/Rh blood group
     * @param hospital       name of the collection hospital
     * @param collectionDate date the blood was collected
     * @param quantity       number of units (must be &ge; 1)
     * @throws IllegalArgumentException if quantity &lt; 1 or collectionDate is null
     */
    public BloodPacket(String packetId, String donorId, BloodType bloodType,
                       String hospital, LocalDate collectionDate, int quantity) {
        if (collectionDate == null) {
            throw new IllegalArgumentException(
                    "Contract violation [Pre-condition]: collectionDate must not be null.");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "Contract violation [Class Invariant]: quantity must be >= 1, got: " + quantity);
        }
        this.packetId       = packetId;
        this.donorId        = donorId;
        this.bloodType      = bloodType;
        this.hospital       = hospital;
        this.collectionDate = collectionDate;
        this.expiryDate     = collectionDate.plusDays(SHELF_LIFE_DAYS);
        this.quantity        = quantity;
        this.status          = BloodPacketStatus.COLLECTED;
        this.createdAt       = LocalDateTime.now();
    }

    // ── Getters ───────────────────────────────────────────────

    public String            getPacketId()       { return packetId; }
    public String            getDonorId()        { return donorId; }
    public BloodType         getBloodType()      { return bloodType; }
    public String            getHospital()       { return hospital; }
    public LocalDate         getCollectionDate() { return collectionDate; }
    public LocalDate         getExpiryDate()     { return expiryDate; }
    public int               getQuantity()       { return quantity; }
    public BloodPacketStatus getStatus()         { return status; }
    public LocalDateTime     getCreatedAt()      { return createdAt; }

    // ── Setters (restricted) ──────────────────────────────────

    public void setPacketId(String packetId)   { this.packetId = packetId; }
    public void setDonorId(String donorId)     { this.donorId = donorId; }
    public void setBloodType(BloodType type)   { this.bloodType = type; }
    public void setHospital(String hospital)   { this.hospital = hospital; }
    public void setQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "Contract violation [Class Invariant]: quantity must be >= 1.");
        }
        this.quantity = quantity;
    }
    public void setCollectionDate(LocalDate date) {
        this.collectionDate = date;
        this.expiryDate     = date.plusDays(SHELF_LIFE_DAYS);
    }

    // ── Domain Operations ─────────────────────────────────────

    /**
     * Returns the number of days remaining before this packet expires.
     *
     * @return days remaining (negative if already expired)
     */
    public long getDaysUntilExpiry() {
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    /**
     * Checks whether this blood packet has exceeded its shelf life.
     *
     * @return {@code true} if today &gt; expiryDate
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    /**
     * Transitions the packet to {@link BloodPacketStatus#SCREENED}.
     *
     * <p><b>Pre-condition:</b>  {@code status == COLLECTED}</p>
     * <p><b>Post-condition:</b> {@code status == SCREENED}</p>
     *
     * @throws IllegalStateException if the packet is not in COLLECTED state
     */
    public void markScreened() {
        if (this.status != BloodPacketStatus.COLLECTED) {
            throw new IllegalStateException(
                    "Contract violation [Pre-condition]: Cannot screen a packet in state: " + status);
        }
        this.status = BloodPacketStatus.SCREENED;
    }

    /**
     * Transitions the packet to {@link BloodPacketStatus#AVAILABLE} (passes screening).
     *
     * <p><b>Pre-condition:</b>  {@code status == SCREENED}</p>
     * <p><b>Post-condition:</b> {@code status == AVAILABLE}</p>
     *
     * @throws IllegalStateException if the packet is not in SCREENED state
     */
    public void markAvailable() {
        if (this.status != BloodPacketStatus.SCREENED) {
            throw new IllegalStateException(
                    "Contract violation [Pre-condition]: Cannot mark available from state: " + status);
        }
        this.status = BloodPacketStatus.AVAILABLE;
    }

    /**
     * Transitions the packet to {@link BloodPacketStatus#REJECTED} (fails screening).
     *
     * <p><b>Pre-condition:</b>  {@code status == SCREENED}</p>
     * <p><b>Post-condition:</b> {@code status == REJECTED}</p>
     *
     * @throws IllegalStateException if the packet is not in SCREENED state
     */
    public void markRejected() {
        if (this.status != BloodPacketStatus.SCREENED) {
            throw new IllegalStateException(
                    "Contract violation [Pre-condition]: Cannot reject from state: " + status);
        }
        this.status = BloodPacketStatus.REJECTED;
    }

    /**
     * Transitions the packet to {@link BloodPacketStatus#ALLOCATED}.
     *
     * <p><b>Pre-condition:</b>  {@code status == AVAILABLE && !isExpired()}</p>
     * <p><b>Post-condition:</b> {@code status == ALLOCATED}</p>
     *
     * @throws IllegalStateException    if the packet is not AVAILABLE
     * @throws IllegalStateException    if the packet has expired
     */
    public void markAllocated() {
        if (this.status != BloodPacketStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Contract violation [Pre-condition]: Cannot allocate from state: " + status);
        }
        if (isExpired()) {
            throw new IllegalStateException(
                    "Contract violation [Pre-condition]: Cannot allocate an expired packet. " +
                    "Expiry date: " + expiryDate);
        }
        this.status = BloodPacketStatus.ALLOCATED;
    }

    /**
     * Transitions the packet to {@link BloodPacketStatus#EXPIRED}.
     *
     * <p><b>Pre-condition:</b>  {@code status == AVAILABLE && isExpired()}</p>
     * <p><b>Post-condition:</b> {@code status == EXPIRED}</p>
     *
     * @throws IllegalStateException if the packet is not AVAILABLE or is not expired
     */
    public void markExpired() {
        if (this.status != BloodPacketStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Contract violation [Pre-condition]: Cannot expire from state: " + status);
        }
        this.status = BloodPacketStatus.EXPIRED;
    }

    /**
     * Transitions the packet to {@link BloodPacketStatus#COMPLETED}.
     *
     * <p><b>Pre-condition:</b>  {@code status == ALLOCATED}</p>
     * <p><b>Post-condition:</b> {@code status == COMPLETED}</p>
     *
     * @throws IllegalStateException if the packet is not in ALLOCATED state
     */
    public void markCompleted() {
        if (this.status != BloodPacketStatus.ALLOCATED) {
            throw new IllegalStateException(
                    "Contract violation [Pre-condition]: Cannot complete from state: " + status);
        }
        this.status = BloodPacketStatus.COMPLETED;
    }

    @Override
    public String toString() {
        return String.format("BloodPacket[id=%s, type=%s, status=%s, hospital=%s, expires=%s]",
                packetId, bloodType, status, hospital, expiryDate);
    }
}
