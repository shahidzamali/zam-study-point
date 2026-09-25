package com.zam.digitalstore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/products/**")
                .addResourceLocations("file:uploads/products/");

        registry.addResourceHandler("/previews/**")
                .addResourceLocations("file:uploads/previews/");

        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations("file:uploads/thumbnails/");
    }
}