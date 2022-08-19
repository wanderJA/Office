package com.idea.office

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Copyright (c) 2022 Tencent. All rights reserved.
 * 类功能描述:
 *
 * @author brestwang
 * @date 2022/7/10
 */
class App: Application() {
    override fun onCreate() {
        super.onCreate()
    }


    companion object {

        inline fun <reified T> Any.toType(): T? {
            return (this as? T)
        }
        @JvmStatic
        fun main(args:Array<String>){
            val type = 10.toType<String>()
            println("type:${type?.substring(1)}")
        }
    }

}