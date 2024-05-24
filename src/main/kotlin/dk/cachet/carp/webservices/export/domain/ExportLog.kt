package dk.cachet.carp.webservices.export.domain

import org.apache.logging.log4j.LogManager
import java.time.Instant

data class ExportLog(
    val createdAt: Instant = Instant.now(),
    val infoLogs: MutableList<String> = mutableListOf(),
    val errorLogs: MutableList<String> = mutableListOf()
)
{
    companion object
    {
        private val LOGGER = LogManager.getLogger()
    }

    fun info( log: String )
    {
        LOGGER.info( log )
        infoLogs.add( log )
    }

    fun error( log: String, throwable: Throwable? )
    {
        LOGGER.error( log, throwable )
        errorLogs.add( log )
    }
}
