package com.example.kafkaconsumer.service;

import encryption.aes.AESEncryptionAndDecryptionKeys;
import encryption.aes.AESEncryptionAndDecryptionService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);


    @NonNull
    private final AESEncryptionAndDecryptionService aesEncryptionAndDecryptionService;

    @KafkaListener(
            topics = "${kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(ConsumerRecord<String, String> record) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        var headerMaps = headersToMap(record.headers());
        String encryptedKey = headerMaps.get(AESEncryptionAndDecryptionKeys.ENCRYPTION_KEY_CIPHER_TEXT.getKey());
        String transitKeyName = headerMaps.get(AESEncryptionAndDecryptionKeys.ENCRYPTION_KEY_NAME.getKey());
        String messageValueAsString = aesEncryptionAndDecryptionService.decrypt(record.value(), encryptedKey, transitKeyName);
        logger.info("Received message: Key = {}, Value = {}", record.key(), messageValueAsString);
    }

    private Map<String, String> headersToMap(Headers headers) {
        Map<String, String> map = new HashMap<>();
        if (headers == null) {
            return map;
        }
        for (Header header : headers) {
            map.put(header.key(), new String(header.value(), StandardCharsets.UTF_8));

        }
        return map;
    }
}
