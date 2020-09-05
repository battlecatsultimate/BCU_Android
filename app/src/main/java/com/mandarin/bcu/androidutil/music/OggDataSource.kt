package com.mandarin.bcu.androidutil.music

import android.media.MediaDataSource
import common.system.files.FileData

class OggDataSource(private val desc: FileData) : MediaDataSource() {
    private var ins = desc.stream

    private var ind = 0L

    override fun close() {
        ins.close()
    }

    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        if(position != ind) {
            close()
            ins = desc.stream
            ins.skip(position)
            ind = position
        }

        val result = ins.read(buffer, offset, size)

        if(result != -1)
            ind += result

        return result
    }

    override fun getSize(): Long {
        return desc.size().toLong()
    }
}