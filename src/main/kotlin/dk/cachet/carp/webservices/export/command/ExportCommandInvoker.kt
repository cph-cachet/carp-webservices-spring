package dk.cachet.carp.webservices.export.command

import dk.cachet.carp.webservices.export.domain.ExportStatus
import dk.cachet.carp.webservices.export.repository.ExportRepository
import dk.cachet.carp.webservices.security.config.SecurityCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service

interface ExportCommandInvoker {
    fun invoke(command: ExportCommand)
}

@Service
class ExportCommandInvokerImpl(
    private val exportRepository: ExportRepository,
) : ExportCommandInvoker {
    override fun invoke(command: ExportCommand) {
        val constrainCheck = command.canExecute()
        require(constrainCheck.first) { constrainCheck.second }

        CoroutineScope(Dispatchers.IO + SecurityCoroutineContext()).launch {
            try {
                command.execute()

                exportRepository.updateExportStatus(ExportStatus.AVAILABLE, command.entry.id)
            } catch (_: Throwable) {
                exportRepository.updateExportStatus(ExportStatus.ERROR, command.entry.id)
            }
        }
    }
}
