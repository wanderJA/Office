package com.tencent.bbg.rekotlin

import androidx.lifecycle.LiveData
import com.tencent.bbg.liveflow.LiveFlowBus
import kotlinx.coroutines.flow.Flow

/**
 * Defines the interface of Stores in ReKotlin. `Store` is the default implementation of this
 * interface. Applications have a single store that stores the entire application state.
 * Stores receive actions and use reducers combined with these actions, to calculate state changes.
 * Upon every state update a store informs all of its subscribers.
 */
interface StoreType<State : StateType> : DispatchingStoreType {

    /**
     * The current state stored in the store.
     */
    val state: State

    /**
     * The main dispatch function that is used by all convenience `dispatch` methods.
     * This dispatch function can be extended by providing middlewares.
     */
    //val dispatchFunction: DispatchFunction

    /**
     * Dispatches an action creator to the store. Action creators are functions that generate
     * actions. They are called by the store and receive the current state of the application
     * and a reference to the store as their input.
     *
     * Based on that input the action creator can either return an action or not. Alternatively
     * the action creator can also perform an asynchronous operation and dispatch a new action
     * at the end of it.
     *
     * Example of an action creator:
     * <pre>
     * <code>
     * func deleteNote(noteID: Int) -> ActionCreator {
     *     return { state, store in
     *         // only delete note if editing is enabled
     *         if (state.editingEnabled == true) {
     *             return NoteDataAction.DeleteNote(noteID)
     *         } else {
     *             return nil
     *         }
     *      }
     * }
     * </code>
     * </pre>
     *
     * This action creator can then be dispatched as following:
     * <pre>
     * <code>
     * store.dispatch( noteActionCreatore.deleteNote(3) )
     * </code>
     * </pre>
     *
     * @return: By default returns the dispatched action, but middlewares can change the
     * return type, e.g. to return promises
     */
    fun dispatch(actionCreator: ActionCreator<State, StoreType<State>>)

    /**
     * Dispatches an async action creator to the store. An async action creator generates an
     * action creator asynchronously.
     */
    fun dispatch(asyncActionCreator: AsyncActionCreator<State, StoreType<State>>)

    /**
     * Dispatches an async action creator to the store. An async action creator generates an
     * action creator asynchronously. Use this method if you want to wait for the state change
     * triggered by the asynchronously generated action creator.
     *
     * This overloaded version of `dispatch` calls the provided `callback` as soon as the
     * asynchronoously dispatched action has caused a new state calculation.
     *
     * If the ActionCreator does not dispatch an action, the callback block will never
     * be called
     */
    fun dispatch(asyncActionCreator: AsyncActionCreator<State, StoreType<State>>, callback: DispatchCallback<State>?)

    /**
     * 接收有序且连续的事件
     */
    fun getSharedFlow(sticky: Boolean = true): Flow<State>

    /**
     * 接收有序且连续的事件
     */
    fun <Field> getSharedFlow(sticky: Boolean = true, block: State.() -> Field): Flow<Field>

    /**
     * 接收有序的最新事件
     */
    fun getStateFlow(sticky: Boolean = true): Flow<State>

    /**
     * 接收有序的最新事件
     */
    fun <Field> getStateFlow(sticky: Boolean = true, block: State.() -> Field): Flow<Field>

    fun getLiveData(sticky: Boolean = true): LiveData<State>

    fun <Field> getLiveData(sticky: Boolean = true, block: State.() -> Field): LiveData<Field>

    fun getLiveFlowBus(): LiveFlowBus

}