package dk.cachet.carp.webservices.common.configuration.cache

import com.github.benmanes.caffeine.cache.Caffeine
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
@PropertySources(PropertySource(value = ["classpath:config/application-\${spring.profiles.active}.yml"]))
class CachingConfig (
    @Value("\${caffeine.expire-after-write}") private val expireAfterWrite: Long
){

    companion object {
        val LOGGER: Logger = LogManager.getLogger()
        const val TOKEN_CACHE = "token-cache"
        const val ADMIN_BEARER_TOKEN = "admin-bearer-token"
    }

    @Bean
    fun caffeineConfig(): Caffeine<Any, Any> =
        Caffeine.newBuilder()
            .expireAfterWrite(expireAfterWrite, TimeUnit.MILLISECONDS)
            .evictionListener { key: Any?, _: Any?, cause: Any? ->
                LOGGER.info("Evicting cache entry: $key")
                LOGGER.info("Cache entry eviction cause: $cause")
            }

    @Bean
    fun cacheManager(caffeine: Caffeine<Any, Any>): CacheManager {
        val cacheManager = CaffeineCacheManager()
        cacheManager.setCaffeine(caffeine)
        return cacheManager
    }
}