package dk.cachet.carp.webservices.file.authorization

import dk.cachet.carp.webservices.file.repository.FileRepository
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import org.springframework.stereotype.Component

@Component
class FileControllerAuthorizer(
    private val fileRepository: FileRepository,
    private val authenticationService: AuthenticationService,
) {
    fun isFileOwner(fileId: Int): Boolean {
        val file = fileRepository.findById(fileId)

        if (file.isEmpty) return false

        return authenticationService.getId().stringRepresentation == file.get().ownerId
    }
}
