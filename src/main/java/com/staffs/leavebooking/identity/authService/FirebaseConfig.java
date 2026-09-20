package com.staffs.leavebooking.identity.authService;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.http.HttpTransportFactory;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.List;

@Configuration
@Slf4j
@org.springframework.context.annotation.Profile("!test")
public class FirebaseConfig {

    public static final String FIREBASE_CREDENTIALS_FILE_MISSING = "Firebase credentials file missing";
    public static final String SERVICE_ACCOUNT_INVALID_PROJECT_ID = "Service account JSON does not contain a valid project_id";

    private static final String RESOURCE_FILE = "serviceAccountKey.json";

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        ClassPathResource resource = new ClassPathResource(RESOURCE_FILE);
        if (!resource.exists()) {
            throw new FileNotFoundException(FIREBASE_CREDENTIALS_FILE_MISSING);
        }

        try (InputStream serviceAccount = resource.getInputStream()) {

            HttpTransportFactory httpTransportFactory = () -> {
                try {
                    KeyStore windowsRootStore = KeyStore.getInstance("Windows-ROOT");
                    windowsRootStore.load(null, null);

                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    tmf.init(windowsRootStore);

                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, tmf.getTrustManagers(), null);

                    return new NetHttpTransport.Builder().setSslSocketFactory(sslContext.getSocketFactory()).build();
                } catch (Exception e) {
                    log.warn("Could not create Windows-ROOT SSL transport: {}. Falling back to default.", e.getMessage());
                    return new NetHttpTransport();
                }
            };

            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(serviceAccount, httpTransportFactory);

            String projectId = null;
            if (credentials instanceof ServiceAccountCredentials sac) {
                projectId = sac.getProjectId();
            }

            HttpTransport transport = httpTransportFactory.create();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .setHttpTransport(transport)
                    .setConnectTimeout(15000)
                    .setReadTimeout(15000)
                    .build();

            log.info("Firebase initialised for project: {} (with Windows-ROOT SSL trust)", projectId);
            return FirebaseApp.initializeApp(options);
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }

    @Bean
    public JwtDecoder jwtDecoder(FirebaseApp firebaseApp) {
        String projectId = firebaseApp.getOptions().getProjectId();
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalStateException(SERVICE_ACCOUNT_INVALID_PROJECT_ID);
        }

        String jwkSetUri = "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com";

        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        String issuerUri = "https://securetoken.google.com/" + projectId;
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);

        OAuth2TokenValidator<Jwt> withAudience = new JwtClaimValidator<List<String>>(
                "aud",
                audList -> audList != null && audList.contains(projectId)
        );

        OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience);

        jwtDecoder.setJwtValidator(combinedValidator);

        return jwtDecoder;
    }
}
