package com.mandarin.bcu.androidutil.io

import android.content.Context
import java.lang.reflect.Field
import java.util.function.Function

class DefferedLoader<T>(private val id: String, private val obj: Any,private val filed: Field, private val getter: Function<T, Any>) {
    companion object {
        @JvmStatic
        val pending = mutableListOf<DefferedLoader<Context>>()

        fun clearPending(id: String, c: Context) {
            if(pending.isEmpty())
                return

            pending.removeIf { t ->
                if(t.id != id) {
                    false
                } else {
                    t.load(c)
                    true
                }
            }
        }
    }

    fun load(c: T) {
        filed.set(obj, getter.apply(c))
    }

}