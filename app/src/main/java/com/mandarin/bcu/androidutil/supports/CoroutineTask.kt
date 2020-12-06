package com.mandarin.bcu.androidutil.supports

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList

abstract class CoroutineTask<Data> {
    companion object {
        val tasks = ArrayList<CoroutineTask<*>>()
    }
    enum class Status {
        READY,
        DOING,
        DONE
    }

    private var status = Status.READY
    private var index = -1
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
        if(index == -1) {
            tasks.add(tasks.size, this)
            index = tasks.indexOf(this)
            Log.i("CoroutineTask", "Added Task : Index = $index, tasks left : $tasks")
        }

        if(status == Status.DOING) {
            throw IllegalStateException("Execute called while task is doing something")
        }

        if(index == 0) {
            status = Status.DOING

            GlobalScope.launch(Dispatchers.Main) {
                prepare()
            }

            GlobalScope.launch(Dispatchers.Default) {
                doSomething()
                status = Status.DONE
                getOut()

                withContext(Dispatchers.Main) {
                    if(!cancelled) {
                        finish()
                    }
                }
            }
        } else {
            Log.i("CoroutineTask","Waiting for another task to finish... Index = $index")
        }
    }

    fun cancel() {
        status = Status.DONE
        cancelled = true
        getOut()
        GlobalScope.launch(Dispatchers.Main) {
            finish()
        }
    }

    fun getOut() {
        tasks.remove(this)

        for(i in tasks.indices) {
            tasks[i].index--
        }

        if(tasks.isNotEmpty())
            tasks[0].execute()
    }

    override fun toString(): String {
        return "TASK $index, Status : $status"
    }
}