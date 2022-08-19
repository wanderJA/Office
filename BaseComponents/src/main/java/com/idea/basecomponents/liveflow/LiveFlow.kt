package com.idea.basecomponents.liveflow

import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author qingqixu
 * @email qingqixu@tencent.com
 * @date 2021/10/12 10:15
 * @description kotlin flow 线程切换拓展方法
 **/

private const val TAG = "LiveFlowBus"

/**
 * 判断当前线程是否是主线程
 */
private fun isInMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()

/**
 * 注册观察者，并绑定生命周期
 * 默认不进行线程切换，在emit线程执行
 * @param context 回调运行的线程
 * @param lifecycleScope 绑定的生命周期
 * @param action 回调代码
 */
fun <T> Flow<T>.observe(
    context: CoroutineContext = Dispatchers.Unconfined,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    lifecycleScope: CoroutineScope,
    action: suspend (value: T) -> Unit
) {
    lifecycleScope.launch(context, start) {
        try {
            this@observe.collect {
                if (isActive) {
                    action(it)
                }
            }
        } finally {
            // for lifecycleScope, this block run in main thread
//            if (BuildConfig.DEBUG) {
//                Logger.d(TAG, "lifecycleScope@${lifecycleScope} canceled")
//            }
        }
    }
}

/**
 * 注册观察者，并绑定生命周期
 * 默认不进行线程切换，在emit线程执行
 */
fun <T> Flow<T>.observe(
    context: CoroutineContext = Dispatchers.Unconfined,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    owner: LifecycleOwner,
    action: suspend (value: T) -> Unit
) {
    this.observe(context, start, owner.lifecycleScope, action)
}

/**
 * 绑定生命周期
 * 默认不进行线程切换，在emit线程执行
 * @param context flow运行的线程
 * @param lifecycleScope 绑定的生命周期
 */
fun <T> Flow<T>.launchIn(
    context: CoroutineContext = Dispatchers.Unconfined,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    lifecycleScope: CoroutineScope
) {
    launchIn(lifecycleScope)
    lifecycleScope.launch(context, start) {
        try {
            this@launchIn.launchIn(this)
        } finally {
            // for lifecycleScope, this block run in main thread
//            if (BuildConfig.DEBUG) {
//                Logger.d(TAG, "lifecycleScope@${lifecycleScope} canceled")
//            }
        }
    }
}

/**
 * 绑定生命周期
 * 默认不进行线程切换，在emit线程执行
 */
fun <T> Flow<T>.launchIn(
    context: CoroutineContext = Dispatchers.Unconfined,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    owner: LifecycleOwner
) {
    this.launchIn(context, start, owner.lifecycleScope)
}

/**
 * 注册观察者，并绑定生命周期
 * 不切换线程
 */
fun <T> Flow<T>.observeUnconfined(lifecycleScope: CoroutineScope, action: suspend (value: T) -> Unit) {
    this.observe(context = Dispatchers.Unconfined, lifecycleScope = lifecycleScope, action = action)
}

/**
 * 绑定生命周期
 * 不切换线程
 */
fun <T> Flow<T>.launchUnconfined(lifecycleScope: CoroutineScope) {
    this.launchIn(context = Dispatchers.Unconfined, lifecycleScope = lifecycleScope)
}

/**
 * 注册观察者，并绑定生命周期
 * post到主线程执行
 */
fun <T> Flow<T>.observeInMain(lifecycleScope: CoroutineScope, action: suspend (value: T) -> Unit) {
    this.observe(context = Dispatchers.Main, lifecycleScope = lifecycleScope, action = action)
}

/**
 * 绑定生命周期
 * post到主线程执行
 */
fun <T> Flow<T>.launchInMain(lifecycleScope: CoroutineScope) {
    this.launchIn(context = Dispatchers.Main, lifecycleScope = lifecycleScope)
}

/**
 * 注册观察者，并绑定生命周期
 * 如果emit线程是主线程则直接执行; 如果emit线程不是主线程，则post到主线程执行
 */
fun <T> Flow<T>.observeInMainImmediate(lifecycleScope: CoroutineScope, action: suspend (value: T) -> Unit) {
    this.observe(context = Dispatchers.Main.immediate, lifecycleScope = lifecycleScope, action = action)
}

/**
 * 绑定生命周期
 * 如果emit线程是主线程则直接执行; 如果emit线程不是主线程，则post到主线程执行
 */
fun <T> Flow<T>.launchInMainImmediate(lifecycleScope: CoroutineScope) {
    this.launchIn(context = Dispatchers.Main.immediate, lifecycleScope = lifecycleScope)
}

/**
 * 注册观察者，并绑定生命周期
 * 切换到IO线程执行
 */
fun <T> Flow<T>.observeInIO(lifecycleScope: CoroutineScope, action: suspend (value: T) -> Unit) {
    this.observe(context = Dispatchers.IO, lifecycleScope = lifecycleScope, action = action)
}

/**
 * 绑定生命周期
 * 切换到IO线程执行，建议运行 IO 密集型任务
 */
fun <T> Flow<T>.launchInIO(lifecycleScope: CoroutineScope) {
    this.launchIn(context = Dispatchers.IO, lifecycleScope = lifecycleScope)
}

/**
 * 注册观察者，并绑定生命周期
 * 切换到Computation线程执行
 */
fun <T> Flow<T>.observeInComputation(lifecycleScope: CoroutineScope, action: suspend (value: T) -> Unit) {
    this.observe(context = Dispatchers.Default, lifecycleScope = lifecycleScope, action = action)
}

/**
 * 绑定生命周期
 * 切换到Computation线程执行，建议运行 CPU 密集型任务
 */
fun <T> Flow<T>.launchInComputation(lifecycleScope: CoroutineScope) {
    this.launchIn(context = Dispatchers.Default, lifecycleScope = lifecycleScope)
}

/**
 * 注册观察者，并绑定生命周期
 * 如果emit线程是异步线程则直接执行，如果emit线程是主线程，则post到异步线程执行
 */
fun <T> Flow<T>.observeInBackground(lifecycleScope: CoroutineScope, action: suspend (value: T) -> Unit) {
    this.observe(
        context = Dispatchers.Unconfined,
        start = CoroutineStart.UNDISPATCHED,
        lifecycleScope = lifecycleScope
    ) {
        if (isInMainThread()) {
            withContext(Dispatchers.Default) {
                if (isActive) {
                    action(it)
                }
            }
        } else {
            action(it)
        }
    }
}

/**
 * 注册观察者，并绑定生命周期
 * 不切换线程，在 emit 线程执行
 */
fun <T> Flow<T>.observe(lifecycleScope: CoroutineScope, action: suspend (value: T) -> Unit) {
    this.observe(
        context = Dispatchers.Unconfined,
        start = CoroutineStart.UNDISPATCHED,
        lifecycleScope = lifecycleScope,
        action = action
    )
}

/**
 * data binding
 */
inline fun <T, R> Flow<T>.bind(lifecycleScope: CoroutineScope, crossinline setter: (T) -> R) {
    this.observeInMainImmediate(lifecycleScope) {
        setter(it)
    }
}
