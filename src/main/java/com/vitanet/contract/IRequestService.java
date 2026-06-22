package com.vitanet.contract;

import com.vitanet.model.BloodRequest;
import com.vitanet.model.Receiver;

import java.util.List;

/**
 * Service interface for blood request operations in VitaNet.
 *
 * <p>All implementations MUST enforce the Design by Contract (DbC)
 * specifications documented on each method.</p>
 */
public interface IRequestService {

    /**
     * Creates a new blood request.
     *
     * <h4>PRE-CONDITIONS:</h4>
     * <ol>
     *   <li>{@code receiver != null}</li>
     *   <li>{@code request != null}</li>
     *   <li>{@code request.getBloodType() != null}</li>
     *   <li>{@code request.getHospital() != null && !request.getHospital().isEmpty()}</li>
     *   <li>{@code request.getRequestType() != null}</li>
     * </ol>
     *
     * <h4>POST-CONDITIONS:</h4>
     * <ol>
     *   <li>The request is persisted and has a non-null requestId</li>
     *   <li>All donors are notified of the new request</li>
     * </ol>
     *
     * @param receiver the receiver submitting the request
     * @param request  the blood request details
     * @return the persisted {@link BloodRequest} with generated ID
     * @throws IllegalArgumentException if any pre-condition is violated
     */
    BloodRequest createRequest(Receiver receiver, BloodRequest request);

    /**
     * Retrieves all active blood requests, optionally filtered.
     *
     * @return list of active requests (never null, may be empty)
     */
    List<BloodRequest> getActiveRequests();

    /**
     * Updates the status of an existing request.
     *
     * <h4>PRE-CONDITIONS:</h4>
     * <ol>
     *   <li>{@code requestId != null}</li>
     *   <li>A request with the given ID must exist</li>
     * </ol>
     *
     * @param requestId the ID of the request to update
     * @param status    the new status
     * @return the updated request
     */
    BloodRequest updateRequestStatus(String requestId, com.vitanet.enums.RequestStatus status);
}
