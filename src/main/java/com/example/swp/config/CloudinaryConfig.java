package com.example.swp.config;// CloudinaryConfig.java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dvri1a097",
                "api_key", "757614395724356",
                "api_secret", "k3XCDxMm8l2G5_-xD-blhufQ1a4",
                "secure", true
        ));
    }
}