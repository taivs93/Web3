package com.kunfeng2002.be002.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class Web3Config {

    // @Value("${network.ethereum.rpc-url}")
    // private String ethereumRpc;

    @Value("${network.bsc.rpc-url}")
    private String bscRpc;

    // @Value("${network.arbitrum.rpc-url}")
    // private String arbitrumRpc;

    // @Value("${network.optimism.rpc-url}")
    // private String optimismRpc;

    // @Value("${network.avalanche.rpc-url}")
    // private String avalancheRpc;

    // @Bean
    // @Qualifier("ETH")
    // public Web3j ethereumWeb3j() {
    //     return createWeb3jClient(ethereumRpc, "Ethereum");
    // }

    @Bean
    @Qualifier("BSC")
    public Web3j bscWeb3j() {
        return createWeb3jClient(bscRpc, "BSC");
    }

    // @Bean
    // @Qualifier("ARBITRUM")
    // public Web3j arbitrumWeb3j() {
    //     return createWeb3jClient(arbitrumRpc, "Arbitrum");
    // }

    // @Bean
    // @Qualifier("OPTIMISM")
    // public Web3j optimismWeb3j() {
    //     return createWeb3jClient(optimismRpc, "Optimism");
    // }

    // @Bean
    // @Qualifier("AVALANCHE")
    // public Web3j avalancheWeb3j() {
    //     return createWeb3jClient(avalancheRpc, "Avalanche");
    // }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Map<String, Web3j> web3Clients() {
        Map<String, Web3j> map = new HashMap<>();
        // map.put("ETH", createWeb3jClient(ethereumRpc, "Ethereum"));
        map.put("BSC", bscWeb3j());
        // map.put("ARBITRUM", createWeb3jClient(arbitrumRpc, "Arbitrum"));
        // map.put("OPTIMISM", createWeb3jClient(optimismRpc, "Optimism"));
        // map.put("AVALANCHE", createWeb3jClient(avalancheRpc, "Avalanche"));
        // map.put("ETH", ethereumWeb3j());
        // map.put("ARBITRUM", arbitrumWeb3j());
        // map.put("OPTIMISM", optimismWeb3j());
        // map.put("AVALANCHE", avalancheWeb3j());
        return map;
    }

    private Web3j createWeb3jClient(String rpcUrl, String networkName) {
        try {
            if (rpcUrl.startsWith("wss://")) {
                WebSocketService wsService = new WebSocketService(new WebSocketClient(new URI(rpcUrl)), true);
                wsService.connect();
                Web3j web3j = Web3j.build(wsService);
                log.info("Connected to {} (WebSocket)", networkName);
                return web3j;
            } else {
                HttpService httpService = new HttpService(rpcUrl);
                httpService.addHeader("Connection", "keep-alive");
                Web3j web3j = Web3j.build(httpService);
                log.info("Connected to {} (HTTP)", networkName);
                return web3j;
            }
        } catch (Exception e) {
            log.error("Failed to connect to {} network: {}", networkName, rpcUrl, e);
            throw new RuntimeException("Failed to initialize " + networkName, e);
        }
    }
}
