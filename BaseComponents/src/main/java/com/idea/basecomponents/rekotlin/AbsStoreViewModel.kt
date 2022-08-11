package com.tencent.bbg.rekotlin

import androidx.lifecycle.ViewModel

/**
 * @author qingqixu
 * @email qingqixu@tencent.com
 * @date 2022/02/09 14:13
 * @description AbsStoreViewModel
 **/
abstract class AbsStoreViewModel<S : StateType> constructor(
    private val store: Store<S>
) : ViewModel(), StoreType<S> by store {

    override fun onCleared() {
        super.onCleared()
        store.close()
    }

}