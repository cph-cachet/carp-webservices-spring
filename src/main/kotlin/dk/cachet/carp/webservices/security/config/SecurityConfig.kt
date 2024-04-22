package dk.cachet.carp.webservices.security.config

import com.c4_soft.springaddons.security.oidc.spring.SpringAddonsMethodSecurityExpressionHandler
import com.c4_soft.springaddons.security.oidc.spring.SpringAddonsMethodSecurityExpressionRoot
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.Role
import dk.cachet.carp.webservices.study.repository.CoreParticipantRepository
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.core.context.SecurityContextHolder

/**
 * The default security configuration is overridden by c4-soft-spring-addons to be easier to configure.
 * The configuration takes place in the application-properties file and there is no need to write boilerplate.
 *
 * There is a lot of useful information about both this add-on, Spring Security and OAuth in general in its
 * [GitHub repository](https://github.com/ch4mpy/spring-addons/tree/master/spring-addons-starter-oidc)
 */
@Configuration
@EnableMethodSecurity
class SecurityConfig
{
    @Bean
    fun methodSecurityExpressionHandler(
        participantRepository: CoreParticipantRepository
    ): MethodSecurityExpressionHandler =
        SpringAddonsMethodSecurityExpressionHandler {
            ProxiesMethodSecurityExpressionRoot( participantRepository )
        }

    @PostConstruct
    fun init() =
        SecurityContextHolder.setStrategyName( SecurityContextHolder.MODE_INHERITABLETHREADLOCAL )
}

/**
 * Proxies the method security expression root to add custom methods.
 *
 * These methods can be used in SpeL-expressions (e.g., `@PreAuthorize`) in Spring Security annotations.
 */
class ProxiesMethodSecurityExpressionRoot(
    private val participantRepository: CoreParticipantRepository
): SpringAddonsMethodSecurityExpressionRoot()
{
    fun canManageStudy( studyId: UUID ) : Boolean =
        getCarpClaims().contains( Claim.ManageStudy( studyId ) ) || isAdmin()

    fun isProtocolOwner( protocolId: UUID ) : Boolean =
        getCarpClaims().contains( Claim.ProtocolOwner( protocolId ) ) || isAdmin()

    fun isInDeployment( deploymentId: UUID ) : Boolean =
        getCarpClaims().contains( Claim.InDeployment( deploymentId ) ) || isAdmin()

    fun canManageDeployment( deploymentId: UUID ) : Boolean =
        getCarpClaims().contains( Claim.ManageDeployment( deploymentId ) ) || isAdmin()

    fun isConsentOwner( consentId: Int ) : Boolean =
        getCarpClaims().contains( Claim.ConsentOwner( consentId ) ) || isAdmin()

    fun isCollectionOwner( collectionId: Int ) : Boolean =
        getCarpClaims().contains( Claim.CollectionOwner( collectionId ) ) || isAdmin()

    fun isFileOwner( fileId: Int ) : Boolean =
        getCarpClaims().contains( Claim.FileOwner( fileId ) ) || isAdmin()

    // HACK: it is not easy to assign a claim with a studyId when creating deployments,
    // so we inject `CoreParticipantRepository` here to check whether the user is in a deployment
    // which is part of this study.
    //
    // A potential workaround would be to redesign the endpoints for consent, documents, collections and files.
    fun isInDeploymentOfStudy( studyId: UUID ) : Boolean =
        runBlocking(Dispatchers.IO + SecurityCoroutineContext() )
        {
            if ( isAdmin() ) return@runBlocking true

            val id =
                participantRepository.getRecruitment( studyId )?.participantGroups?.keys
                    ?.firstOrNull {
                        getCarpClaims().contains(
                            Claim.InDeployment( it )
                        )
                    }

            id != null
        }

    // TODO: There is probably a way to set up automatic conversion for JWTs in Spring Security
    private fun getCarpClaims(): Collection<Claim> =
        authentication.authorities.mapNotNull { Claim.fromGrantedAuthority( it.authority ) }

    private fun isAdmin() : Boolean = hasRole( Role.SYSTEM_ADMIN.toString() )
}
