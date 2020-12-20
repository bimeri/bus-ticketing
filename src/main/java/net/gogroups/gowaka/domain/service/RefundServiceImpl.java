package net.gogroups.gowaka.domain.service;

import net.gogroups.gowaka.dto.RefundDTO;
import net.gogroups.gowaka.dto.RequestRefundDTO;
import net.gogroups.gowaka.dto.ResponseRefundDTO;
import net.gogroups.gowaka.service.RefundService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Author: Edward Tanko <br/>
 * Date: 12/17/20 8:24 AM <br/>
 */
@Service
public class RefundServiceImpl implements RefundService {

    @Override
    public void requestRefund(RequestRefundDTO requestRefundDTO, String userId) {

    }

    @Override
    public void responseRefund(Long refundId, ResponseRefundDTO responseRefundDTO, String userId) {

    }

    @Override
    public RefundDTO getUserRefund(Long refundId, String userId) {
        return null;
    }

    @Override
    public List<RefundDTO> getAllJourneyRefunds(Long journeyId, String userId) {
        return null;
    }

    @Override
    public void refunded(Long refundId, String userId) {

    }
}
