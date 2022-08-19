package com.idea.gwee.flow.bus

/**
 * Copyright (c) 2022 TTXS. All rights reserved.
 * 类功能描述:
 *
 * @author brestwang
 * @date 2022/8/16
 */
class  EventMessage<T> {
    val key:Int =-1
    var eventData:T? = null
}