package com.compliflow.transfer_service.service;

import com.compliflow.transfer_service.config.BlockchainProperties;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class BlockchainAnchorService {

    private final BlockchainProperties blockchainProperties;

    public BlockchainAnchorService(BlockchainProperties blockchainProperties) {
        this.blockchainProperties = blockchainProperties;
    }

    public AnchorResult anchorProofHash(String integrityHash, Long transferId, String proofSchemaVersion) {
        ensureBlockchainEnabled();

        try (Web3j web3j = Web3j.build(new HttpService(blockchainProperties.getRpcUrl()))) {
            Credentials credentials = Credentials.create(blockchainProperties.getPrivateKey());
            RawTransactionManager transactionManager = new RawTransactionManager(
                    web3j,
                    credentials,
                    blockchainProperties.getChainId()
            );

            Function function = new Function(
                    "anchorAuditProof",
                    List.of(
                            toBytes32(integrityHash),
                            new Uint256(BigInteger.valueOf(transferId)),
                            new Utf8String(proofSchemaVersion)
                    ),
                    Collections.emptyList()
            );

            TransactionReceipt receipt = transactionManager.executeTransaction(
                    blockchainProperties.getGasPriceWei(),
                    blockchainProperties.getGasLimit(),
                    blockchainProperties.getContractAddress(),
                    FunctionEncoder.encode(function),
                    BigInteger.ZERO
            );

            boolean verified = receipt != null
                    && receipt.isStatusOK()
                    && isProofAnchored(integrityHash);

            return new AnchorResult(
                    integrityHash,
                    receipt != null ? receipt.getTransactionHash() : null,
                    verified,
                    blockchainProperties.getNetwork(),
                    LocalDateTime.now()
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to communicate with blockchain node", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to anchor compliance proof on-chain", e);
        }
    }

    public boolean isProofAnchored(String integrityHash) {
        ensureBlockchainEnabled();

        try (Web3j web3j = Web3j.build(new HttpService(blockchainProperties.getRpcUrl()))) {
            Function function = new Function(
                    "isProofAnchored",
                    List.of(toBytes32(integrityHash)),
                    List.of(new TypeReference<Bool>() {})
            );

            String encodedFunction = FunctionEncoder.encode(function);
            org.web3j.protocol.core.methods.response.EthCall response = web3j.ethCall(
                    Transaction.createEthCallTransaction(
                            null,
                            blockchainProperties.getContractAddress(),
                            encodedFunction
                    ),
                    DefaultBlockParameterName.LATEST
            ).send();

            List<org.web3j.abi.datatypes.Type> decoded = FunctionReturnDecoder.decode(
                    response.getValue(),
                    function.getOutputParameters()
            );

            if (decoded.isEmpty()) {
                return false;
            }

            Bool result = (Bool) decoded.getFirst();
            return result.getValue();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to verify anchored proof on-chain", e);
        }
    }

    private void ensureBlockchainEnabled() {
        if (!blockchainProperties.isEnabled()) {
            throw new IllegalStateException("Blockchain anchoring is disabled. Enable blockchain.enabled and configure RPC, private key, and contract address.");
        }
        if (blockchainProperties.getPrivateKey() == null || blockchainProperties.getPrivateKey().isBlank()) {
            throw new IllegalStateException("Blockchain private key is missing.");
        }
        if (blockchainProperties.getContractAddress() == null || blockchainProperties.getContractAddress().isBlank()) {
            throw new IllegalStateException("Blockchain contract address is missing.");
        }
    }

    private Bytes32 toBytes32(String hexHash) {
        byte[] raw = Numeric.hexStringToByteArray(hexHash);
        if (raw.length > 32) {
            raw = Arrays.copyOfRange(raw, raw.length - 32, raw.length);
        }
        if (raw.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(raw, 0, padded, 32 - raw.length, raw.length);
            raw = padded;
        }
        return new Bytes32(raw);
    }

    public record AnchorResult(
            String integrityHash,
            String transactionHash,
            boolean verified,
            String network,
            LocalDateTime anchoredAt
    ) {
    }
}