package dk.cachet.carp.webservices

import dk.cachet.carp.webservices.file.repository.FileRepository
import dk.cachet.carp.webservices.file.util.FileUtil
import org.apache.logging.log4j.LogManager
import org.springframework.beans.BeansException
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@SpringBootApplication
@EnableAsync
class Application

fun main(args: Array<String>) {
    val ctx = runApplication<Application>(*args)
    REMOVE_AFTER_MIGRATION().run(ctx)
}

@Suppress("ClassNaming")
class REMOVE_AFTER_MIGRATION {
    fun run(ctx: ApplicationContext) {
        LogManager.getLogger().info(
            ">-----------------------------------------------------Starting migration " +
                "https://github.com/cph-cachet/carp-webservices-spring/issues/195",
        )
        val filesRepo = ctx.getBean(FileRepository::class.java)
        val fileUtil = ctx.getBean(FileUtil::class.java)
        val environment = ctx.getBean(Environment::class.java)

        if (environment.activeProfiles.get(0) != ("development")) {
            LogManager.getLogger().info(
                ">----------------------------------Migration skipped as its only meant for development environment",
            )
            return
        }

        if (fileUtil.isDirectory(fileUtil.filePath.resolve("studies"))) {
            LogManager.getLogger().info(">----------------------------------Migration already completed previously")
            return
        }

        val files = filesRepo.findAll()

        files.forEach { file ->
            run {
                val targetPath =
                    resolveFileStoragePathForFilenameAndRelativePath(
                        file.storageName,
                        Path.of("studies", file.studyId),
                        fileUtil,
                    )

                val sourcePath = fileUtil.resolveFileStorage(file.storageName)

                LogManager.getLogger().info("copyFileStart" + file.id)
                copyFile(sourcePath, targetPath)
                LogManager.getLogger().info("copyFileEnd" + file.id)

                LogManager.getLogger().info("deleteFileStart" + file.id)
                deleteFile(sourcePath)
                LogManager.getLogger().info("deleteFileEnd" + file.id)
            }
        }

        LogManager.getLogger().info(">-----------------------------------------------------Migration completed")
    }

    fun copyFile(
        sourcePath: Path,
        targetPath: Path,
    ) {
        try {
            // Ensure the parent directory of the target path exists
            targetPath.parent?.let { Files.createDirectories(it) }

            // Copy the file from source to target
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: IOException) {
            LogManager.getLogger().info("Failed to copy file: ${e.message}")
        }
    }

    fun deleteFile(file: Path) {
        try {
            Files.delete(file)
        } catch (e: IOException) {
            LogManager.getLogger().info("Failed to delete file: ${e.message}")
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
