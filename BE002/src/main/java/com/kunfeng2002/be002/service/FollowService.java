package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.dto.request.FollowRequest;
import com.kunfeng2002.be002.entity.FollowedAddress;
import com.kunfeng2002.be002.repository.FollowedAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final FollowedAddressRepository repository;
    private final BlockchainListener blockchainListener;

    @Transactional
    public void follow(FollowRequest request) {
        String normalizedAddress = request.getAddress().toLowerCase();
        String normalizedNetwork = request.getNetwork().toUpperCase();

        if (repository.existsByAddressAndNetwork(normalizedAddress, normalizedNetwork)) {
            log.info("Address {} already followed on network {}", normalizedAddress, normalizedNetwork);
            return;
        }

        FollowedAddress fa = FollowedAddress.builder()
                .address(normalizedAddress)
                .network(normalizedNetwork)
                .build();

        repository.save(fa);

        blockchainListener.addToCache(normalizedNetwork, normalizedAddress);

        log.info("Started following address {} on network {}", normalizedAddress, normalizedNetwork);
    }

    @Transactional
    public void unfollow(String address, String network) {
        String normalizedAddress = address.toLowerCase();
        String normalizedNetwork = network.toUpperCase();

        repository.deleteByAddressAndNetwork(normalizedAddress, normalizedNetwork);

        blockchainListener.removeFromCache(normalizedNetwork, normalizedAddress);

        log.info("Stopped following address {} on network {}", normalizedAddress, normalizedNetwork);
    }

    @Cacheable(value = "followedAddresses", key = "#network")
    public Set<String> getFollowedAddresses(String network) {
        return repository.findByNetwork(network.toUpperCase())
                .stream()
                .map(FollowedAddress::getAddress)
                .collect(Collectors.toSet());
    }

    public List<FollowedAddress> getAllFollowedAddresses() {
        return repository.findAll();
    }

    public boolean isAddressFollowed(String address, String network) {
        return repository.existsByAddressAndNetwork(
                address.toLowerCase(),
                network.toUpperCase()
        );
    }

    public List<String> getSupportedNetworks() {
        return List.of("ETH", "BSC", "ARBITRUM", "OPTIMISM", "AVALANCHE");
    }
}