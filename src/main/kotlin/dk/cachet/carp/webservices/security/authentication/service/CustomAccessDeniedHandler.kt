package dk.cachet.carp.webservices.security.authentication.service

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Service

@Service
class CustomAccessDeniedHandler : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        accessDeniedException: AccessDeniedException?,
    ) {
        if (response != null && !response.isCommitted) {
            response.status = HttpServletResponse.SC_FORBIDDEN
            response.contentType = "application/json"
            response.characterEncoding = "UTF-8"
            response.writer.use { writer ->
                writer.write(
                    """{
                        "statusCode": 403,
                        "exception": "${accessDeniedException?.javaClass?.kotlin?.qualifiedName}",
                        "message": "Custom error message: Access denied due to insufficient permissions.",
                        "path": "${request?.requestURI}"
                    }""",
                )
            }
        }
        if (accessDeniedException != null) {
            throw accessDeniedException
        } else {
            throw AccessDeniedException("Access denied or studyId does not exist.")
        }
    }
}
