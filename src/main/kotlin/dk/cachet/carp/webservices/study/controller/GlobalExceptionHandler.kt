package dk.cachet.carp.webservices.study.controller
import dk.cachet.carp.webservices.study.exceptions.StudyServiceException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(StudyServiceException::class)
    fun handleStudyServiceException(ex: StudyServiceException): ResponseEntity<String> {
        return ResponseEntity.status(ex.status).body(ex.message)
    }
}
