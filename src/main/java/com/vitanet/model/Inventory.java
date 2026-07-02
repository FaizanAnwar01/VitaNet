package com.vitanet.model;

import com.vitanet.enums.BloodPacketStatus;
import com.vitanet.enums.BloodType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * <b>Thread-Safe</b> blood bank inventory — the central shared resource
 * of the VitaNet system.
 *
 * <p>This class enforces all four phases of the SCD course:</p>
 * <ul>
 *   <li><b>Design by Contract (DbC)</b> — Lecture 4:
 *       Pre-/Post-conditions &amp; Class Invariant</li>
 *   <li><b>Preventing Representation Exposure</b> — Lectures 3 &amp; 5:
 *       All getters return defensive copies or unmodifiable views</li>
 *   <li><b>Explicit Inquiry (CQS)</b> — Lecture 5:
 *       Pure queries separated from mutators</li>
 *   <li><b>Thread Safety &amp; Concurrency</b> — Lectures 12 &amp; 13:
 *       All operations guarded by {@link ReentrantLock} to ensure
 *       <b>Atomicity</b>, <b>Visibility</b>, and <b>Ordering</b></li>
 * </ul>
 *
 * <hr>
 * <h3>═══════════════════════════════════════════</h3>
 * <h3>CLASS INVARIANT (CI)</h3>
 * <h3>═══════════════════════════════════════════</h3>
 * <pre>
 *   CI-1: totalBloodPackets &gt;= 0
 *   CI-2: totalBloodPackets == packets.size()
 *   CI-3: Every packet in the inventory has status == AVAILABLE
 * </pre>
 *
 * <h3>═══════════════════════════════════════════</h3>
 * <h3>THREAD SAFETY STRATEGY (Lectures 12 &amp; 13)</h3>
 * <h3>═══════════════════════════════════════════</h3>
 * <pre>
 *   Mechanism:  java.util.concurrent.locks.ReentrantLock
 *   Scope:      Every public method acquires the lock before accessing
 *               shared state (packets list + totalBloodPackets counter)
 *   Guarantee:
 *     • ATOMICITY   — check-then-act sequences are indivisible
 *     • VISIBILITY  — ReentrantLock establishes happens-before edges;
 *                     all writes by thread A are visible to thread B
 *                     after B acquires the same lock
 *     • ORDERING    — lock acquisition serializes access, preventing
 *                     interleaved reads/writes (race conditions)
 *   Pattern:    lock.lock(); try { ... } finally { lock.unlock(); }
 * </pre>
 *
 * <p><b>Why ReentrantLock over synchronized?</b> ReentrantLock provides
 * explicit lock/unlock semantics, tryLock() for non-blocking attempts,
 * and fairness policies — all useful when multiple hospital threads
 * compete for the last blood packet.</p>
 */
public class Inventory {

    // ── Shared mutable state (guarded by 'lock') ──
    private final List<BloodPacket> packets;
    private int totalBloodPackets;

    // ══════════════════════════════════════════════════════════
    //  REENTRANT LOCK — Thread Safety (Lectures 12 & 13)
    // ══════════════════════════════════════════════════════════

    /**
     * Fair {@link ReentrantLock} that serializes all access to shared state.
     *
     * <p><b>Fairness = true</b> ensures threads are served in FIFO order,
     * preventing starvation when multiple hospital threads compete.</p>
     */
    private final ReentrantLock lock = new ReentrantLock(true); // fair lock

    // ── Constructor ───────────────────────────────────────────

    /**
     * Creates a new, empty Inventory.
     *
     * <p><b>Post-condition:</b> {@code totalBloodPackets == 0 && packets.isEmpty()}</p>
     */
    public Inventory() {
        this.packets           = new ArrayList<>();
        this.totalBloodPackets = 0;
        checkInvariant();
    }

    // ══════════════════════════════════════════════════════════
    //  CLASS INVARIANT CHECK (called while lock is held)
    // ══════════════════════════════════════════════════════════

    /**
     * Verifies the class invariant. Must be called while the lock is held.
     *
     * @throws IllegalStateException if any invariant condition is violated
     */
    private void checkInvariant() {
        if (totalBloodPackets < 0) {
            throw new IllegalStateException(
                    "CONTRACT VIOLATION [Class Invariant CI-1]: " +
                    "totalBloodPackets must be >= 0, but was: " + totalBloodPackets);
        }
        if (totalBloodPackets != packets.size()) {
            throw new IllegalStateException(
                    "CONTRACT VIOLATION [Class Invariant CI-2]: " +
                    "totalBloodPackets (" + totalBloodPackets +
                    ") does not match actual packet count (" + packets.size() + ").");
        }
        for (BloodPacket p : packets) {
            if (p.getStatus() != BloodPacketStatus.AVAILABLE) {
                throw new IllegalStateException(
                        "CONTRACT VIOLATION [Class Invariant CI-3]: " +
                        "Packet " + p.getPacketId() + " has status " + p.getStatus() +
                        " but only AVAILABLE packets may reside in inventory.");
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  PURE QUERY METHODS — "Explicit Inquiry" (Lecture 5)
    //  Thread-safe: each acquires the lock for consistent reads.
    // ══════════════════════════════════════════════════════════════

    /**
     * Returns the total number of blood packets currently in inventory.
     *
     * <p><b>Thread-safe:</b> acquires lock for visibility guarantee.</p>
     * <p><b>Pure Query</b> — no mutation.</p>
     */
    public int getTotalBloodPackets() {
        lock.lock();
        try {
            return totalBloodPackets;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns an unmodifiable defensive copy of all packets.
     *
     * <p><b>Thread-safe + Defensive Copy</b> — snapshot under lock,
     * returned as unmodifiable list.</p>
     */
    public List<BloodPacket> getAllPackets() {
        lock.lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(packets));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Checks whether blood of the given type is available.
     *
     * <p><b>Explicit Inquiry</b> — pure query counterpart to
     * {@link #reserveBlood(BloodType)}. Call this to <em>ask</em>
     * before calling reserveBlood to <em>act</em> (CQS).</p>
     *
     * <p><b>Thread-safe:</b> acquires lock.</p>
     *
     * @param bloodType the blood type to check
     * @return {@code true} if at least one non-expired packet exists
     */
    public boolean checkAvailability(BloodType bloodType) {
        if (bloodType == null) {
            throw new IllegalArgumentException(
                    "Contract violation [Pre-condition]: bloodType must not be null.");
        }
        lock.lock();
        try {
            return packets.stream()
                    .anyMatch(p -> p.getBloodType() == bloodType && !p.isExpired());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Overloaded: checks availability by String label.
     *
     * <p><b>Thread-safe + Pure Query.</b></p>
     */
    public boolean checkAvailability(String bloodGroup) {
        if (bloodGroup == null || bloodGroup.isEmpty()) {
            throw new IllegalArgumentException(
                    "Contract violation [Pre-condition]: bloodGroup must not be null or empty.");
        }
        return checkAvailability(BloodType.fromLabel(bloodGroup));
    }

    /**
     * Returns the count of available (non-expired) packets of the given type.
     *
     * <p><b>Thread-safe + Pure Query.</b></p>
     */
    public int getAvailableCount(BloodType bloodType) {
        if (bloodType == null) {
            throw new IllegalArgumentException(
                    "Contract violation [Pre-condition]: bloodType must not be null.");
        }
        lock.lock();
        try {
            return (int) packets.stream()
                    .filter(p -> p.getBloodType() == bloodType)
                    .filter(p -> !p.isExpired())
                    .count();
        } finally {
            lock.unlock();
        }
    }

    /** Overloaded: count by String label. Thread-safe. */
    public int getAvailableCount(String bloodGroup) {
        if (bloodGroup == null || bloodGroup.isEmpty()) {
            throw new IllegalArgumentException(
                    "Contract violation [Pre-condition]: bloodGroup must not be null or empty.");
        }
        return getAvailableCount(BloodType.fromLabel(bloodGroup));
    }

    /**
     * Returns an unmodifiable snapshot of available packets.
     *
     * <p><b>Thread-safe + Defensive Copy.</b></p>
     */
    public List<BloodPacket> getAvailablePackets(BloodType bloodType) {
        if (bloodType == null) {
            throw new IllegalArgumentException(
                    "Contract violation [Pre-condition]: bloodType must not be null.");
        }
        lock.lock();
        try {
            List<BloodPacket> result = packets.stream()
                    .filter(p -> p.getBloodType() == bloodType)
                    .filter(p -> !p.isExpired())
                    .collect(Collectors.toList());
            return Collections.unmodifiableList(result);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns an unmodifiable snapshot of blood type statistics.
     *
     * <p><b>Thread-safe + Defensive Copy.</b></p>
     */
    public Map<BloodType, Integer> getBloodTypeStats() {
        lock.lock();
        try {
            Map<BloodType, Integer> stats = new HashMap<>();
            for (BloodType bt : BloodType.values()) {
                long count = packets.stream()
                        .filter(p -> p.getBloodType() == bt && !p.isExpired())
                        .count();
                if (count > 0) {
                    stats.put(bt, (int) count);
                }
            }
            return Collections.unmodifiableMap(stats);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Checks if the inventory is empty.
     *
     * <p><b>Thread-safe + Pure Query.</b></p>
     */
    public boolean isEmpty() {
        lock.lock();
        try {
            return totalBloodPackets == 0;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the name of the thread currently holding the lock, if any.
     * Useful for debugging concurrency scenarios.
     *
     * @return thread name or "none" if unlocked
     */
    public String getLockHolder() {
        return lock.isLocked() ? "locked (queued: " + lock.getQueueLength() + ")" : "unlocked";
    }

    // ══════════════════════════════════════════════════════════════
    //  MUTATOR METHODS — "Command" operations
    //  Thread-safe: each acquires the lock for ATOMICITY.
    //  The entire check-then-act sequence is indivisible.
    // ══════════════════════════════════════════════════════════════

    /**
     * Adds a blood packet to the inventory.
     *
     * <p><b>Thread-safe:</b> The entire pre-check → add → post-check → invariant
     * sequence executes atomically under the lock.</p>
     *
     * <h4>Design by Contract</h4>
     * <p><b>Pre-condition:</b> packet != null, status == AVAILABLE, !expired</p>
     * <p><b>Post-condition:</b> totalBloodPackets == old + 1</p>
     */
    public void addPacket(BloodPacket packet) {
        if (packet == null) {
            throw new IllegalArgumentException(
                    "CONTRACT VIOLATION [Pre-condition]: packet must not be null.");
        }
        if (packet.getStatus() != BloodPacketStatus.AVAILABLE) {
            throw new IllegalArgumentException(
                    "CONTRACT VIOLATION [Pre-condition]: Only AVAILABLE packets can be " +
                    "added. Packet " + packet.getPacketId() + " has status: " + packet.getStatus());
        }
        if (packet.isExpired()) {
            throw new IllegalArgumentException(
                    "CONTRACT VIOLATION [Pre-condition]: Cannot add expired packet " +
                    packet.getPacketId());
        }

        lock.lock();    // ★ ACQUIRE LOCK — Atomicity begins
        try {
            int oldSize = totalBloodPackets;

            packets.add(packet);
            totalBloodPackets++;

            // Post-condition
            if (totalBloodPackets != oldSize + 1) {
                throw new IllegalStateException(
                        "CONTRACT VIOLATION [Post-condition]: totalBloodPackets should be " +
                        (oldSize + 1) + " but is " + totalBloodPackets);
            }

            checkInvariant();
        } finally {
            lock.unlock();  // ★ RELEASE LOCK — guaranteed by finally
        }
    }

    /**
     * <b>THREAD-SAFE</b> reservation of a blood packet by type.
     *
     * <p><b>Concurrency Scenario (Lectures 12 &amp; 13):</b> Two hospital threads
     * call {@code reserveBlood(O_NEGATIVE)} at the exact same millisecond for the
     * last remaining O- packet. Without the lock, both could pass the availability
     * check and try to remove the same packet — a <em>race condition</em>.</p>
     *
     * <p><b>Solution:</b> The <em>entire check-then-act sequence</em> (check
     * availability → find packet → remove from list → decrement counter →
     * verify post-condition → check invariant) is enclosed in a single
     * {@code lock.lock() / unlock()} block, making it <b>atomic</b>.</p>
     *
     * <h4>Thread Safety Guarantees</h4>
     * <ul>
     *   <li><b>ATOMICITY</b> — The check + remove is indivisible; no interleaving</li>
     *   <li><b>VISIBILITY</b> — The ReentrantLock's unlock() establishes a
     *       happens-before edge to the next lock(), so the second thread sees
     *       the updated packet list and counter</li>
     *   <li><b>ORDERING</b> — The fair lock serializes threads in FIFO order,
     *       preventing starvation</li>
     * </ul>
     *
     * <h4>Design by Contract</h4>
     * <p><b>Pre-condition:</b> bloodType != null, checkAvailability(bloodType) == true</p>
     * <p><b>Post-condition:</b> totalBloodPackets == old - 1, packet removed from list</p>
     *
     * @param bloodType the blood type to reserve
     * @return the reserved packet (removed from inventory)
     * @throws IllegalArgumentException if bloodType is null
     * @throws IllegalStateException    if no available packet exists
     *         (the SECOND hospital thread will get this exception)
     */
    public BloodPacket reserveBlood(BloodType bloodType) {
        if (bloodType == null) {
            throw new IllegalArgumentException(
                    "CONTRACT VIOLATION [Pre-condition]: bloodType must not be null.");
        }

        lock.lock();    // ★ ACQUIRE LOCK — ATOMICITY starts here
        try {
            // ── ATOMIC CHECK (inside lock — cannot be interleaved) ──
            BloodPacket toReserve = null;
            for (BloodPacket p : packets) {
                if (p.getBloodType() == bloodType && !p.isExpired()) {
                    toReserve = p;
                    break;
                }
            }

            if (toReserve == null) {
                throw new IllegalStateException(
                        "CONTRACT VIOLATION [Pre-condition]: No available (non-expired) " +
                        "blood packets of type " + bloodType + " in inventory. " +
                        "Call checkAvailability() before reserveBlood(). " +
                        "[Thread: " + Thread.currentThread().getName() + "]");
            }

            // ── ATOMIC ACT (inside same lock — indivisible with check) ──
            int oldSize = totalBloodPackets;

            packets.remove(toReserve);
            totalBloodPackets--;

            // Post-condition
            if (totalBloodPackets != oldSize - 1) {
                throw new IllegalStateException(
                        "CONTRACT VIOLATION [Post-condition]: totalBloodPackets should be " +
                        (oldSize - 1) + " but is " + totalBloodPackets);
            }

            checkInvariant();

            System.out.println("    [LOCK] Thread '" + Thread.currentThread().getName() +
                    "' reserved packet " + toReserve.getPacketId() +
                    " (" + bloodType + "). Remaining: " + totalBloodPackets);

            return toReserve;

        } finally {
            lock.unlock();  // ★ RELEASE LOCK — VISIBILITY flush + ORDERING handoff
        }
    }

    /**
     * Removes a blood packet by ID.
     *
     * <p><b>Thread-safe:</b> atomic under lock.</p>
     */
    public BloodPacket removePacket(String packetId) {
        if (packetId == null) {
            throw new IllegalArgumentException(
                    "CONTRACT VIOLATION [Pre-condition]: packetId must not be null.");
        }

        lock.lock();
        try {
            if (totalBloodPackets <= 0) {
                throw new IllegalStateException(
                        "CONTRACT VIOLATION [Pre-condition]: Cannot remove from empty inventory. " +
                        "totalBloodPackets = " + totalBloodPackets);
            }

            BloodPacket toRemove = null;
            for (BloodPacket p : packets) {
                if (packetId.equals(p.getPacketId())) {
                    toRemove = p;
                    break;
                }
            }

            if (toRemove == null) {
                throw new IllegalArgumentException(
                        "CONTRACT VIOLATION [Pre-condition]: No packet found with ID: " + packetId);
            }

            int oldSize = totalBloodPackets;
            packets.remove(toRemove);
            totalBloodPackets--;

            if (totalBloodPackets != oldSize - 1) {
                throw new IllegalStateException(
                        "CONTRACT VIOLATION [Post-condition]: count mismatch after removal.");
            }

            checkInvariant();
            return toRemove;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Sweeps expired packets from inventory.
     *
     * <p><b>Thread-safe:</b> atomic under lock.</p>
     */
    public List<BloodPacket> moveExpiredToHistory() {
        lock.lock();
        try {
            int oldSize = totalBloodPackets;

            List<BloodPacket> expired = packets.stream()
                    .filter(BloodPacket::isExpired)
                    .collect(Collectors.toList());

            for (BloodPacket p : expired) {
                packets.remove(p);
                totalBloodPackets--;
                p.markExpired();
            }

            if (totalBloodPackets != oldSize - expired.size()) {
                throw new IllegalStateException(
                        "CONTRACT VIOLATION [Post-condition]: count mismatch after expiry sweep.");
            }

            checkInvariant();
            return Collections.unmodifiableList(expired);

        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        lock.lock();
        try {
            return String.format("Inventory[totalPackets=%d, types=%s]",
                    totalBloodPackets, getBloodTypeStats());
        } finally {
            lock.unlock();
        }
    }
}
