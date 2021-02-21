package net.gogroups.gowaka.domain.service;

import lombok.extern.slf4j.Slf4j;
import net.gogroups.gowaka.domain.model.ServiceCharge;
import net.gogroups.gowaka.domain.repository.ServiceChargeServiceRepository;
import net.gogroups.gowaka.dto.ServiceChargeDTO;
import net.gogroups.gowaka.exception.ResourceNotFoundException;
import net.gogroups.gowaka.service.ServiceChargeService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Author: Edward Tanko <br/>
 * Date: 10/31/20 6:06 AM <br/>
 */
@Service
@Slf4j
public class ServiceChargeServiceImpl implements ServiceChargeService {

    private ServiceChargeServiceRepository serviceChargeServiceRepository;

    public ServiceChargeServiceImpl(ServiceChargeServiceRepository serviceChargeServiceRepository) {
        this.serviceChargeServiceRepository = serviceChargeServiceRepository;
    }

    @Override
    @Cacheable("service_charges")
    public List<ServiceChargeDTO> getServiceCharges() {
        return serviceChargeServiceRepository.findAll()
                .stream()
                .map(serviceCharge -> new ServiceChargeDTO(serviceCharge.getId(), serviceCharge.getPercentageCharge(), serviceCharge.getFlatCharge()))
                .collect(Collectors.toList());
    }

    @Override
    public void updateServiceCharge(ServiceChargeDTO serviceChargeDTO) {
        Optional<ServiceCharge> serviceChargeOptional = serviceChargeServiceRepository.findById(serviceChargeDTO.getId());
        if(!serviceChargeOptional.isPresent()){
            throw new ResourceNotFoundException("Service charge not found");
        }
        ServiceCharge serviceCharge = serviceChargeOptional.get();
        serviceCharge.setPercentageCharge(serviceChargeDTO.getPercentageCharge());
        serviceChargeServiceRepository.save(serviceCharge);
    }
}
