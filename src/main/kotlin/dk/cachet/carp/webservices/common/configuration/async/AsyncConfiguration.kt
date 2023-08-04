package dk.cachet.carp.webservices.common.configuration.async

import dk.cachet.carp.webservices.common.exception.async.AsyncExceptionHandler
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

/**
 * The Configuration Class [AsyncConfiguration].
 * The [AsyncConfiguration] implements the [AsyncConfigurer] interface to enable asynchronous functionality for the application.
 */
@Configuration
@EnableAsync
@EnableScheduling
class AsyncConfiguration: AsyncConfigurer
{
    companion object
    {
        private const val MAX_POOL_SIZE = 4
        private const val QUEUE_CAPACITY = 100
    }

    /**
     * The function [getAsyncExecutor] implements the [Executor] interface for an extensible thread pool implementation.
     * @return The [ThreadPoolTaskExecutor] initialized.
     */
    @Bean(value = ["asyncExecutor"])
    override fun getAsyncExecutor(): Executor
    {
        val threadPoolTaskExecutor = ThreadPoolTaskExecutor()
        val cores = Runtime.getRuntime().availableProcessors()

        // Minimum parallel threads that can run at same time
        threadPoolTaskExecutor.corePoolSize = cores

        // Maximum parallel threads that can run at same time
        threadPoolTaskExecutor.maxPoolSize = cores * MAX_POOL_SIZE

        // Queue is using when all core pool are filled.
        threadPoolTaskExecutor.setQueueCapacity(cores * QUEUE_CAPACITY)

        // The thread invokes itself on rejected pool (increasing queue capacity).
        threadPoolTaskExecutor.setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())

        // Set [true] to wait for scheduled tasks to complete on shutdown,
        // not interrupting running tasks and executing all tasks in the queue.
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true)

        threadPoolTaskExecutor.setThreadNamePrefix("async-")

        threadPoolTaskExecutor.initialize()
        return threadPoolTaskExecutor
    }

    /**
     * The function [getAsyncUncaughtExceptionHandler] retrieves the uncaught exceptions thrown from asynchronous methods.
     * @return The [AsyncExceptionHandler].
     */
    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler
    {
        return AsyncExceptionHandler()
    }

    /**
     * The function [taskScheduler] initializes a new [ConcurrentTaskScheduler], using a single thread executor as default.
     * @returns The [ConcurrentTaskScheduler] created.
     */
    @Bean
    fun taskScheduler(): TaskScheduler
    {
        return ConcurrentTaskScheduler()
    }
}