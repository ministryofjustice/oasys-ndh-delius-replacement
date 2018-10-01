package uk.gov.justice.digital.ndh.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mashape.unirest.http.Unirest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@Slf4j
public class NomisClientConfig {

    @Bean
    public LoadingCache<String, Optional<String>> oauthTokenProvider(@Value("${custody.api.user:none}") String custodyApiUser,
                                                                     @Value("${custody.ai.password:none}") String custodyApiPassword,
                                                                     @Value("${oauth.url}") String oauthUrl) {
        return CacheBuilder.newBuilder().build(new CacheLoader<String, Optional<String>>() {
            @Override
            public Optional<String> load(String key) {
                try {
                    final val body = Unirest.post(oauthUrl)
                            .basicAuth(custodyApiUser, custodyApiPassword)
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .body("grant_type=client_credentials")
                            .asJson().getBody();

                    return Optional.ofNullable(body.getObject().get("access_token").toString());
                } catch (Exception e) {
                    log.error(e.getMessage());
                    return Optional.empty();
                }
            }
        });
    }
}
