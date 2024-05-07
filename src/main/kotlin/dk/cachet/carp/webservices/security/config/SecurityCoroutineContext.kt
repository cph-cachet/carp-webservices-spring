package dk.cachet.carp.webservices.security.config

import kotlinx.coroutines.ThreadContextElement
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import kotlin.coroutines.CoroutineContext

// https://blog.jdriven.com/2021/07/propagating-the-spring-securitycontext-to-your-kotlin-coroutines/
class SecurityCoroutineContext(
    private val securityContext: SecurityContext = SecurityContextHolder.getContext()
) : ThreadContextElement<SecurityContext?>
{
    companion object Key : CoroutineContext.Key<SecurityCoroutineContext>

    override val key: CoroutineContext.Key<SecurityCoroutineContext> get() = Key

    override fun updateThreadContext( context: CoroutineContext ): SecurityContext?
    {
        val previousSecurityContext = SecurityContextHolder.getContext()
        SecurityContextHolder.setContext(securityContext)
        return previousSecurityContext.takeIf { it.authentication != null }
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: SecurityContext?)
    {
        if ( oldState == null )
        {
            SecurityContextHolder.clearContext()
        }
        else
        {
            SecurityContextHolder.setContext(oldState)
        }
    }
}