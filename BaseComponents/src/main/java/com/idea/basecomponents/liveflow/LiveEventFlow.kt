package com.tencent.bbg.liveflow

/**
 * @author qingqixu
 * @email qingqixu@tencent.com
 * @date 2021/10/15 18:21
 * @description LiveEventFlow
 **/
internal class LiveEventFlow<T>
    : RecyclableEventFlow<T>(), EventFlow<T> by MutableEventFlow()