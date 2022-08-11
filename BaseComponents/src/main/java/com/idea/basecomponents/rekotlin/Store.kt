package com.tencent.bbg.rekotlin

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.tencent.bbg.liveflow.LiveFlowBus
import com.tencent.bbg.liveflow.LocalLiveFlowBus
import java.io.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * see detail in {@link https://github.com/ReKotlin/ReKotlin}
 * - 由于 FlowRedux 在 dispatch action 和 update state 的时候各进行了一次线程切换（或 post 操作），
 * - 部分场景下要求在同一线程（主线程执行），考虑已有部分业务使用 FlowRedux，这里不便进行修改，
 * - 另考虑到 ReKotlin 在整体设计上更为合理，故重新引入，后期再统一
 */

typealias DispatchCallback<State> = (State) -> Unit

typealias ActionCreator<State, Store> = (state: State, store: Store) -> Action?

typealias AsyncActionCreator<State, Store> =
            (state: State, store: Store, actionCreatorCallback: (ActionCreator<State, Store>) -> Unit) -> Unit

class Store<State : StateType>(
    private val reducer: Reducer<State>,
    private val initialState: State,
    middleware: List<Middleware<State>> = emptyList(),
    private val dispatchScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : StoreType<State>, Closeable {

    private val stateClass = initialState.javaClass

    private val eventFlowBus = LocalLiveFlowBus()

    private val stateFlowBus = LocalLiveFlowBus().apply {
        dispatch(true, stateClass, initialState)
        configureDispatchScope(stateClass, dispatchScope)
    }

    @Suppress("NAME_SHADOWING")
    private val dispatchFunction: DispatchFunction = middleware
        .reversed()
        .fold(
            initial = initial@{ action: Action ->
                this.defaultDispatch(action)
                return@initial false
            },
            operation = { dispatchFunction, middleware ->
                val dispatch = dispatch@{ action: Action ->
                    this.dispatch(action)
                    return@dispatch false
                }
                val getState = { this.state }
                middleware(dispatch, getState)(dispatchFunction)
            }
        )

    private fun defaultDispatch(action: Action) {
        stateFlowBus.update(true, stateClass) {
            reducer(action, this ?: initialState)
        }
    }

    override val state: State
        get() {
            return stateFlowBus.valueOf(stateClass) ?: initialState
        }

    override fun dispatch(action: Action) {
        this.dispatchFunction(action)
    }

    override fun dispatch(actionCreator: ActionCreator<State, StoreType<State>>) {
        actionCreator(this.state, this)?.let {
            this.dispatch(it)
        }
    }

    override fun dispatch(asyncActionCreator: AsyncActionCreator<State, StoreType<State>>) {
        this.dispatch(asyncActionCreator, null)
    }

    override fun dispatch(
        asyncActionCreator: AsyncActionCreator<State, StoreType<State>>,
        callback: DispatchCallback<State>?
    ) {
        asyncActionCreator(this.state, this) { actionProvider ->
            actionProvider(this.state, this)?.let {
                this.dispatch(it)
                callback?.invoke(this.state)
            }
        }
    }

    override fun getSharedFlow(sticky: Boolean): Flow<State> {
        return stateFlowBus.sharedFlowOf(sticky, stateClass)
            .filterNotNull()
            .flowOn(Dispatchers.Unconfined)
    }

    override fun <Field> getSharedFlow(sticky: Boolean, block: State.() -> Field): Flow<Field> {
        return stateFlowBus.sharedFlowOf(sticky, stateClass)
            .filterNotNull()
            .map { it.block() }
            .flowOn(Dispatchers.Unconfined)
    }

    override fun getStateFlow(sticky: Boolean): Flow<State> {
        return stateFlowBus.stateFlowOf(sticky, stateClass)
            .filterNotNull()
            .distinctUntilChanged()
            .flowOn(Dispatchers.Unconfined)
    }

    override fun <Field> getStateFlow(sticky: Boolean, block: State.() -> Field): Flow<Field> {
        return stateFlowBus.stateFlowOf(sticky, stateClass)
            .filterNotNull()
            .map { it.block() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Unconfined)
    }

    override fun getLiveData(sticky: Boolean): LiveData<State> {
        return getStateFlow(sticky).asLiveData()
    }

    override fun <Field> getLiveData(sticky: Boolean, block: State.() -> Field): LiveData<Field> {
        return getStateFlow(sticky, block).asLiveData()
    }

    override fun getLiveFlowBus(): LiveFlowBus {
        return eventFlowBus
    }

    override fun close() {
        dispatchScope.cancel()
    }

}
