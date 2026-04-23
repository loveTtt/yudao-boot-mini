package com.jsmauto.wms.module.system.framework.captcha.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CaptchaSpiResourceTest {

    @ParameterizedTest
    @CsvSource({
            "META-INF/services/com.anji.captcha.service.CaptchaCacheService,com.jsmauto.wms.module.system.framework.captcha.core.RedisCaptchaServiceImpl",
            "META-INF/services/com.anji.captcha.service.CaptchaService,com.jsmauto.wms.module.system.framework.captcha.core.PictureWordCaptchaServiceImpl"
    })
    void shouldLoadSpiResourceWithoutUtf8Bom(String resourcePath, String expectedClassName) throws IOException {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            assertNotNull(inputStream, () -> "资源不存在: " + resourcePath);
            byte[] bytes = inputStream.readAllBytes();
            assertFalse(hasUtf8Bom(bytes), () -> "SPI 资源不允许包含 UTF-8 BOM: " + resourcePath);
            assertEquals(expectedClassName, new String(bytes, StandardCharsets.UTF_8).trim());
        }
    }

    private boolean hasUtf8Bom(byte[] bytes) {
        return bytes.length >= 3
                && bytes[0] == (byte) 0xEF
                && bytes[1] == (byte) 0xBB
                && bytes[2] == (byte) 0xBF;
    }
}
