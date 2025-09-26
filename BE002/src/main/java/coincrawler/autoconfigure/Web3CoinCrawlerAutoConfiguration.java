package coincrawler.autoconfigure;

import com.kunfeng2002.web3.coincrawler.config.Web3CoinCrawlerProperties;
import com.kunfeng2002.web3.coincrawler.controller.Web3CoinCrawlerController;
import com.kunfeng2002.web3.coincrawler.repository.CoinRepository;
import com.kunfeng2002.web3.coincrawler.repository.PairRepository;
import com.kunfeng2002.web3.coincrawler.repository.TokenRepository;
import com.kunfeng2002.web3.coincrawler.service.Web3CoinCrawlerService;
import com.kunfeng2002.web3.coincrawler.service.api.*;
import com.kunfeng2002.web3.coincrawler.service.cache.CacheService;
import com.kunfeng2002.web3.coincrawler.service.crawler.CoinCrawlerService;
import com.kunfeng2002.web3.coincrawler.service.crawler.PairCrawlerService;
import com.kunfeng2002.web3.coincrawler.service.crawler.TokenCrawlerService;
import com.kunfeng2002.web3.coincrawler.service.export.ExportService;
import com.kunfeng2002.web3.coincrawler.service.impl.Web3CoinCrawlerServiceImpl;
import com.kunfeng2002.web3.coincrawler.service.security.SecurityService;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnClass(Web3CoinCrawlerService.class)
@EnableConfigurationProperties(Web3CoinCrawlerProperties.class)
@ConditionalOnProperty(prefix = "web3.coin-crawler", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableJpaRepositories(basePackages = "com.kunfeng2002.web3.coincrawler.repository")
@EnableCaching
@EnableAsync
@EnableScheduling
@Import(JpaRepositoriesAutoConfiguration.class)
public class Web3CoinCrawlerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate(Web3CoinCrawlerProperties properties) {
        RestTemplate restTemplate = new RestTemplate();

        var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(properties.getDatabase().getConnectionPool().getMaximumPoolSize())
                .setMaxConnPerRoute(50)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(org.apache.hc.client5.http.config.RequestConfig.custom()
                        .setConnectionRequestTimeout(Timeout.ofMilliseconds(30000))
                        .setResponseTimeout(Timeout.ofMilliseconds(30000))
                        .build())
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        restTemplate.setRequestFactory(factory);

        return restTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public TheGraphApiService theGraphApiService(Web3CoinCrawlerProperties properties, RestTemplate restTemplate) {
        return new TheGraphApiService(properties, restTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public CoinMarketCapApiService coinMarketCapApiService(Web3CoinCrawlerProperties properties, RestTemplate restTemplate) {
        return new CoinMarketCapApiService(properties, restTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public BSCScanApiService bscScanApiService(Web3CoinCrawlerProperties properties, RestTemplate restTemplate) {
        return new BSCScanApiService(properties, restTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public CoinGeckoApiService coinGeckoApiService(Web3CoinCrawlerProperties properties, RestTemplate restTemplate) {
        return new CoinGeckoApiService(properties, restTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public PancakeSwapApiService pancakeSwapApiService(Web3CoinCrawlerProperties properties, RestTemplate restTemplate) {
        return new PancakeSwapApiService(properties, restTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public CoinCrawlerService coinCrawlerService(
            Web3CoinCrawlerProperties properties,
            CoinRepository coinRepository,
            TheGraphApiService theGraphApiService,
            CoinMarketCapApiService coinMarketCapApiService,
            BSCScanApiService bscScanApiService,
            CacheService cacheService,
            SecurityService securityService) {
        return new CoinCrawlerService(properties, coinRepository, theGraphApiService,
                coinMarketCapApiService, bscScanApiService, cacheService, securityService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PairCrawlerService pairCrawlerService(
            Web3CoinCrawlerProperties properties,
            PairRepository pairRepository,
            CoinRepository coinRepository,
            TheGraphApiService theGraphApiService,
            PancakeSwapApiService pancakeSwapApiService,
            CacheService cacheService,
            SecurityService securityService) {
        return new PairCrawlerService(properties, pairRepository, coinRepository,
                theGraphApiService, pancakeSwapApiService, cacheService, securityService);
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenCrawlerService tokenCrawlerService(
            Web3CoinCrawlerProperties properties,
            TokenRepository tokenRepository,
            TheGraphApiService theGraphApiService,
            CoinGeckoApiService coinGeckoApiService,
            BSCScanApiService bscScanApiService,
            CacheService cacheService,
            SecurityService securityService) {
        return new TokenCrawlerService(properties, tokenRepository, theGraphApiService,
                coinGeckoApiService, bscScanApiService, cacheService, securityService);
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheService cacheService(Web3CoinCrawlerProperties properties) {
        return new CacheService(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityService securityService(Web3CoinCrawlerProperties properties) {
        return new SecurityService(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ExportService exportService() {
        return new ExportService();
    }

    @Bean
    @ConditionalOnMissingBean
    public Web3CoinCrawlerService web3CoinCrawlerService(
            Web3CoinCrawlerProperties properties,
            CoinRepository coinRepository,
            PairRepository pairRepository,
            TokenRepository tokenRepository,
            CoinCrawlerService coinCrawlerService,
            PairCrawlerService pairCrawlerService,
            TokenCrawlerService tokenCrawlerService,
            CacheService cacheService,
            ExportService exportService) {
        return new Web3CoinCrawlerServiceImpl(properties, coinRepository, pairRepository,
                tokenRepository, coinCrawlerService, pairCrawlerService, tokenCrawlerService,
                cacheService, exportService);
    }

    @Bean
    @ConditionalOnMissingBean
    public Web3CoinCrawlerController web3CoinCrawlerController(Web3CoinCrawlerService web3CoinCrawlerService) {
        return new Web3CoinCrawlerController(web3CoinCrawlerService);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "web3.coin-crawler", name = "enabled", havingValue = "true")
    public Web3CoinCrawlerScheduler web3CoinCrawlerScheduler(
            Web3CoinCrawlerProperties properties,
            CoinCrawlerService coinCrawlerService,
            PairCrawlerService pairCrawlerService,
            TokenCrawlerService tokenCrawlerService) {
        return new Web3CoinCrawlerScheduler(properties, coinCrawlerService, pairCrawlerService, tokenCrawlerService);
    }
}
