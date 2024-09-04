package dk.cachet.carp.webservices.study.exceptions

import dk.cachet.carp.common.application.UUID
import org.springframework.http.HttpStatus

class StudyServiceException(message: String, val status: HttpStatus) : RuntimeException(message) {
    companion object {
        fun studyNotFound(studyId: UUID): StudyServiceException {
            return StudyServiceException("Study with id $studyId does not exist.", HttpStatus.BAD_REQUEST)
        }

        fun manageStudyPermissionDenied(): StudyServiceException {
            return StudyServiceException("You do not have permission to manage this study.", HttpStatus.FORBIDDEN)
        }
    }
}
