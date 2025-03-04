package dk.cachet.carp.webservices.file.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI

@Configuration
class S3Config(
    @Value("\${s3.space.key}") private val key: String,
    @Value("\${s3.space.secret}") private val secret: String,
    @Value("\${s3.space.endpoint}") private val endpoint: String, // without bucketname in front
) {
    @Bean
    fun s3Client(): S3Client {
        val credentials = AwsBasicCredentials.create(key, secret)
        val region = "us-east-1" // required by DigitalOcean for some setup

        return S3Client.builder()
            .endpointOverride(URI.create(endpoint))
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(false).build())
            .build()
    }
}
