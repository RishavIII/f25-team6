package com.f25_team6.duet.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String uploadsDir = System.getProperty("user.dir") + System.getProperty("file.separator") + "uploads" + System.getProperty("file.separator");
    registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + uploadsDir);
  }
}
