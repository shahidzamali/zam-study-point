package com.zam.digitalstore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {

                // ==============================
                // PRODUCT PDF FILES
                // ==============================
                registry.addResourceHandler("/files/**")
                                .addResourceLocations("file:uploads/products/");

                // ==============================
                // PREVIEW / DEMO IMAGES
                // ==============================
                registry.addResourceHandler("/previews/**")
                                .addResourceLocations("file:uploads/previews/");

                // ==============================
                // THUMBNAIL IMAGES
                // ==============================
                registry.addResourceHandler("/thumbnails/**")
                                .addResourceLocations("file:uploads/thumbnails/");
        }
}