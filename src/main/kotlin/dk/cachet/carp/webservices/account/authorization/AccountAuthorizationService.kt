package dk.cachet.carp.webservices.account.authorization
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import dk.cachet.carp.webservices.account.domain.AccountRequest
//import dk.cachet.carp.webservices.security.authorization.Auth
//import dk.cachet.carp.webservices.security.authorization.Role
//import org.springframework.stereotype.Service
//
//@Service
//class AccountAuthorizationService
//{
//    fun canInvite(accountRequest: AccountRequest): Boolean = canAccountRequestInfoOfAnother( accountRequest )
//    fun canQueryRole(accountRequest: AccountRequest) : Boolean = canAccountRequestInfoOfAnother( accountRequest )
//
//    private fun canAccountRequestInfoOfAnother(accountRequest: AccountRequest) : Boolean
//    {
//        authorizationService.require( Role.RESEARCHER )
//        val requesterRole = authenticationService.getCurrentPrincipal().role!!
//
//        return requesterRole >= Role.RESEARCHER && requesterRole >= accountRequest.role
//    }
//}