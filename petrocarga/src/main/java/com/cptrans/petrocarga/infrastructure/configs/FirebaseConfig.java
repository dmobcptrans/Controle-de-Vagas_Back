package com.cptrans.petrocarga.infrastructure.configs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.cptrans.petrocarga.infrastructure.configs.properties.FirebasePushProperties;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties(FirebasePushProperties.class)
public class FirebaseConfig {
    private final FirebasePushProperties firebasePushProperties;

    public FirebaseConfig(@Autowired FirebasePushProperties firebasePushProperties) {
        this.firebasePushProperties = firebasePushProperties;
    }

/*
* Inicializa o FirebaseApp com as credenciais informadas no arquivo de propriedades.
* Se as credenciais forem informadas, o FirebaseApp é inicializado com as credenciais informadas.
* Se as credenciais forem informadas, mas não é possível inicializar o FirebaseApp, então é lançada exceção de que as credenciais não foram informadas.
*
*/
    @PostConstruct
    public void init() throws IOException {
        InputStream serviceAccount;

        if (!FirebaseApp.getApps().isEmpty() || !firebasePushProperties.isEnabled()) return;

        if(firebasePushProperties.getCredentials() == null || firebasePushProperties.getCredentials().isBlank()){
            throw new IllegalArgumentException("Firebase credentials não foram informados.");
        }

        if(firebasePushProperties.getCredentials().startsWith("classpath:")){
            String path = firebasePushProperties.getCredentials().replace("classpath:", "");
            serviceAccount = new ClassPathResource(path).getInputStream();
        }else{
            serviceAccount = new ByteArrayInputStream(firebasePushProperties.getCredentials().getBytes(StandardCharsets.UTF_8));
        }

        if(serviceAccount == null) return;

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();

        FirebaseApp.initializeApp(options);
    }
}


