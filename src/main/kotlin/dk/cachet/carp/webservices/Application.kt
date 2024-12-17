package dk.cachet.carp.webservices

import dk.cachet.carp.webservices.common.exception.file.FileStorageException
import dk.cachet.carp.webservices.file.repository.FileRepository
import org.springframework.beans.BeansException
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component
import dk.cachet.carp.webservices.file.util.FileUtil
import dk.cachet.carp.webservices.file.util.FileUtil.OS
import org.apache.commons.io.FileUtils
import java.nio.file.Path
import java.nio.file.Paths
import org.springframework.core.env.Environment
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@SpringBootApplication
@EnableAsync
class Application

fun main(args: Array<String>) {
    val ctx = runApplication<Application>(*args)
    REMOVE_AFTER_MIGRATION().run(ctx)
}

class REMOVE_AFTER_MIGRATION {
    fun run(ctx: ApplicationContext) {
        val filesRepo = ctx.getBean(FileRepository::class.java)
        val fileUtil = ctx.getBean(FileUtil::class.java)

        val files = filesRepo.findAll()

        files.forEach { file ->
            run {
                val targetPath = resolveFileStoragePathForFilenameAndRelativePath(
                    file.storageName,
                    Path.of("studies", file.studyId),
                    fileUtil,
                )
                println(file.id)
            }
        }

    }

    fun copyFile(sourcePath: Path, targetPath: Path) {
        try {
            // Ensure the parent directory of the target path exists
            targetPath.parent?.let { Files.createDirectories(it) }

            // Copy the file from source to target
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: IOException) {
            throw FileStorageException("Failed to copy file: ${e.message}")
        }
    }

    fun resolveFileStoragePathForFilenameAndRelativePath(
        fileName: String,
        relativePath: Path,
        fileUtil: FileUtil,
    ): Path {
        val rootFolder: Path? = Paths.get(fileUtil.filePath.toString()).toAbsolutePath().normalize()
        val path =
            fileUtil.storageDirectory.resolve(
                fileUtil.removeRootPrefix(rootFolder.toString()) + "/" + relativePath + "/" + fileName,
            )
        fileUtil.isDirectoryOrElseCreate(path.parent)

        return path
    }
}

@Component
class SpringApplicationContext : ApplicationContextAware {
    companion object {
        private var context: ApplicationContext? = null

        fun <T> getBean(clazz: Class<T>): T {
            return context!!.getBean(clazz)
        }
    }

    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }
}
