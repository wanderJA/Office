package com.tencent.bbg.liveflow

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.tencent.bbg.logger.Logger
import com.tencent.bbg.raftwrapper.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * @author qingqixu
 * @email qingqixu@tencent.com
 * @date 2021/10/12 09:51
 * @description kotlin flow 实现局部事件总线
 * 1/ 支持发送 sticky/normal event
 * 2/ 支持注册 sticky/normal event
 * 3/ 减少不必要的线程切换
 * 4/ 支持自动回收 StateFlow(无 sticky event 及 observers)
 * 5/ 支持监听 event 中的某个具体属性，避免事件过多不便管理
 * 6/ 支持 flow 的各种特性，如通过 flowOn 自由切换线(协)程、自动绑定生命周期、链式调用等
 **/
class LocalLiveFlowBus(
    /**
     * event 分发线程，默认不切换线程
     */
    private val defaultDispatchScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
    /**
     * 是否自动回收，无 sticky event 及 observer 的时候自动回收 flow
     */
    private val autoClear: Boolean = false

) : LiveFlowBus {

    companion object {
        private const val TAG = "LocalLiveFlowBus"
    }

    private val lock = Any()
    private val eventFlowMap = HashMap<Any, RecyclableEventFlow<*>>()
    private val dispatchScopeMap = HashMap<Any, CoroutineScope>()

    override fun <T> configureDispatchScope(clazz: Class<T>, scope: CoroutineScope?) {
        dispatchScopeMap.apply {
            if (scope == null) {
                remove(clazz)
            } else {
                put(clazz, scope)
            }
        }
    }

    override fun <T> sharedFlowOf(
        sticky: Boolean,
        clazz: Class<T>,
        filter: ((T?) -> Boolean)?
    ): Flow<T?> {
        return getSharedFlowOf(sticky, clazz, { this }, filter)//.distinctUntilChanged()
    }

    override fun <T, R> sharedFlowOf(
        sticky: Boolean,
        clazz: Class<T>,
        transform: T?.() -> R?,
        filter: ((R?) -> Boolean)?
    ): Flow<R?> {
        return getSharedFlowOf(sticky, clazz, transform, filter)//.distinctUntilChanged()
    }

    override fun <T> stateFlowOf(
        sticky: Boolean,
        clazz: Class<T>,
        filter: ((T?) -> Boolean)?
    ): Flow<T?> {
        return getStateFlowOf(sticky, clazz, { this }, filter)//.distinctUntilChanged()
    }

    override fun <T, R> stateFlowOf(
        sticky: Boolean,
        clazz: Class<T>,
        transform: T?.() -> R?,
        filter: ((R?) -> Boolean)?
    ): Flow<R?> {
        return getStateFlowOf(sticky, clazz, transform, filter)//.distinctUntilChanged()
    }

    override fun <T> liveDataOf(
        sticky: Boolean,
        clazz: Class<T>,
        filter: ((T?) -> Boolean)?
    ): LiveData<T?> {
        return getStateFlowOf(sticky, clazz, { this }, filter).distinctUntilChanged().asLiveData()
    }

    override fun <T, R> liveDataOf(
        sticky: Boolean,
        clazz: Class<T>,
        transform: T?.() -> R?,
        filter: ((R?) -> Boolean)?
    ): LiveData<R?> {
        return getStateFlowOf(sticky, clazz, transform, filter).distinctUntilChanged().asLiveData()
    }

    override fun <T : Any> dispatch(sticky: Boolean, value: T): Job {
        return dispatch(sticky, value.javaClass, value)
    }

    override fun <T> dispatch(sticky: Boolean, clazz: Class<T>, value: T?): Job {
        return dispatchScopeOf(clazz).launch {
            // 这里暂时不触发自动回收逻辑，避免出现创建之后又马上被回收的情况
            withEventFlowOf(clazz, false /* sticky.not() */) {
                if (isActive.not()) {
                    return@withEventFlowOf
                }
                debug { "try emit $value" }
                tryEmit(sticky, value)
            }
        }
    }

    override fun <T> update(sticky: Boolean, clazz: Class<T>, block: T?.() -> T?): Job {
        return dispatchScopeOf(clazz).launch {
            // 同上，这里暂时不触发自动回收逻辑，避免出现创建之后又马上被回收的情况
            withEventFlowOf(clazz, false /* sticky.not() */) {
                if (isActive.not()) {
                    return@withEventFlowOf
                }
                debug { "try update" }
                tryUpdate(sticky, block)
            }
        }
    }

    override fun <T> valueOf(clazz: Class<T>): T? {
        return getEventFlowOrNull(clazz)?.event?.getValue()
    }

    /**
     * 暂时不对外开放，建议使用 autoClear 功能
     */
    private fun clear() {
        debug { "clear all cache" }
        eventFlowMap.clear()
        dispatchScopeMap.clear()
    }

    /**
     * 创建有序且连续的Flow
     */
    private fun <T, R> getSharedFlowOf(
        sticky: Boolean,
        clazz: Class<T>,
        transform: T?.() -> R?,
        filter: ((R?) -> Boolean)? = null
    ): Flow<R?> {
        return flow {
            withEventFlowOf(clazz, true) {
                collectAll(sticky = sticky, transform = transform, filter = filter) {
                    this@flow.emit(it)
                }
            }
        }//.flowOn(Dispatchers.Unconfined)
    }

    /**
     * 创建有序但不连续的Flow(发送最新值)
     */
    private fun <T, R> getStateFlowOf(
        sticky: Boolean,
        clazz: Class<T>,
        transform: T?.() -> R?,
        filter: ((R?) -> Boolean)? = null
    ): Flow<R?> {
        return flow {
            withEventFlowOf(clazz, true) {
                collectLatest(sticky = sticky, transform = transform, filter = filter) {
                    this@flow.emit(it)
                }
            }
        }//.flowOn(Dispatchers.Unconfined)
    }

    /**
     * 获取 EventFlow，可为空
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> getEventFlowOrNull(clazz: Class<T>): EventFlow<T>? {
        return eventFlowMap[clazz] as? EventFlow<T>
    }

    private fun <T> createEventFlow(): RecyclableEventFlow<T> {
        return LiveEventFlow()
    }

    /**
     * 获取 EventFlow，如果 EventFlow 为空或已回收 则创建一个新的对象
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> obtainEventFlow(clazz: Class<T>): RecyclableEventFlow<T> {
        eventFlowMap[clazz]?.tryObtain()?.let {
            return it as RecyclableEventFlow<T>
        }

        synchronized(lock) {
            eventFlowMap[clazz]?.tryObtain()?.let {
                return it as RecyclableEventFlow<T>
            }

            return createEventFlow<T>().also {
                it.obtain()
                eventFlowMap[clazz] = it
            }
        }
    }

    /**
     * 通过引用计数的方式操作 EventFlow，如果 EventFlow 为空或已回收 则创建一个新的对象
     */
    private inline fun <T> withEventFlowOf(
        clazz: Class<T>,
        tryClear: Boolean,
        block: EventFlow<T>.() -> Unit
    ) {
        val flow = obtainEventFlow(clazz)
        try {
            flow.block()
        } finally {
            flow.free()
            // 预先check一次，减少冗余的锁操作
            if (tryClear
                && autoClear
                && flow.isRecyclable()
                && flow.event?.sticky != true
            ) {
                tryClearEventFlow(clazz)
            }
        }
    }

    /**
     * 尝试回收(清除) StateFlow
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> tryClearEventFlow(clazz: Class<T>) {
        if (autoClear.not()) {
            return
        }

        synchronized(lock) {
            val flow = eventFlowMap[clazz] as? RecyclableEventFlow<T> ?: return
            flow.tryRecycle { // 没有在发送 event，也没有 observer
                if (event?.sticky != true) { // 无 sticky event
                    eventFlowMap.remove(clazz)
                    debug { "clear EventFlow: $this" }
                    true
                } else {
                    false
                }
            }
        }
    }

    /**
     * event 分发线程，默认不切换线程
     */
    private fun <T> dispatchScopeOf(clazz: Class<T>): CoroutineScope {
        return dispatchScopeMap[clazz] ?: defaultDispatchScope
    }

    /**
     * 输出调试信息
     */
    private inline fun debug(crossinline block: () -> String) {
        Logger.d(TAG, block())
    }

}