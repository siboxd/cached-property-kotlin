package com.github.yamamotoj.cachedproperty

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

/**
 * Creates a property delegate that caches the value returned by [initializer] until [CachedProperty.invalidate] is called
 *
 * @see cached
 */
class CachedProperty<out T>(val initializer: () -> T) : ReadOnlyProperty<Any?, T>, Invalidatable {
    private var cachedValue: CachedValue<T> = CachedValue.Invalid

    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
        when (val currentCachedValue = cachedValue) {
            CachedValue.Invalid -> initializer().also { cachedValue = CachedValue.Value(it) }
            is CachedValue.Value<T> -> currentCachedValue.value
        }

    override fun invalidate() {
        cachedValue = CachedValue.Invalid
    }

    /** Internal class defining cached value states */
    private sealed class CachedValue<out T> {
        object Invalid : CachedValue<Nothing>()
        class Value<out T>(val value: T) : CachedValue<T>()
    }
}

/**
 * Creates a property delegate that caches the value returned by [initializer] until [CachedProperty.invalidate] is called
 *
 * The created property delegate is similar to kotlin `lazy { }` property delegate.
 * The [initializer] is called once first value request is made.
 * After this moment that's the value returned on subsequent getter calls.
 *
 * The main difference from `lazy { }` comes when the [CachedProperty.invalidate] method is called on this delegate object.
 * It will clear the cache and the [initializer] will be called again on next getter request.
 *
 * Example usage:
 *
 *      class SomeClass {
 *
 *          private val cacheDelegate = cache { Random(10).nextInt() }
 *          val myData by cacheDelegate
 *
 *          // Clears cached value and makes the `cache`
 *          // initializer block run again on next `myData` request
 *          fun revoke() = cacheDelegate.invalidate()
 *      }
 */
fun <T> cached(initializer: () -> T): CachedProperty<T> = CachedProperty(initializer)

/**
 * Invalidates this property *delegate*, if it's a [CachedProperty]; does nothing otherwise.
 *
 * This method has effect only if the provided property is delegated with [CachedProperty] instance.
 *
 * @see cached
 */
fun KProperty0<*>.invalidateCache() {
    (getDelegate() as? CachedProperty<*>)?.also { it.invalidate() }
}

/** Calls [invalidateCache] on each property */
fun Iterable<KProperty0<*>>.invalidateAllCaches() = forEach { it.invalidateCache() }

/** Calls [invalidateCache] on each property */
fun Sequence<KProperty0<*>>.invalidateAllCaches() = forEach { it.invalidateCache() }
