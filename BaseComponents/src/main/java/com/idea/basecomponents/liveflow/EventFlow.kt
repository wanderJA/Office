package com.tencent.bbg.liveflow

import android.os.SystemClock

/**
 * @author qingqixu
 * @email qingqixu@tencent.com
 * @date 2021/11/5 19:30
 * @description EventFlow接口
 **/
internal interface EventFlow<T> {

    /**
     * 获取当前event
     */
    val event: LiveEvent<T>?

    /**
     * 监听接收有序的所有event
     */
    suspend fun <R> collectAll(
        sticky: Boolean = true,
        transform: T?.() -> R?,
        filter: ((R?) -> Boolean)? = null,
        collector: suspend (R?) -> Unit,
    )

    /**
     * 监听并接收有序的最新event
     */
    suspend fun <R> collectLatest(
        sticky: Boolean = true,
        transform: T?.() -> R?,
        filter: ((R?) -> Boolean)? = null,
        collector: suspend (R?) -> Unit,
    )

    //fun collectUnOrdered(sticky: Boolean = true, collector: (T?) -> Unit)

    /**
     * 发送新event，覆盖原event
     */
    fun tryEmit(sticky: Boolean = true, value: T?): Boolean

    /**
     * 更新event，基于原event做局部或全部更新
     */
    fun tryUpdate(sticky: Boolean = true, block: T?.() -> T?): Boolean

}

internal data class LiveEvent<T>(

    /**
     * 是否 sticky event
     */
    val sticky: Boolean = true,

    /**
     * 下一个event
     */
    @Volatile
    var nextEvent: LiveEvent<T>? = null,

    /**
     * event发布或者更新的时间
     */
    val whenMs: Long = SystemClock.elapsedRealtime(),

    /**
     * event携带的数据
     */
    private val value: T? = null,

    /**
     * 延迟创建的数据
     */
    private val lazyValue: Lazy<T?>? = null,
) {

    companion object {
        const val WHEN_NOTHING = -1L
    }

    fun getValue(): T? {
        return value ?: lazyValue?.value
    }

}