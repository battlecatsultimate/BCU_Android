package com.mandarin.bcu.androidutil.supports

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private var canceled = false
    var out = false

    abstract fun prepare()

    abstract fun doSomething()

    abstract fun finish()

    open fun progressUpdate(vararg data: Data) {

    }

    fun publishProgress(vararg data: Data) {
        CoroutineScope(Dispatchers.Main).launch {
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

            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    prepare()
                }

                doSomething()
                status = Status.DONE
                getOut()

                withContext(Dispatchers.Main) {
                    if(!canceled) {
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
        canceled = true
        getOut()
    }

    private fun getOut() {
        if(out)
            return

        tasks.remove(this)

        for(i in tasks.indices) {
            tasks[i].index--
        }

        if(tasks.isNotEmpty())
            tasks[0].execute()

        out = true
    }

    fun getStatus() : Status {
        return status
    }

    override fun toString(): String {
        return "TASK $index | Status : $status"
    }
}