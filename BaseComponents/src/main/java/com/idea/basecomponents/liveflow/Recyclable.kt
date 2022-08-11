package com.tencent.bbg.liveflow

/**
 * @author qingqixu
 * @email qingqixu@tencent.com
 * @date 2021/11/4 15:14
 * @description RecyclableFlow
 **/
internal interface Recyclable<FLOW> {

    /**
     * 获取引用
     */
    fun tryObtain(): FLOW?

    /**
     * 获取引用
     */
    fun obtain(): FLOW

    /**
     * 释放引用
     */
    fun free()

    /**
     * 回收引用
     */
    fun tryRecycle(block: FLOW.() -> Boolean): Boolean

    /**
     * 是否可以回收，仅用于快速判断
     */
    fun isRecyclable(): Boolean

}