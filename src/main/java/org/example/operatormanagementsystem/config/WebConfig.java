//package org.example.operatormanagementsystem.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//@RequiredArgsConstructor
//@Configuration
//public class WebConfig  implements WebMvcConfigurer {
//    private final ApiUsageInterceptor apiUsageInterceptor;
//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**")
//                        .allowedOrigins("*") // Frontend domain ở đây!
//                        .allowedMethods("*")
//                        .allowedHeaders("*")
//                        .allowCredentials(false); // Nếu FE có dùng cookie, session
//            }
//        };
//    }
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(apiUsageInterceptor)
//                .addPathPatterns("/api/**"); // hoặc tùy chỉnh
//    }
//}