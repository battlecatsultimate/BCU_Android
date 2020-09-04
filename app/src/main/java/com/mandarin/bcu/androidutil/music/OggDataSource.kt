package com.mandarin.bcu.androidutil.music

import android.media.MediaDataSource
import common.system.files.FileData

class OggDataSource(private val desc: FileData) : MediaDataSource() {

    override fun close() {
    }

    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        val inp = desc.stream

        inp.skip(position)

        val result = inp.read(buffer, offset, size)

        inp.close()

        return result
    }

    override fun getSize(): Long {
        return desc.size().toLong()
    }
}