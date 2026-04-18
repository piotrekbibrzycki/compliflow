package com.compliflow.transfer_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
@ConfigurationProperties(prefix = "blockchain")
@Getter
@Setter
public class BlockchainProperties {

    private boolean enabled = false;
    private String rpcUrl = "http://localhost:8545";
    private String privateKey = "";
    private String contractAddress = "";
    private String network = "sepolia";
    private long chainId = 11155111L;
    private BigInteger gasPriceWei = BigInteger.valueOf(20_000_000_000L);
    private BigInteger gasLimit = BigInteger.valueOf(300_000L);
}