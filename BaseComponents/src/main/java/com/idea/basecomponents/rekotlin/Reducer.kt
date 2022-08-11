package com.tencent.bbg.rekotlin

typealias Reducer<ReducerStateType> = (action: Action, state: ReducerStateType) -> ReducerStateType