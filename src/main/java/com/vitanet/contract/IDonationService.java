package com.vitanet.contract;

import com.vitanet.model.BloodPacket;
import com.vitanet.model.DonationRecord;
import com.vitanet.model.Donor;

/**
 * Service interface for blood donation operations in VitaNet.
 *
 * <p>All implementations MUST enforce the Design by Contract (DbC)
 * specifications documented on each method.</p>
 *
 * @see com.vitanet.service.DonationServiceImpl
 */
public interface IDonationService {

    /**
     * Processes a blood donation from a donor.
     *
     * <hr>
     * <h3>══════════════ DESIGN BY CONTRACT ══════════════</h3>
     *
     * <h4>PRE-CONDITIONS:</h4>
     * <ol>
     *   <li>{@code donor != null}
     *       — a valid Donor object must be provided</li>
     *   <li>{@code bloodGroup != null && !bloodGroup.isEmpty()}
     *       — blood group label must be specified</li>
     *   <li>{@code donor.isEligible() == true}
     *       — the donor must have waited at least 90 days since
     *       their last donation (or be a first-time donor).
     *       Formally: {@code daysSinceLastDonation >= 90}</li>
     * </ol>
     *
     * <h4>POST-CONDITIONS:</h4>
     * <ol>
     *   <li>{@code Inventory.size == old(Inventory.size) + 1}
     *       — exactly one new packet is added to the inventory</li>
     *   <li>A new {@link DonationRecord} is generated and returned</li>
     *   <li>{@code donor.getDonationCount() == old(donor.getDonationCount()) + 1}
     *       — the donor's count is incremented</li>
     *   <li>{@code donor.getLastDonation() == today}
     *       — the donor's last donation is updated</li>
     *   <li>{@code donor.getBadge()} is recalculated</li>
     * </ol>
     *
     * <h4>INVARIANT (Inventory):</h4>
     * <p>{@code Inventory.totalBloodPackets >= 0} — preserved after call</p>
     *
     * @param donor      the donor performing the donation
     * @param bloodGroup the blood type label (e.g. "O+", "A-")
     * @return the generated {@link DonationRecord}
     * @throws IllegalArgumentException if any pre-condition is violated
     * @throws IllegalStateException    if the donor is not eligible (90-day rule)
     */
    DonationRecord donateBlood(Donor donor, String bloodGroup);

    /**
     * Allocates a blood packet from inventory to a receiver.
     *
     * <hr>
     * <h3>══════════════ DESIGN BY CONTRACT ══════════════</h3>
     *
     * <h4>PRE-CONDITIONS:</h4>
     * <ol>
     *   <li>{@code receiver != null}
     *       — a valid Receiver object must be provided</li>
     *   <li>{@code bloodGroup != null && !bloodGroup.isEmpty()}
     *       — blood group label must be specified</li>
     *   <li>{@code Inventory.getAvailableCount(bloodGroup) > 0}
     *       — at least one packet of the requested type must exist</li>
     *   <li>{@code packet.collectionDate + 42 days >= currentDate}
     *       — the allocated packet must NOT be expired</li>
     * </ol>
     *
     * <h4>POST-CONDITIONS:</h4>
     * <ol>
     *   <li>{@code Inventory.size == old(Inventory.size) - 1}
     *       — exactly one packet is removed from inventory</li>
     *   <li>The removed packet's status is set to ALLOCATED</li>
     *   <li>A new {@link DonationRecord} is created linking donor → receiver</li>
     * </ol>
     *
     * <h4>INVARIANT (Inventory):</h4>
     * <p>{@code Inventory.totalBloodPackets >= 0} — preserved after call</p>
     *
     * @param receiver   the receiver requesting blood
     * @param bloodGroup the blood type label (e.g. "O+", "A-")
     * @return the generated {@link DonationRecord}
     * @throws IllegalArgumentException if receiver or bloodGroup is null
     * @throws IllegalStateException    if no matching blood is available
     * @throws IllegalStateException    if the only available packets are expired
     */
    DonationRecord allocateBlood(com.vitanet.model.Receiver receiver, String bloodGroup);
}
