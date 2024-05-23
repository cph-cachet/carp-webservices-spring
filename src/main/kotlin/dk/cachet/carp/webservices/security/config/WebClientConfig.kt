package dk.cachet.carp.webservices.security.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig
{
    @Bean
    fun defaultExchangeStrategies(
        objectMapper: ObjectMapper
    ) = ExchangeStrategies.builder()
        .codecs {
            it.defaultCodecs().jackson2JsonEncoder( Jackson2JsonEncoder( objectMapper, MediaType.APPLICATION_JSON ) )
            it.defaultCodecs().jackson2JsonDecoder( Jackson2JsonDecoder( objectMapper, MediaType.APPLICATION_JSON ) )
        }.build()

    @Bean
    fun defaultWebClientBuilder(
        authorizedClientManager: ReactiveOAuth2AuthorizedClientManager,
        defaultExchangeStrategies: ExchangeStrategies
    ): WebClient.Builder
    {
        val authorizedClientFilter = ServerOAuth2AuthorizedClientExchangeFilterFunction( authorizedClientManager )

        return WebClient.builder()
            .exchangeStrategies( defaultExchangeStrategies )
            .defaultHeaders {
                it.contentType = MediaType.APPLICATION_JSON
                it.accept = listOf(MediaType.APPLICATION_JSON)
            }
            .filter( authorizedClientFilter )
    }

    @Bean
    fun authorizedClientManager(
        clientRegistrationRepository: ReactiveClientRegistrationRepository,
        authorizedClientService: ReactiveOAuth2AuthorizedClientService
    ): ReactiveOAuth2AuthorizedClientManager
    {
        val authorizedClientManager =
            AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService
            )

        authorizedClientManager.setAuthorizedClientProvider(
            ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build()
        )

        return authorizedClientManager
    }
}