package com.kunfeng2002.be002.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;

import java.net.URI;

@Configuration
@Slf4j
public class Web3Config {

    @Value("${network.ethereum.rpc-url}")
    private String ethereumRpc;

    @Value("${network.bsc.rpc-url}")
    private String bscRpc;

    @Value("${network.arbitrum.rpc-url}")
    private String arbitrumRpc;

    @Value("${network.optimism.rpc-url}")
    private String optimismRpc;

    @Value("${network.avalanche.rpc-url}")
    private String avalancheRpc;

    @Bean("ETH")
    public Web3j web3Ethereum() {
        return createWeb3jClient(ethereumRpc, "Ethereum");
    }

    @Bean("BSC")
    public Web3j web3Bsc() {
        return createWeb3jClient(bscRpc, "BSC");
    }

    @Bean("ARBITRUM")
    public Web3j web3Arbitrum() {
        return createWeb3jClient(arbitrumRpc, "Arbitrum");
    }

    @Bean("OPTIMISM")
    public Web3j web3Optimism() {
        return createWeb3jClient(optimismRpc, "Optimism");
    }

    @Bean("AVALANCHE")
    public Web3j web3Avalanche() {
        return createWeb3jClient(avalancheRpc, "Avalanche");
    }

    private Web3j createWeb3jClient(String rpcUrl, String networkName) {
        try {
            Web3j web3j;
            if (rpcUrl.startsWith("wss://")) {
                WebSocketService webSocketService = new WebSocketService(new WebSocketClient(new URI(rpcUrl)), true);
                webSocketService.connect();
                web3j = Web3j.build(webSocketService);
                log.info("Connected to {} network using WebSocket. Client version: {}", networkName, web3j.web3ClientVersion().send().getWeb3ClientVersion());
            } else {
                HttpService httpService = new HttpService(rpcUrl);
                httpService.addHeader("Connection", "keep-alive");
                web3j = Web3j.build(httpService);
                log.info("Connected to {} network using HTTP. Client version: {}", networkName, web3j.web3ClientVersion().send().getWeb3ClientVersion());
            }
            return web3j;
        } catch (Exception e) {
            log.error("Failed to connect to {} network at {}", networkName, rpcUrl, e);
            throw new RuntimeException("Failed to initialize " + networkName + " Web3j client", e);
        }
    }
}