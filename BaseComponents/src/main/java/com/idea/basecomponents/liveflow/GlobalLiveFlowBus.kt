package com.idea.basecomponents.liveflow

/**
 * @author qingqixu
 * @email qingqixu@tencent.com
 * @date 2021/10/12 09:51
 * @description kotlin flow 实现全局事件总线
 **/
object GlobalLiveFlowBus : LiveFlowBus by LocalLiveFlowBus(autoClear = true)