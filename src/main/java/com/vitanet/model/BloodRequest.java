package com.vitanet.model;

import com.vitanet.enums.BloodType;
import com.vitanet.enums.RequestStatus;
import com.vitanet.enums.RequestType;

import java.time.LocalDateTime;

/**
 * Represents a blood request posted by a {@link Receiver}.
 *
 * <p>Maps to the Mongoose {@code RequestPost} schema.</p>
 *
 * <h3>Class Invariant</h3>
 * <ul>
 *   <li>{@code receiverId != null}</li>
 *   <li>{@code bloodType != null}</li>
 *   <li>{@code requestType != null}</li>
 *   <li>{@code status != null}</li>
 * </ul>
 */
public class BloodRequest {

    private String        requestId;
    private String        receiverId;
    private BloodType     bloodType;
    private String        hospital;
    private RequestType   requestType;
    private String        notes;
    private RequestStatus status;
    private LocalDateTime createdAt;

    // ── Constructors ──────────────────────────────────────────

    public BloodRequest() {
        this.status    = RequestStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    public BloodRequest(String requestId, String receiverId, BloodType bloodType,
                        String hospital, RequestType requestType, String notes) {
        this.requestId   = requestId;
        this.receiverId  = receiverId;
        this.bloodType   = bloodType;
        this.hospital    = hospital;
        this.requestType = requestType;
        this.notes       = notes != null ? notes : "";
        this.status      = RequestStatus.ACTIVE;
        this.createdAt   = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────

    public String        getRequestId()     { return requestId; }
    public void          setRequestId(String id) { this.requestId = id; }

    public String        getReceiverId()    { return receiverId; }
    public void          setReceiverId(String id) { this.receiverId = id; }

    public BloodType     getBloodType()     { return bloodType; }
    public void          setBloodType(BloodType bt) { this.bloodType = bt; }

    public String        getHospital()      { return hospital; }
    public void          setHospital(String h) { this.hospital = h; }

    public RequestType   getRequestType()   { return requestType; }
    public void          setRequestType(RequestType rt) { this.requestType = rt; }

    public String        getNotes()         { return notes; }
    public void          setNotes(String n) { this.notes = n; }

    public RequestStatus getStatus()        { return status; }

    public LocalDateTime getCreatedAt()     { return createdAt; }

    // ── Domain Operations ─────────────────────────────────────

    /**
     * Marks this request as fulfilled.
     *
     * <p><b>Pre-condition:</b>  {@code status == ACTIVE}</p>
     * <p><b>Post-condition:</b> {@code status == FULFILLED}</p>
     *
     * @throws IllegalStateException if the request is not ACTIVE
     */
    public void fulfill() {
        if (this.status != RequestStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Contract violation [Pre-condition]: Cannot fulfill request in state: " + status);
        }
        this.status = RequestStatus.FULFILLED;
    }

    /**
     * Cancels this request.
     *
     * <p><b>Pre-condition:</b>  {@code status == ACTIVE}</p>
     * <p><b>Post-condition:</b> {@code status == CANCELLED}</p>
     *
     * @throws IllegalStateException if the request is not ACTIVE
     */
    public void cancel() {
        if (this.status != RequestStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Contract violation [Pre-condition]: Cannot cancel request in state: " + status);
        }
        this.status = RequestStatus.CANCELLED;
    }

    /**
     * Checks if this is an emergency request.
     *
     * @return {@code true} if requestType is EMERGENCY
     */
    public boolean isEmergency() {
        return this.requestType == RequestType.EMERGENCY;
    }

    @Override
    public String toString() {
        return String.format("BloodRequest[id=%s, type=%s, blood=%s, status=%s, emergency=%s]",
                requestId, requestType, bloodType, status, isEmergency());
    }
}
