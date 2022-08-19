package com.idea.gwee.flow.bus

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Copyright (c) 2022 TTXS. All rights reserved.
 * 类功能描述:
 *
 * @author brestwang
 * @date 2022/8/16
 */
object FlowBus {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val eventFlowMap = ConcurrentHashMap<Any, Flow<Any>>()


    private fun <T> with(eventKey: Any): Flow<T> {
        MutableSharedFlow<String>().asSharedFlow()
        if (!eventFlowMap.contains(eventKey)) {
            eventFlowMap[eventKey.javaClass.name] = MutableSharedFlow()
        }
        return eventFlowMap[eventKey] as Flow<T>
    }

    fun <T> getBus(eventKey: Any): Flow<T> {
        return with(eventKey)
    }

    fun observe(owner: LifecycleOwner, eventKey: Any) {
        owner.lifecycleScope.launch {


        }

    }

    data class EventMessage<T>(val key: Int, var eventData: T? = null)

}

