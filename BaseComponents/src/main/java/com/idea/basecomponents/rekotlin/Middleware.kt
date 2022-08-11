package com.tencent.bbg.rekotlin

typealias DispatchFunction = (Action) -> Boolean

typealias Middleware<State> = (DispatchFunction, () -> State) -> (DispatchFunction) -> DispatchFunction