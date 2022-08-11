package com.tencent.bbg.liveflow

import android.os.SystemClock
import com.tencent.bbg.liveflow.LiveEvent.Companion.WHEN_NOTHING
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * @author qingqixu
 * @email qingqixu@tencent.com
 * @date 2021/11/5 14:04
 * @description MutableEventFlow
 *
 * 使用单向链表重新实现了 StateFlowImpl 和 SharedFlowImpl 的部分功能(精简):
 * 1/ 同时支持接收 【有序且连续的事件】 和 【有序但不连续的事件即最新事件】，由接收端根据需要自由选择
 * 2/ 使用单向链表，简化 GC 问题
 * 3/ 精简部分(暂时)无用功能，减少线程锁
 *
 **/
internal open class MutableEventFlow<T>(
    private val updateImmediately: Boolean = true // 是否立即执行 update 逻辑
) : EventFlow<T> {

    companion object {
        private const val TAG = "MutableEventFlow"
        private const val NO_CHANGES = 0
    }

    private val liveSlots = CopyOnWriteArrayList<LiveSlot<T>>()

    private val currentEvent = AtomicReference<LiveEvent<T>>(
        LiveEvent(
            sticky = false,
            whenMs = WHEN_NOTHING
        )
    )

    private val sequence = AtomicInteger(NO_CHANGES)

    //override val value: T? = currentEvent.get()?.value

    override val event: LiveEvent<T>?
        get() = currentEvent.get()

    override suspend fun <R> collectAll(
        sticky: Boolean,
        transform: T?.() -> R?,
        filter: ((R?) -> Boolean)?,
        collector: suspend (R?) -> Unit,
    ) {
        val slot = allocateSlot()
        try {
            val collectTimeMs = SystemClock.elapsedRealtime()
            val collectorJob = currentCoroutineContext()[Job]
            while (true) {
                val oldEvent = slot.event.get()
                val newEvent = if (oldEvent === null) { // only in the first time
                    currentEvent.get()
                } else {
                    oldEvent.nextEvent
                }

                collectorJob?.ensureActive()
                if (newEvent != null) {
                    if (newEvent.whenMs >= collectTimeMs || (sticky && newEvent.sticky)) {
                        val value = transform.invoke(newEvent.getValue())
                        if (filter == null || filter.invoke(value)) {
                            collector.invoke(value)
                        }
                    }
                    slot.event.set(newEvent)
                } else {
                    //slot.event.set(oldEvent)
                    if (slot.takePending().not()) {
                        slot.awaitValue()
                    }
                }
            }
        } finally {
            freeSlot(slot)
        }
    }

    override suspend fun <R> collectLatest(
        sticky: Boolean,
        transform: T?.() -> R?,
        filter: ((R?) -> Boolean)?,
        collector: suspend (R?) -> Unit,
    ) {
        val slot = allocateSlot()
        try {
            val collectTimeMs = SystemClock.elapsedRealtime()
            val collectorJob = currentCoroutineContext()[Job]
            while (true) {
                val oldEvent = slot.event.get()
                val newEvent = currentEvent.get()

                collectorJob?.ensureActive()
                if (oldEvent != newEvent) {
                    if (newEvent.whenMs >= collectTimeMs || (sticky && newEvent.sticky)) {
                        val value = transform.invoke(newEvent.getValue())
                        if (filter == null || filter.invoke(value)) {
                            collector.invoke(value)
                        }
                    }
                    slot.event.set(newEvent)
                }
                if (slot.takePending().not()) {
                    slot.awaitValue()
                }
            }
        } finally {
            freeSlot(slot)
        }
    }

    //override fun collectUnOrdered(sticky: Boolean, collector: (T?) -> Unit) { }

    override fun tryEmit(sticky: Boolean, value: T?): Boolean {
        var newEvent: LiveEvent<T>
        var lastEvent: LiveEvent<T>?
        while (true) {
            lastEvent = currentEvent.get()
            newEvent = LiveEvent(
                sticky = sticky,
                value = value
            )
            if (currentEvent.compareAndSet(lastEvent, newEvent)) {
                lastEvent?.nextEvent = newEvent
                break
            }
        }

        notifyChanges()
        return true
    }

    override fun tryUpdate(sticky: Boolean, block: T?.() -> T?): Boolean {
        var newEvent: LiveEvent<T>
        var lastEvent: LiveEvent<T>?
        while (true) {
            lastEvent = currentEvent.get()
            newEvent = LiveEvent(
                sticky = sticky,
                lazyValue = lazy(LazyThreadSafetyMode.PUBLICATION) {
                    lastEvent.getValue().block()
                }
            )
            if (currentEvent.compareAndSet(lastEvent, newEvent)) {
                lastEvent?.nextEvent = newEvent
                if (updateImmediately) {
                    newEvent.getValue()
                }
                break
            }
        }

        notifyChanges()
        return true
    }

    private fun notifyChanges() {
        if (sequence.getAndIncrement() == NO_CHANGES) {
            while (true) {
                val changes = sequence.get()
                val event = currentEvent.get()
                try {
                    liveSlots.forEach { it.makePending(event) }
                } finally {
                    if (sequence.compareAndSet(changes, NO_CHANGES)) {
                        break
                    }
                }
            }
        }
    }

    private fun allocateSlot(): LiveSlot<T> {
        return LiveSlot<T>().also {
            liveSlots.add(it)
        }
    }

    private fun freeSlot(slot: LiveSlot<T>) {
        slot.state.set(null)
        liveSlots.remove(slot)
    }

    @Suppress("UNCHECKED_CAST")
    private fun LiveSlot<T>.makePending(event: LiveEvent<T>) {
        while (true) {
            if (this.event.get() === event) {
                return
            }

            when (val state = this.state.get()) {
                EMITTING -> {
                    if (this.state.compareAndSet(EMITTING, PENDING)) {
                        return
                    }
                }
                is Continuation<*> -> {
                    if (this.state.compareAndSet(state, EMITTING)) {
                        (state as Continuation<Unit>).resume(Unit)
                    }
                }
                else -> {
                    return
                }
            }
        }
    }

    private fun LiveSlot<T>.takePending(): Boolean {
        return this.state.getAndSet(EMITTING) === PENDING
    }

    private suspend fun LiveSlot<T>.awaitValue(): Unit = suspendCancellableCoroutine { cancellableContinuation ->
        if (this.state.compareAndSet(EMITTING, cancellableContinuation)) {
            return@suspendCancellableCoroutine
        }
        cancellableContinuation.resumeWith(Result.success(Unit))
    }

}

private val EMITTING = Any()

private val PENDING = Any()

private data class LiveSlot<T>(
    /**
     * 当前的状态
     */
    val state: AtomicReference<Any> = AtomicReference(EMITTING),
    /**
     * 已经接收的事件
     */
    val event: AtomicReference<LiveEvent<T>?> = AtomicReference(null)
)