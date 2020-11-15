package com.mandarin.bcu.androidutil.supports

import android.app.Activity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException

abstract class CoroutineTask<Data> {
    enum class Status {
        READY,
        DOING,
        DONE
    }

    private var status = Status.READY
    var cancelled = false

    abstract fun prepare()

    abstract fun doSomething()

    abstract fun finish()

    open fun progressUpdate(vararg data: Data) {

    }

    fun publishProgress(vararg data: Data) {
        GlobalScope.launch(Dispatchers.Main) {
            progressUpdate(*data)
        }
    }

    fun execute() {
        if(status == Status.DOING) {
            throw IllegalStateException("Execute called while task is doing something")
        }

        status = Status.DOING

        GlobalScope.launch(Dispatchers.Main) {
            prepare()
        }

        GlobalScope.launch(Dispatchers.Default) {
            doSomething()
            status = Status.DONE

            withContext(Dispatchers.Main) {
                if(!cancelled)
                    finish()
            }
        }
    }

    fun cancel() {
        status = Status.DONE
        cancelled = true
        GlobalScope.launch(Dispatchers.Main) {
            finish()
        }
    }
}