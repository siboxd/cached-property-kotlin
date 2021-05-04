package com.github.yamamotoj.cachedproperty

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private val intValueCacheDelegate = cache { Random().nextInt() }
    val intValue by intValueCacheDelegate

    private val booleanValueCacheDelegate = CachedProperty { Random().nextBoolean() }
    val booleanValue by booleanValueCacheDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // After this call, subsequent access to intValue will trigger the initializer computation
        intValueCacheDelegate.invalidate()

        // After this call, subsequent access to booleanValue will trigger the initializer computation
        booleanValueCacheDelegate.invalidate()
    }
}
