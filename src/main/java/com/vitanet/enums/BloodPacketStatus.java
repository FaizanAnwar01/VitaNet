package com.vitanet.enums;

/**
 * Lifecycle states of a {@link com.vitanet.model.BloodPacket}.
 *
 * <p>State transitions (see Statechart Diagram):
 * <pre>
 *   COLLECTED → SCREENED → AVAILABLE → ALLOCATED → COMPLETED
 *                 ↘ REJECTED                ↘ EXPIRED
 * </pre>
 */
public enum BloodPacketStatus {
    COLLECTED,
    SCREENED,
    AVAILABLE,
    ALLOCATED,
    EXPIRED,
    REJECTED,
    COMPLETED
}
