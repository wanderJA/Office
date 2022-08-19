package com.idea.office

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.idea.office", appContext.packageName)

        runBlocking {
            CoroutineScope(SupervisorJob()).launch {
                val asyncA = async(SupervisorJob()) {
                    delay(100)
                    Log.d("coroutine", "asyncA${System.currentTimeMillis()}")
                }
                val asyncB = async(SupervisorJob()) {
                    delay(200)
                    Log.d("coroutine", "asyncB${System.currentTimeMillis()}")
                }

                val resultA = kotlin.runCatching { asyncA.await() }
                val resultB = kotlin.runCatching { asyncB.await() }
            }
        }
    }
}