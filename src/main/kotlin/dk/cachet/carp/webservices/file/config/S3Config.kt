package dk.cachet.carp.webservices.file.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class S3Config(
    @Value("\${s3.space.key}") private val key: String,
    @Value("\${s3.space.secret}") private val secret: String,
    @Value("\${s3.space.endpoint}") private val endpoint: String,
    @Value("\${s3.space.region}") private val region: String,
) {
    @Bean
    fun configureS3(): AmazonS3 {
        val credentials = BasicAWSCredentials(key, secret)

        return AmazonS3ClientBuilder.standard().withEndpointConfiguration(
            EndpointConfiguration(endpoint, region),
        ).withCredentials(
            AWSStaticCredentialsProvider(credentials),
        ).build()
    }
}
