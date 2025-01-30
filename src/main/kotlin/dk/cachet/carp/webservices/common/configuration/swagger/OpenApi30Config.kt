package dk.cachet.carp.webservices.common.configuration.swagger

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest
import dk.cachet.carp.webservices.common.environment.EnvironmentUtil
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.beans.BeanUtils
import org.springframework.beans.BeanWrapper
import org.springframework.beans.BeanWrapperImpl
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.util.MimeTypeUtils
import org.springframework.util.StreamUtils
import org.springframework.util.StringUtils
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

// https://stackoverflow.com/q/59898874/13179591
// https://stackoverflow.com/a/73622024/13179591
@Configuration
class OpenApi30Config(
    @Value("\${spring.application.name}") private val moduleName: String,
    @Value("\${spring.application.version}") private val apiVersion: String,
    @Value("classpath:openapi/description.txt") private val docResource: Resource,
    private val objectMapper: ObjectMapper,
    private val environmentUtil: EnvironmentUtil,
) {
    companion object {
        const val SCHEME = "bearer"
        const val FORMAT = "JWT"
        const val BEARER_DESCRIPTION =
            "Provide the bearer token. A Bearer token can be acquired from the POST /oauth/token endpoint."
        const val OPENAPI_FOLDER = "/openapi"
    }

    @Bean
    fun customOpenAPI(): OpenAPI? {
        val apiTitle = String.format(Locale.getDefault(), "%s API", StringUtils.capitalize(moduleName))

        return OpenAPI()
            .addServersItem(Server().url(environmentUtil.url))
            .addSecurityItem(SecurityRequirement().addList(SCHEME))
            .components(
                Components()
                    .addSecuritySchemes(
                        SCHEME,
                        SecurityScheme()
                            .name(SCHEME)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme(SCHEME)
                            .description(BEARER_DESCRIPTION)
                            .bearerFormat(FORMAT),
                    ),
            )
            .info(
                Info()
                    .title(apiTitle)
                    .version(apiVersion)
                    .description(getResourceContent(docResource)),
            )
    }

//    @Bean
//    fun openApiCustomizer(loadedOperations: Map<String, Operation>): OpenApiCustomizer? {
//        return OpenApiCustomizer { openAPI: OpenAPI ->
//            for (path in openAPI.paths.values.stream()) {
//                val operations = arrayListOf(path.post, path.get, path.put, path.delete)
//                for (operation in operations) {
//                    if (operation == null) {
//                        continue
//                    }
//
//                    val loadedOperation = loadedOperations[operation.tags[0]] ?: continue
//
//                    // schema has to be present, otherwise examples will not load on the swagger UI
//                    val schema = operation.requestBody?.content?.get(MimeTypeUtils.APPLICATION_JSON_VALUE)?.schema
//
//                    val ignoredNames = getNullPropertyNames(loadedOperation)
//                    BeanUtils.copyProperties(loadedOperation, operation, *ignoredNames)
//
//                    if (schema != null) {
//                        operation.requestBody?.content?.get(MimeTypeUtils.APPLICATION_JSON_VALUE)?.schema = schema
//                    }
//                }
//            }
//        }
//    }

    @Bean
    fun openApiCustomizer(loadedOperations: Map<String, Operation>): OpenApiCustomizer {
        return OpenApiCustomizer { openAPI: OpenAPI ->
            for (path in openAPI.paths.values) {
                val operations = listOf(path.post, path.get, path.put, path.delete)

                for (operation in operations) {
                    if (operation == null) continue

                    val loadedOperation = loadedOperations[operation.tags[0]] ?: continue

                    BeanUtils.copyProperties(loadedOperation, operation)

                    // whatever is in the json files (resources/openapi/../*.json) overwrites the stuff generated by
                    // annotations in the controllers
                    operation.requestBody = loadedOperation.requestBody
                    operation.responses = loadedOperation.responses
                    operation.parameters = loadedOperation.parameters
                }
            }

            generateAdditionalSchemasForOpenApi(openAPI)
        }
    }

    @Bean
    fun loadOperationsDocumentation(): Map<String, Operation> {
        val loader = PathMatchingResourcePatternResolver()
        val resources = loader.getResources("classpath*:$OPENAPI_FOLDER/**/*.json")
        val operations = mutableMapOf<String, Operation>()

        for (resource in resources) {
            val operation = objectMapper.readValue(resource.inputStream, Operation::class.java)
            // keys in the form of 'accounts/inviteStudyOwner.json'
            val key = resource.url.toExternalForm().split('/').takeLast(2).joinToString("/")
            operations[key] = operation
        }

        return operations
    }

    fun getNullPropertyNames(obj: Any): Array<String> {
        val objWrapper: BeanWrapper = BeanWrapperImpl(obj)

        val nullPropertyNames = ArrayList<String>()
        for (descriptor in objWrapper.propertyDescriptors) {
            val value = objWrapper.getPropertyValue(descriptor.name)
            if (value == null) {
                nullPropertyNames.add(descriptor.name)
            }
        }

        return nullPropertyNames.toTypedArray()
    }

    private fun getResourceContent(resource: Resource): String {
        return StreamUtils.copyToString(resource.inputStream, Charset.defaultCharset())
    }

    private fun generateAdditionalSchemasForOpenApi(openAPI: OpenAPI) {
        // the json files (resources/openapi/../*.json) reference schemas that might not
        // been previously automatically generated. If that's the case, we need to add them manually
        val schemas = mutableSetOf<Class<*>>()
        schemas.add(ProtocolServiceRequest.Add::class.java)
        schemas.add(Unit::class.java)
        schemas.add(ProtocolVersion::class.java)
        schemas.add(ProtocolServiceRequest.GetVersionHistoryFor::class.java)
        schemas.add(ProtocolServiceRequest.GetAllForOwner::class.java)
        schemas.add(ProtocolServiceRequest.GetBy::class.java)
        schemas.add(ProtocolServiceRequest.UpdateParticipantDataConfiguration::class.java)
        schemas.add(ProtocolServiceRequest.AddVersion::class.java)
        schemas.add(ApiVersion::class.java)

        for (clazz in schemas) {
            if (openAPI.components.schemas.containsKey(clazz.simpleName)) {
                continue
            }
            openAPI.components.addSchemas(
                clazz.simpleName,
                ModelConverters.getInstance().readAllAsResolvedSchema(clazz).schema
            )
        }
    }
}
