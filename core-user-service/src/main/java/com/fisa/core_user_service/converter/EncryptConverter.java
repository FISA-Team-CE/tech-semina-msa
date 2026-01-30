package com.fisa.core_user_service.converter;

import com.fisa.core_user_service.util.AES256Util;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

@Converter
@RequiredArgsConstructor
public class EncryptConverter implements AttributeConverter<String, String> {

    private final AES256Util aes256Util;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            return aes256Util.encrypt(attribute);
        } catch (Exception e) {
            throw new RuntimeException("암호화 실패", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            return aes256Util.decrypt(dbData);
        } catch (Exception e) {
            throw new RuntimeException("복호화 실패", e);
        }
    }
}