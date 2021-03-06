package org.hoshino9.anti.indulged.core

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class DefaultAntiIndulged(val clock: Clock, val factory: ReminderFactory) : AntiIndulged, CoroutineScope {
    private var timer: Job? = null

    @Synchronized
    override fun startTiming() {
        println("Trying to start timing...")

        if (timer?.isActive == true) {
            return
        }

        timer = launch(this.coroutineContext) {
            while (isActive) {
                val clock = coroutineContext[Clock.Key] ?: throw NoSuchElementException("Clock.Key not found")

                println(clock)
                val reminder = factory.newInstance(clock.rest)

                if (reminder.shouldClose) {
                    stopTiming()
                    reminder.remind()
                    break
                }

                clock.increase()
                reminder.remind()

                delay(clock.cycle)
            }
        }

        println("Started")
    }

    @Synchronized
    override fun stopTiming() {
        println("Trying to stop timing")

        val timer = timer ?: return

        if (!timer.isActive) {
            return
        }

        timer.cancel()

        println("Stopped")
    }

    suspend fun join() {
        if (timer?.isActive == true) {
            timer?.join()
        }
    }

    override val coroutineContext: CoroutineContext
        get() = clock

    companion object {
        const val MINUTES_1: Long = 60 * 1000
        const val CYCLE: Long = MINUTES_1
        const val MINUTES_10: Long = 10 * MINUTES_1
        const val MINUTES_5: Long = 5 * MINUTES_1
        const val MINUTES_0: Long = 0
    }
}