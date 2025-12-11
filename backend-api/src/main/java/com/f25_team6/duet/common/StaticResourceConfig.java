package com.f25_team6.duet.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
  @Override
  public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
    // Serve files saved under the working directory's ./uploads folder at the /uploads/** URL path
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:./uploads/");
  }
}
