package com.idea.basecomponents.liveflow

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

/**
 * @author qingqixu
 * @email qingqixu@tencent.com
 * @date 2021/10/17 14:20
 * @description LiveViewModel
 **/
@Deprecated("Will be removed in the future.")
interface LiveViewModel<S> {

    val liveFlowBus: LiveFlowBus

    /**
     * 接收有序且连续的事件
     */
    fun getSharedFlow(sticky: Boolean = true): Flow<S>

    /**
     * 接收有序且连续的事件
     */
    fun <R> getSharedFlow(sticky: Boolean = true, block: S.() -> R): Flow<R>

    /**
     * 接收有序的最新事件
     */
    fun getStateFlow(sticky: Boolean = true): Flow<S>

    /**
     * 接收有序的最新事件
     */
    fun <R> getStateFlow(sticky: Boolean = true, block: S.() -> R): Flow<R>

    fun getLiveData(sticky: Boolean = true): LiveData<S>

    fun <R> getLiveData(sticky: Boolean = true, block: S.() -> R): LiveData<R>

    fun getLiveValue(): S

    fun dispatchLiveValue(sticky: Boolean = true, value: S)

    fun updateLiveValue(sticky: Boolean = true, block: S.() -> S)

}