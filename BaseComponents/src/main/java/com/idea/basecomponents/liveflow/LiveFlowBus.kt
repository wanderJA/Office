package com.tencent.bbg.liveflow

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

/**
 * @author qingqixu
 * @email qingqixu@tencent.com
 * @date 2021/10/12 09:53
 * @description kotlin flow 实现事件总线接口
 **/
interface LiveFlowBus {

    /**
     * 配置事件的发布协程，可以通过该方法将发布逻辑统一切换到同一个协程
     */
    fun <T> configureDispatchScope(clazz: Class<T>, scope: CoroutineScope?)

    /**
     * 接收有序的最新的事件
     */
    fun <T> liveDataOf(
        sticky: Boolean = true,
        clazz: Class<T>,
        filter: ((T?) -> Boolean)? = null
    ): LiveData<T?>

    /**
     * 接收有序的最新的事件
     */
    fun <T, R> liveDataOf(
        sticky: Boolean = true,
        clazz: Class<T>,
        transform: T?.() -> R?,
        filter: ((R?) -> Boolean)? = null
    ): LiveData<R?>

    /**
     * 最新的事件
     */
    fun <T> valueOf(clazz: Class<T>): T?

    /**
     * 发布新事件
     */
    fun <T : Any> dispatch(sticky: Boolean = true, value: T): Job

    /**
     * 发布新事件
     */
    fun <T> dispatch(sticky: Boolean = true, clazz: Class<T>, value: T?): Job

    /**
     * 更新事件
     */
    fun <T> update(sticky: Boolean = true, clazz: Class<T>, block: T?.() -> T?): Job

    /**
     * 接收有序且连续的事件
     */
    fun <T> sharedFlowOf(
        sticky: Boolean,
        clazz: Class<T>,
        filter: ((T?) -> Boolean)? = null
    ): Flow<T?>

    /**
     * 接收有序且连续的事件
     */
    fun <T, R> sharedFlowOf(
        sticky: Boolean,
        clazz: Class<T>,
        transform: T?.() -> R?,
        filter: ((R?) -> Boolean)? = null
    ): Flow<R?>

    /**
     * 接收有序的最新事件
     */
    fun <T> stateFlowOf(
        sticky: Boolean,
        clazz: Class<T>,
        filter: ((T?) -> Boolean)? = null
    ): Flow<T?>

    /**
     * 接收有序的最新的事件
     */
    fun <T, R> stateFlowOf(
        sticky: Boolean,
        clazz: Class<T>,
        transform: T?.() -> R?,
        filter: ((R?) -> Boolean)? = null
    ): Flow<R?>

}