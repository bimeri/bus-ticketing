package net.gogroups.gowaka.service;

import net.gogroups.gowaka.dto.RefundDTO;
import net.gogroups.gowaka.dto.RequestRefundDTO;
import net.gogroups.gowaka.dto.ResponseRefundDTO;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 12/17/20 8:23 AM <br/>
 */
public interface RefundService {

    void requestRefund(RequestRefundDTO requestRefundDTO, String userId);

    void responseRefund(Long refundId, ResponseRefundDTO responseRefundDTO, String userId);

    RefundDTO getUserRefund(Long refundId, String userId);

    List<RefundDTO> getAllJourneyRefunds(Long journeyId, String userId);

    void refunded(Long refundId, String userId);
}
