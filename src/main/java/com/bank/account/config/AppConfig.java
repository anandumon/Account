//package com.bank.account.config;
//
//import org.keycloak.admin.client.Keycloak;
//import org.keycloak.admin.client.KeycloakBuilder;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.EnableAspectJAutoProxy;
//import org.springframework.web.client.RestTemplate;
//
//@Configuration
//@EnableAspectJAutoProxy
//public class AppConfig {
//
//    @Value("${keycloak.admin.server-url}")
//    private String serverUrl;
//
//    @Value("${keycloak.admin.realm}")
//    private String realm;
//
//    @Value("${keycloak.admin.username}")
//    private String username;
//
//    @Value("${keycloak.admin.password}")
//    private String password;
//
//    @Value("${keycloak.admin.client-id}")
//    private String clientId;
//
//    @Bean
//    public Keycloak keycloak() {
//        return KeycloakBuilder.builder()
//                .serverUrl(serverUrl)
//                .realm(realm)
//                .username(username)
//                .password(password)
//                .clientId(clientId)
//                .build();
//    }
//
//    @Bean
//    public RestTemplate restTemplate() {
//        return new RestTemplate();
//    }
//}
