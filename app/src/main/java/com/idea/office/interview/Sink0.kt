package com.idea.office.interview

/**
 * Copyright (c) 2022 TTXS. All rights reserved.
 * 类功能描述:
 *
 * @author brestwang
 * @date 2022/8/19
 */
private object Sink0 {
    @JvmStatic
    fun main(args: Array<String>) {
        val a = intArrayOf(0, 0, 1, 4, 5, 0, 2, 3)
        a.forEach {
            print("$it\t")
        }
        sink(a)
        println()
        a.forEach {
            print("$it\t")
        }
    }

    private fun sink(intArray: IntArray) {
        var last0 = intArray.size - 1
        var i = 0
        while (i < last0) {
            var value = intArray[i]
            if (value == 0) {
                skinRecursion(intArray, i, last0)
                last0--
            }
            value = intArray[i]
            if (value != 0) {
                i++
            }
        }

    }

    private fun skinRecursion(intArray: IntArray, i: Int, last0: Int) {
        if (i == last0) {
            return
        }
        val temp = intArray[i]
        intArray[i] = intArray[i + 1]
        intArray[i + 1] = temp
        skinRecursion(intArray, i + 1, last0)
    }
}