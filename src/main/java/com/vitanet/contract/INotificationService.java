package com.vitanet.contract;

import com.vitanet.model.BloodPacket;
import com.vitanet.model.BloodRequest;
import com.vitanet.model.Notification;

import java.util.List;

/**
 * Service interface for notification operations in VitaNet.
 */
public interface INotificationService {

    /**
     * Notifies all receivers about a new blood donation.
     *
     * <h4>PRE-CONDITIONS:</h4>
     * <ol>
     *   <li>{@code packet != null}</li>
     * </ol>
     *
     * <h4>POST-CONDITIONS:</h4>
     * <ol>
     *   <li>One notification is created for each registered receiver</li>
     * </ol>
     *
     * @param packet the newly created blood packet
     * @throws IllegalArgumentException if packet is null
     */
    void notifyReceivers(BloodPacket packet);

    /**
     * Notifies all donors about a new blood request.
     *
     * <h4>PRE-CONDITIONS:</h4>
     * <ol>
     *   <li>{@code request != null}</li>
     * </ol>
     *
     * <h4>POST-CONDITIONS:</h4>
     * <ol>
     *   <li>One notification is created for each registered donor</li>
     * </ol>
     *
     * @param request the new blood request
     * @throws IllegalArgumentException if request is null
     */
    void notifyDonors(BloodRequest request);

    /**
     * Retrieves all notifications for a given user.
     *
     * @param userId the user's ID
     * @return list of notifications (never null)
     */
    List<Notification> getUserNotifications(String userId);

    /**
     * Marks all notifications for a user as read.
     *
     * @param userId the user's ID
     */
    void markAllRead(String userId);
}
