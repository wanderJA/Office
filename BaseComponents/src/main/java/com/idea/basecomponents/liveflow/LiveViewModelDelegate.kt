package com.idea.basecomponents.liveflow

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.idea.basecomponents.liveflow.observeInBackground
import com.idea.basecomponents.liveflow.observeInComputation
import com.idea.basecomponents.liveflow.observeInIO
import com.idea.basecomponents.liveflow.observeInMainImmediate
import com.idea.basecomponents.liveflow.observeUnconfined
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

/**
 * @author qingqixu
 * @email qingqixu@tencent.com
 * @date 2021/10/17 14:13
 * @description LiveViewModelDelegate
 **/
@Deprecated("Will be removed in the future.")
open class LiveViewModelDelegate<S : Any> constructor(
    private val initialState: S,
    private val liveFlowBusProvider: Lazy<LiveFlowBus>? = null
) : ViewModel(), LiveViewModel<S> {

    private val defaultDispatchScope = CoroutineScope(Dispatchers.Unconfined)

    private val _liveFlowBus by lazy {
        liveFlowBusProvider?.value ?: LocalLiveFlowBus(
            defaultDispatchScope = defaultDispatchScope,
            autoClear = false
        )
    }

    private val stateClass = initialState.javaClass

    init {
        _liveFlowBus.apply {
            configureDispatchScope(stateClass, viewModelScope)
            dispatch(sticky = true, value = initialState)
        }
    }

    override fun onCleared() {
        super.onCleared()
        defaultDispatchScope.cancel()
    }

    final override val liveFlowBus = _liveFlowBus

    override fun getSharedFlow(sticky: Boolean): Flow<S> {
        return liveFlowBus.sharedFlowOf(sticky, stateClass).filterNotNull()
    }

    override fun <R> getSharedFlow(sticky: Boolean, block: S.() -> R): Flow<R> {
        return getSharedFlow(sticky).map { it.block() }
    }

    override fun getStateFlow(sticky: Boolean): Flow<S> {
        return liveFlowBus.stateFlowOf(sticky, stateClass).filterNotNull()
    }

    override fun <R> getStateFlow(sticky: Boolean, block: S.() -> R): Flow<R> {
        return getStateFlow(sticky).map { it.block() }
    }

    override fun getLiveData(sticky: Boolean): LiveData<S> {
        return getStateFlow(sticky).distinctUntilChanged().asLiveData()
    }

    override fun <R> getLiveData(sticky: Boolean, block: S.() -> R): LiveData<R> {
        return getStateFlow(sticky, block).distinctUntilChanged().asLiveData()
    }

    override fun getLiveValue(): S {
        return liveFlowBus.valueOf(stateClass) ?: initialState
    }

    override fun dispatchLiveValue(sticky: Boolean, value: S) {
        liveFlowBus.dispatch(sticky, stateClass, value)
    }

    override fun updateLiveValue(sticky: Boolean, block: S.() -> S) {
        liveFlowBus.update(sticky, stateClass) {
            this?.block() ?: initialState.block()
        }
    }

    /**
     * 注册观察者，并绑定生命周期，在主线程执行
     */
    protected fun <T> Flow<T>.observe(action: suspend (value: T) -> Unit) {
        observeInMainImmediate(viewModelScope, action)
    }

    /**
     * 注册观察者，并绑定生命周期，在主线程执行
     */
    protected fun <T> Flow<T>.observeInMain(action: suspend (value: T) -> Unit) {
        observeInMainImmediate(viewModelScope, action)
    }

    /**
     * 注册观察者，并绑定生命周期，在异步线程执行
     */
    protected fun <T> Flow<T>.observeInBackground(action: suspend (value: T) -> Unit) {
        observeInBackground(viewModelScope, action)
    }

    /**
     * 注册观察者，并绑定生命周期，在 IO 线程执行
     */
    protected fun <T> Flow<T>.observeInIO(action: suspend (value: T) -> Unit) {
        observeInIO(viewModelScope, action)
    }

    /**
     * 注册观察者，并绑定生命周期，在 Computation 线程执行
     */
    protected fun <T> Flow<T>.observeInComputation(action: suspend (value: T) -> Unit) {
        observeInComputation(viewModelScope, action)
    }

    /**
     * 注册观察者，并绑定生命周期，不切换线程
     */
    protected fun <T> Flow<T>.observeUnconfined(action: suspend (value: T) -> Unit) {
        observeUnconfined(viewModelScope, action)
    }

}