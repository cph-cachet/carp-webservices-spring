package dk.cachet.carp.webservices.common.configuration.datasource

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.*
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

@Configuration
@ComponentScan(basePackages = ["dk.cachet.carp.webservices"])
@PropertySources(PropertySource(value = ["classpath:config/application-\${spring.profiles.active}.yml"]))
@ConfigurationProperties(prefix = "spring.datasource")
class DataSourceConfiguration(

    @Value("\${spring.datasource.url}") private val datasourceUrl: String,
    @Value("\${spring.datasource.driver-class-name}") private val dbDriverClassName: String,
    @Value("\${spring.datasource.username}") private val dbUsername: String,
    @Value("\${spring.datasource.password}") private val dbPassword: String,
    @Value("\${spring.datasource.hikari.pool-name}") private val carpHikariPool: String,
    @Value("\${spring.datasource.hikari.maximum-pool-size}") private val maxPoolSize: Number,
    @Value("\${spring.datasource.hikari.max-lifetime}") private val maxLifetime: Number,
    @Value("\${spring.datasource.hikari.minimum-idle}") private val minimumIdle: Number,
    @Value("\${spring.datasource.hikari.connection-timeout}") private val connectionTimeout: Number,
    @Value("\${spring.datasource.hikari.leak-detection-threshold}") private val leakDetectionThreshold: Number,
    @Value("\${spring.datasource.hikari.idle-timeout}") private val idleTimeout: Number
): HikariConfig()
{
    @Bean
    @Qualifier("dataSource")
    fun dataSource(): DataSource
    {
        val hikariConfig = HikariConfig()
        hikariConfig.driverClassName = dbDriverClassName
        hikariConfig.jdbcUrl = datasourceUrl
        hikariConfig.username = dbUsername
        hikariConfig.password = dbPassword
        hikariConfig.poolName = carpHikariPool

        // Add DataSource Property
        hikariConfig.maximumPoolSize = maxPoolSize.toInt()
        hikariConfig.idleTimeout = idleTimeout.toLong()
        hikariConfig.maxLifetime = maxLifetime.toLong()
        hikariConfig.minimumIdle = minimumIdle.toInt()
        hikariConfig.connectionTimeout = connectionTimeout.toLong()
        hikariConfig.leakDetectionThreshold = leakDetectionThreshold.toLong()

        // Set data source properties
        hikariConfig.addDataSourceProperty("useUnicode", "true")
        hikariConfig.addDataSourceProperty("characterEncoding", "utf8")
        hikariConfig.addDataSourceProperty("socketTimeout",
            TimeUnit.SECONDS.convert(15, TimeUnit.MINUTES).toInt())

        // Auto Reconnect
        hikariConfig.addDataSourceProperty("autoReconnect", true)

        // Test query
        hikariConfig.connectionTestQuery = "select 1"

        return HikariDataSource(hikariConfig)
    }
}