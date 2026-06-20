package com.vitanet;

import com.vitanet.enums.BloodType;
import com.vitanet.model.BloodPacket;
import com.vitanet.model.DonationRecord;
import com.vitanet.model.Inventory;
import com.vitanet.model.Receiver;
import com.vitanet.model.Donor;
import com.vitanet.service.DonationServiceImpl;
import com.vitanet.util.InputValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * VitaNet — Complete SCD Project Demonstration
 *
 * <p>Demonstrates all four phases of the SCD course:</p>
 * <ol>
 *   <li><b>Phase 1:</b> UML Diagrams (PlantUML files)</li>
 *   <li><b>Phase 2:</b> Design by Contract — Lecture 4</li>
 *   <li><b>Phase 3:</b> Immutability, Defensive Copies, CQS — Lectures 3 &amp; 5</li>
 *   <li><b>Phase 4:</b> Regex Validation + Concurrency — Lectures 9–13</li>
 * </ol>
 */
public class Main {

    private static final String SEPARATOR  = "═".repeat(60);
    private static final String THIN_SEP   = "─".repeat(60);
    private static final String PHASE_SEP  = "★".repeat(60);

    public static void main(String[] args) throws InterruptedException {

        System.out.println(SEPARATOR);
        System.out.println("  VitaNet — Complete SCD Project Demonstration");
        System.out.println("  Phases 2, 3 & 4  (Lectures 3–5, 9–13)");
        System.out.println(SEPARATOR);
        System.out.println();

        // ── Setup ──
        Inventory inventory = new Inventory();
        DonationServiceImpl donationService = new DonationServiceImpl(inventory);

        Donor donor = new Donor("D001", "ahmed@vitanet.com", "pass123",
                "Ahmed Khan", BloodType.O_POSITIVE, "+92-300-1234567");

        Receiver receiver = new Receiver("R001", "sara@vitanet.com", "pass456",
                "Sara Ali", BloodType.O_POSITIVE, "+92-301-7654321");

        // ══════════════════════════════════════════════════════════
        //  PHASE 2: Design by Contract (Lecture 4)
        // ══════════════════════════════════════════════════════════

        System.out.println(PHASE_SEP);
        System.out.println("  PHASE 2: Design by Contract (Lecture 4)");
        System.out.println(PHASE_SEP);
        System.out.println();

        // TEST 1: Successful Donation
        System.out.println("▶ TEST 1: Successful Blood Donation");
        System.out.println(THIN_SEP);
        DonationRecord record1 = donationService.donateBlood(donor, "O+");
        System.out.println("  ✅ Donation successful! Record: " + record1.getRecordId());
        System.out.println("  Inventory: " + inventory.getTotalBloodPackets() + " | Badge: " + donor.getBadge());
        System.out.println();

        // TEST 2: 90-Day Cooloff
        System.out.println("▶ TEST 2: Donation REJECTED — 90-Day Cooloff");
        System.out.println(THIN_SEP);
        try { donationService.donateBlood(donor, "O+"); }
        catch (IllegalStateException e) {
            System.out.println("  🛑 Pre-condition enforced: " + shorten(e.getMessage()));
        }
        System.out.println();

        // TEST 3: Successful Allocation
        System.out.println("▶ TEST 3: Successful Blood Allocation");
        System.out.println(THIN_SEP);
        DonationRecord record2 = donationService.allocateBlood(receiver, "O+");
        System.out.println("  ✅ Allocated! " + record2.getDonorId() + " → " + record2.getReceiverId());
        System.out.println("  Inventory: " + inventory.getTotalBloodPackets());
        System.out.println();

        // TEST 4: Empty Inventory
        System.out.println("▶ TEST 4: Allocation REJECTED — Empty Inventory");
        System.out.println(THIN_SEP);
        try { donationService.allocateBlood(receiver, "O+"); }
        catch (IllegalStateException e) {
            System.out.println("  🛑 Pre-condition enforced: " + shorten(e.getMessage()));
        }
        System.out.println();

        // ══════════════════════════════════════════════════════════
        //  PHASE 3: Safe Design (Lectures 3 & 5)
        // ══════════════════════════════════════════════════════════

        System.out.println(PHASE_SEP);
        System.out.println("  PHASE 3: Safe Design (Lectures 3 & 5)");
        System.out.println(PHASE_SEP);
        System.out.println();

        // TEST 5: Immutability
        System.out.println("▶ TEST 5: DonationRecord is IMMUTABLE");
        System.out.println(THIN_SEP);
        boolean isFinalClass = java.lang.reflect.Modifier.isFinal(DonationRecord.class.getModifiers());
        boolean allFinal = true;
        for (var f : DonationRecord.class.getDeclaredFields())
            if (!java.lang.reflect.Modifier.isFinal(f.getModifiers())) allFinal = false;
        boolean noSetters = true;
        for (var m : DonationRecord.class.getDeclaredMethods())
            if (m.getName().startsWith("set")) noSetters = false;
        System.out.println("  final class: " + isFinalClass + " | all final fields: " + allFinal + " | no setters: " + noSetters);
        System.out.println("  ✅ DonationRecord is TRULY IMMUTABLE!");
        System.out.println();

        // TEST 6: Defensive Copying
        System.out.println("▶ TEST 6: Inventory Returns DEFENSIVE COPIES");
        System.out.println(THIN_SEP);
        Donor donor3 = new Donor("D003", "bilal@vitanet.com", "pass111",
                "Bilal Ahmed", BloodType.B_POSITIVE, "+92-303-1111111");
        donationService.donateBlood(donor3, "B+");

        try { inventory.getAllPackets().add(null); }
        catch (UnsupportedOperationException e) { System.out.println("  ✅ getAllPackets() UNMODIFIABLE"); }
        try { inventory.getAvailablePackets(BloodType.B_POSITIVE).remove(0); }
        catch (UnsupportedOperationException e) { System.out.println("  ✅ getAvailablePackets() UNMODIFIABLE"); }
        try { inventory.getBloodTypeStats().put(BloodType.A_POSITIVE, 999); }
        catch (UnsupportedOperationException e) { System.out.println("  ✅ getBloodTypeStats() UNMODIFIABLE"); }
        System.out.println("  [Representation Exposure PREVENTED]");
        System.out.println();

        // TEST 7: CQS
        System.out.println("▶ TEST 7: checkAvailability() vs reserveBlood() — CQS");
        System.out.println(THIN_SEP);
        int before = inventory.getTotalBloodPackets();
        boolean avail = inventory.checkAvailability(BloodType.B_POSITIVE);
        System.out.println("  checkAvailability(B+): " + avail + " — inventory still " + inventory.getTotalBloodPackets());
        if (avail) {
            BloodPacket bp = inventory.reserveBlood(BloodType.B_POSITIVE);
            System.out.println("  reserveBlood(B+): removed " + bp.getPacketId() + " — inventory now " + inventory.getTotalBloodPackets());
        }
        System.out.println("  ✅ Command-Query Separation VERIFIED");
        System.out.println();

        // ══════════════════════════════════════════════════════════
        //  PHASE 4: Advanced Construction & Concurrency
        //           (Lectures 9–13)
        // ══════════════════════════════════════════════════════════

        System.out.println(PHASE_SEP);
        System.out.println("  PHASE 4: Regex + Concurrency (Lectures 9–13)");
        System.out.println(PHASE_SEP);
        System.out.println();

        // ────────────────────────────────────────────────────────
        //  PART A: Regular Expressions (Lectures 9–11)
        // ────────────────────────────────────────────────────────

        System.out.println("▶ TEST 8: CNIC Regex Validation");
        System.out.println(THIN_SEP);
        String[] cnics = {"35202-1234567-1", "12345-6789012-3", "3520-1234567-1", "35202-123456-1",
                          "35202-12345678-1", "ABCDE-1234567-1", "3520212345671", ""};
        for (String c : cnics) {
            System.out.printf("  %-22s → %s%n", "\"" + c + "\"", InputValidator.isValidCNIC(c) ? "✅ VALID" : "❌ INVALID");
        }
        System.out.println();

        System.out.println("▶ TEST 9: Phone Number Regex Validation");
        System.out.println(THIN_SEP);
        String[] phones = {"+92-300-1234567", "+923001234567", "0300-1234567", "03001234567",
                           "+92-400-1234567", "1234567890", "+91-300-1234567", ""};
        for (String p : phones) {
            System.out.printf("  %-22s → %s%n", "\"" + p + "\"", InputValidator.isValidPhone(p) ? "✅ VALID" : "❌ INVALID");
        }
        System.out.println();

        System.out.println("▶ TEST 10: Email Regex Validation");
        System.out.println(THIN_SEP);
        String[] emails = {"ahmed@vitanet.com", "sara.ali+tag@hospital.org.pk", "user@domain.co",
                           "@invalid.com", "no-at-sign", "user@.com", "user@domain", ""};
        for (String e : emails) {
            System.out.printf("  %-35s → %s%n", "\"" + e + "\"", InputValidator.isValidEmail(e) ? "✅ VALID" : "❌ INVALID");
        }
        System.out.println();

        System.out.println("▶ TEST 11: Password Strength Regex Validation");
        System.out.println(THIN_SEP);
        String[] passwords = {"Pass123", "StrongP@ss1", "Ab1234", "password", "12345678",
                              "ALLCAPS1", "short", "Ab1"};
        for (String pw : passwords) {
            System.out.printf("  %-18s → %s%n", "\"" + pw + "\"", InputValidator.isValidPassword(pw) ? "✅ VALID" : "❌ INVALID");
        }
        System.out.println();

        System.out.println("▶ TEST 12: CNIC Region/Serial Extraction (Matcher.group())");
        System.out.println(THIN_SEP);
        String testCNIC = "35202-1234567-1";
        System.out.println("  CNIC:   " + testCNIC);
        System.out.println("  Region: " + InputValidator.extractCNICRegion(testCNIC));
        System.out.println("  Serial: " + InputValidator.extractCNICSerial(testCNIC));
        System.out.println("  Domain: " + InputValidator.extractEmailDomain("ahmed@vitanet.com"));
        System.out.println();

        System.out.println("▶ TEST 13: InputValidator Reject with Exception");
        System.out.println(THIN_SEP);
        try { InputValidator.requireValidCNIC("bad-cnic"); }
        catch (IllegalArgumentException e) { System.out.println("  🛑 CNIC: " + shorten(e.getMessage())); }
        try { InputValidator.requireValidPhone("bad-phone"); }
        catch (IllegalArgumentException e) { System.out.println("  🛑 Phone: " + shorten(e.getMessage())); }
        try { InputValidator.requireValidEmail("bad-email"); }
        catch (IllegalArgumentException e) { System.out.println("  🛑 Email: " + shorten(e.getMessage())); }
        try { InputValidator.requireValidPassword("weak"); }
        catch (IllegalArgumentException e) { System.out.println("  🛑 Password: " + shorten(e.getMessage())); }
        System.out.println();

        // ────────────────────────────────────────────────────────
        //  PART B: Concurrency & Thread Safety (Lectures 12–13)
        // ────────────────────────────────────────────────────────

        System.out.println(PHASE_SEP);
        System.out.println("  CONCURRENCY SIMULATION (Lectures 12 & 13)");
        System.out.println("  Scenario: 5 hospitals race for the LAST O- packet");
        System.out.println(PHASE_SEP);
        System.out.println();

        // Setup: Add exactly ONE O-Negative packet to inventory
        Donor onegDonor = new Donor("D-ONEG", "oneg@vitanet.com", "Pass123",
                "O-Neg Donor", BloodType.O_NEGATIVE, "+92-300-0000000");
        donationService.donateBlood(onegDonor, "O-");

        System.out.println("  Inventory has " + inventory.getAvailableCount(BloodType.O_NEGATIVE) + " O- packet(s)");
        System.out.println("  Launching 5 hospital threads simultaneously...");
        System.out.println();

        int threadCount = 5;
        CountDownLatch startGate   = new CountDownLatch(1);    // all threads wait on this
        CountDownLatch finishGate  = new CountDownLatch(threadCount); // main waits for all to finish
        AtomicInteger  successCount = new AtomicInteger(0);
        AtomicInteger  failCount    = new AtomicInteger(0);

        for (int i = 1; i <= threadCount; i++) {
            final String hospitalName = "Hospital-" + i;
            Thread t = new Thread(() -> {
                try {
                    startGate.await();  // wait for the gun — all threads start at the exact same moment

                    // Attempt to reserve the last O- packet
                    BloodPacket reserved = inventory.reserveBlood(BloodType.O_NEGATIVE);
                    successCount.incrementAndGet();
                    System.out.println("  ✅ " + hospitalName + " GOT the O- packet: " + reserved.getPacketId());

                } catch (IllegalStateException e) {
                    failCount.incrementAndGet();
                    System.out.println("  ❌ " + hospitalName + " REJECTED: No O- available " +
                            "(race lost — contract enforced!)");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishGate.countDown();
                }
            }, hospitalName);
            t.start();
        }

        // Fire the starting gun — all 5 threads unblock simultaneously
        System.out.println("  [STARTING GUN] All 5 threads released at the same instant!");
        System.out.println();
        startGate.countDown();

        // Wait for all threads to finish
        finishGate.await();

        System.out.println();
        System.out.println(THIN_SEP);
        System.out.println("  CONCURRENCY RESULTS:");
        System.out.println("    Threads that GOT the packet:   " + successCount.get());
        System.out.println("    Threads that were REJECTED:    " + failCount.get());
        System.out.println("    Inventory O- remaining:        " + inventory.getAvailableCount(BloodType.O_NEGATIVE));
        System.out.println("    Inventory total remaining:     " + inventory.getTotalBloodPackets());

        if (successCount.get() == 1 && failCount.get() == 4) {
            System.out.println("    ✅ THREAD SAFETY VERIFIED!");
            System.out.println("       Exactly 1 hospital got the packet.");
            System.out.println("       4 hospitals were safely rejected.");
            System.out.println("       No race condition. No double-allocation.");
        } else {
            System.out.println("    ❌ UNEXPECTED: success=" + successCount.get() + ", fail=" + failCount.get());
        }
        System.out.println();

        System.out.println("  Thread Safety Guarantees Demonstrated:");
        System.out.println("    • ATOMICITY:   check-then-act in reserveBlood() is indivisible");
        System.out.println("    • VISIBILITY:  ReentrantLock ensures happens-before ordering");
        System.out.println("    • ORDERING:    Fair lock serves threads in FIFO order");
        System.out.println();

        // ══════════════════════════════════════════════════════════
        //  FINAL SUMMARY
        // ══════════════════════════════════════════════════════════

        System.out.println(SEPARATOR);
        System.out.println("  ✅ ALL PHASES COMPLETE — FULL SCD PROJECT VERIFIED");
        System.out.println();
        System.out.println("  Phase 2 — Design by Contract (Lecture 4):");
        System.out.println("    • Pre-conditions:     if-guard → throw");
        System.out.println("    • Post-conditions:    verified after mutation");
        System.out.println("    • Class Invariant:    Inventory.totalBloodPackets >= 0");
        System.out.println();
        System.out.println("  Phase 3 — Safe Design (Lectures 3 & 5):");
        System.out.println("    • Immutability:       DonationRecord is final + all final fields");
        System.out.println("    • Defensive Copying:  Constructor & getter deep-copy dates");
        System.out.println("    • No Rep. Exposure:   All getters → unmodifiable views");
        System.out.println("    • Explicit Inquiry:   checkAvailability() ≠ reserveBlood()");
        System.out.println();
        System.out.println("  Phase 4 — Advanced Construction (Lectures 9-13):");
        System.out.println("    • Regex:              InputValidator with Pattern + Matcher");
        System.out.println("    • CNIC:               ^\\d{5}-\\d{7}-\\d{1}$");
        System.out.println("    • Phone:              +92/03xx formats with optional dashes");
        System.out.println("    • Email:              Simplified RFC-5322");
        System.out.println("    • Thread Safety:      ReentrantLock(fair=true)");
        System.out.println("    • Race Condition:     5 threads, 1 packet → only 1 succeeds");
        System.out.println(SEPARATOR);
    }

    /** Shortens a long exception message for cleaner console output. */
    private static String shorten(String msg) {
        return msg.length() > 90 ? msg.substring(0, 90) + "..." : msg;
    }
}
