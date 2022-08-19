package com.idea.basecomponents.liveflow

import java.util.concurrent.atomic.AtomicLong

/**
 * @author qingqixu
 * @email qingqixu@tencent.com
 * @date 2021/11/6 10:21
 * @description RecyclableEventFlow
 **/
internal abstract class RecyclableEventFlow<T> : EventFlow<T>, Recyclable<EventFlow<T>> {

    private companion object {
        private const val TAG = "RecyclableEventFlow"
        private const val RECYCLED = Long.MIN_VALUE
        private const val NONE = 0L
    }

    private val refCount = AtomicLong(NONE) // 引用计数

    /**
     * 获取引用
     */
    override fun tryObtain(): EventFlow<T>? {
        return if (refCount.getAndIncrement() < NONE) {
            null
        } else {
            this
        }
    }

    /**
     * 获取引用
     */
    override fun obtain(): EventFlow<T> {
        refCount.getAndIncrement()
        return this
    }

    /**
     * 释放引用
     */
    override fun free() {
        refCount.getAndDecrement()
    }

    /**
     * 回收引用
     */
    override fun tryRecycle(block: EventFlow<T>.() -> Boolean): Boolean {
        if (refCount.compareAndSet(NONE, RECYCLED)) {
            return if (this.block()) {
                true
            } else {
                refCount.set(NONE)
                false
            }
        }
        return false
    }

    /**
     * 是否可以回收，仅用于快速判断
     */
    override fun isRecyclable() = refCount.get() == NONE

}