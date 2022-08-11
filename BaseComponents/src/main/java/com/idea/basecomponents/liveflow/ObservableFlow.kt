package com.tencent.bbg.liveflow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

/**
 * @author qingqixu
 * @email qingqixu@tencent.com
 * @date 2022/1/26
 * @description ObservableFlow
 **/

/**
 * 注册观察者，并绑定生命周期
 * 如果emit线程是主线程则直接执行; 如果emit线程不是主线程，则post到主线程执行
 */
fun <T> Flow<T>.onChangeInMainImmediate(
    lifecycleScope: CoroutineScope,
    action: suspend (previous: T?, current: T) -> Unit
) {
    this.observe(
        context = Dispatchers.Main.immediate,
        lifecycleScope = lifecycleScope,
        action = ObservableActionImpl(action)
    )
}

private class ObservableActionImpl<T>(
    @JvmField val observer: suspend (old: T?, new: T) -> Unit
): suspend (T) -> Unit {

    private var previous: T? = null

    override suspend fun invoke(current: T) {
        if (previous != current) {
            previous = current
            observer(previous, current)
        }
    }
}