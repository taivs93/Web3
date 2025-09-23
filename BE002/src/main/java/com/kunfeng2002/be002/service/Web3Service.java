package com.kunfeng2002.be002.service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.KeccakDigest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.utils.Numeric;

@Service
@Slf4j
public class Web3Service {

    private final Map<String, Web3j> web3Clients;

    public Web3Service(Map<String, Web3j> web3Clients) {
        this.web3Clients = web3Clients;
    }

    public BigInteger getGasPrice(String network) {
        Web3j web3 = web3Clients.get(network.toUpperCase());
        if (web3 == null) {
            throw new IllegalArgumentException("Unsupported network: " + network);
        }

        try {
            EthGasPrice gasPriceResponse = web3.ethGasPrice().send();
            return gasPriceResponse.getGasPrice();
        } catch (Exception e) {
            log.error("Failed to fetch gas price for network {}", network, e);
            throw new RuntimeException("Unable to fetch gas price from " + network, e);
        }
    }

    public Web3j getWeb3(String network) {
        Web3j web3 = web3Clients.get(network.toUpperCase());
        if (web3 == null) {
            throw new IllegalArgumentException("Unsupported network: " + network);
        }
        return web3;
    }

    public boolean verifySignature(String message, String signature, String address) throws SignatureException {
        try {
            System.out.println("=== Web3Service verifySignature ===");
            System.out.println("Original address: " + address);
            System.out.println("Original signature: " + signature);
            System.out.println("Message: " + message);
            
            if (address.startsWith("0x")) {
                address = address.toLowerCase();
            } else {
                address = "0x" + address.toLowerCase();
            }

            if (signature.startsWith("0x")) {
                signature = signature.substring(2);
            }

            System.out.println("Processed address: " + address);
            System.out.println("Processed signature: " + signature);
            System.out.println("Signature length: " + signature.length());

            if (signature.length() != 130) {
                throw new SignatureException("Invalid signature length: " + signature.length());
            }

            boolean result = verifyWithBouncyCastle(message, signature, address);
            System.out.println("Verification result: " + result);
            return result;
            
        } catch (SignatureException e) {
            System.out.println("SignatureException: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            throw new SignatureException("Signature verification failed: " + e.getMessage());
        }
    }

    private boolean verifyWithWeb3j(String message, String signature, String address) throws SignatureException {
        try {
            String prefix = "\u0019Ethereum Signed Message:\n" + message.length();
            String prefixedMessage = prefix + message;

            byte[] messageHash = Hash.sha3(prefixedMessage.getBytes(StandardCharsets.UTF_8));

            byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
            if (signatureBytes.length != 65) {
                throw new SignatureException("Invalid signature bytes length: " + signatureBytes.length);
            }
            byte[] r = new byte[32];
            byte[] s = new byte[32];
            System.arraycopy(signatureBytes, 0, r, 0, 32);
            System.arraycopy(signatureBytes, 32, s, 0, 32);

            int v = signatureBytes[64] & 0xFF;
            int recoveryId = v >= 27 ? v - 27 : v;

            for (int i = 0; i <= 3; i++) {
                try {
                    Sign.SignatureData sigData = new Sign.SignatureData((byte) (i % 2), r, s);
                    BigInteger publicKey = Sign.signedMessageToKey(messageHash, sigData);
                    String recoveredAddress = "0x" + Keys.getAddress(publicKey);
                    
                    if (recoveredAddress.equalsIgnoreCase(address)) {
                        return true;
                    }
                } catch (Exception e) {
                    System.out.println("Web3j recoveryId " + i + " failed: " + e.getMessage());
                }
            }
            
            return false;
        } catch (Exception e) {
            System.out.println("Web3j verification failed: " + e.getMessage());
            return false;
        }
    }

    private boolean verifyWithBouncyCastle(String message, String signature, String address) throws SignatureException {
        try {
            String prefix = "\u0019Ethereum Signed Message:\n" + message.length();
            String prefixedMessage = prefix + message;
            byte[] messageHash = keccak256(prefixedMessage.getBytes(StandardCharsets.UTF_8));

            byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
            
            if (signatureBytes.length != 65) {
                throw new SignatureException("Invalid signature byte length");
            }
            
            byte[] r = new byte[32];
            byte[] s = new byte[32];
            System.arraycopy(signatureBytes, 0, r, 0, 32);
            System.arraycopy(signatureBytes, 32, s, 0, 32);

            BigInteger rBig = new BigInteger(1, r);
            BigInteger sBig = new BigInteger(1, s);

            for (int recoveryId = 0; recoveryId <= 3; recoveryId++) {
                try {
                    String recoveredAddress = recoverAddressBC(messageHash, rBig, sBig, recoveryId);
                    if (recoveredAddress != null && recoveredAddress.equalsIgnoreCase(address)) {
                        return true;
                    }
                } catch (Exception e) {
                    System.out.println("BouncyCastle recoveryId " + recoveryId + " failed: " + e.getMessage());
                }
            }
            return false;
        } catch (Exception e) {
            System.out.println("BouncyCastle verification failed: " + e.getMessage());
            return false;
        }
    }

    private byte[] keccak256(byte[] input) {
        KeccakDigest digest = new KeccakDigest(256);
        digest.update(input, 0, input.length);
        byte[] output = new byte[digest.getDigestSize()];
        digest.doFinal(output, 0);
        return output;
    }

    private String recoverAddressBC(byte[] messageHash, BigInteger r, BigInteger s, int recoveryId) {
        try {
            X9ECParameters params = SECNamedCurves.getByName("secp256k1");
            ECDomainParameters domain = new ECDomainParameters(params.getCurve(), params.getG(), params.getN());
            BigInteger n = domain.getN();
            
            BigInteger x = r;
            if (recoveryId >= 2) {
                x = r.add(n);
            }
            
            ECPoint R = decompressPoint(params.getCurve(), x, (recoveryId & 1) == 1);
            if (R == null) {
                return null;
            }

            BigInteger e = new BigInteger(1, messageHash);
            e = n.subtract(e.mod(n));

            BigInteger rInv = r.modInverse(n);
            ECPoint pubKeyPoint = R.multiply(s).add(domain.getG().multiply(e)).multiply(rInv);

            byte[] pubKeyBytes = pubKeyPoint.getEncoded(false);
            byte[] pubKeyHash = keccak256(java.util.Arrays.copyOfRange(pubKeyBytes, 1, pubKeyBytes.length));

            byte[] address = new byte[20];
            System.arraycopy(pubKeyHash, 12, address, 0, 20);

            return "0x" + Numeric.toHexString(address).substring(2);
        } catch (Exception e) {
            System.out.println("BouncyCastle address recovery failed: " + e.getMessage());
            return null;
        }
    }

    private ECPoint decompressPoint(org.bouncycastle.math.ec.ECCurve curve, BigInteger x, boolean yOdd) {
        try {
            byte[] encoded = new byte[33];
            encoded[0] = (byte) (yOdd ? 0x03 : 0x02);
            byte[] xBytes = x.toByteArray();

            int srcPos = 0;
            if (xBytes.length == 33 && xBytes[0] == 0) {
                srcPos = 1;
            }
            
            int bytesToCopy = Math.min(32, xBytes.length - srcPos);
            int destPos = 33 - bytesToCopy;

            for (int i = 1; i < 33; i++) {
                encoded[i] = 0;
            }
            System.arraycopy(xBytes, srcPos, encoded, destPos, bytesToCopy);
            
            return curve.decodePoint(encoded);
        } catch (Exception e) {
            System.out.println("Failed to decompress point: " + e.getMessage());
            return null;
        }
    }
}