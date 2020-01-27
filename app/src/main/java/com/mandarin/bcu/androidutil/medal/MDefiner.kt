package com.mandarin.bcu.androidutil.medal

import android.os.Environment
import com.mandarin.bcu.androidutil.StaticStore
import common.system.files.AssetData
import java.io.File

class MDefiner {
    private val filen = "MedalName.txt"
    private val filed = "MedalExplanation.txt"
    private val lan = arrayOf("/en/", "/zh/", "/kr/", "/jp/")
    fun define() {
        if (StaticStore.medalnumber == 0) {
            val path = Environment.getExternalStorageDirectory().path + "/Android/data/com.mandarin.BCU/files/org/page/medal/"
            val f = File(path)
            if (f.exists()) {
                StaticStore.medalnumber = f.list().size
            }
        }
        if (StaticStore.medallang == 1) {
            StaticStore.MEDNAME.clear()
            StaticStore.MEDEXP.clear()
            for (l in lan) {
                var path = Environment.getExternalStorageDirectory().path + "/Android/data/com.mandarin.BCU/lang" + l + filen
                var f = File(path)
                if (f.exists()) {
                    val qs = AssetData.getAsset(f).readLine()
                    if (qs != null) {
                        for (str in qs) {
                            val strs = str.trim { it <= ' ' }.split("\t").toTypedArray()
                            if (strs.size == 1) {
                                continue
                            }
                            val id = strs[0].trim { it <= ' ' }.toInt()
                            val name = strs[1].trim { it <= ' ' }
                            StaticStore.MEDNAME.put(l.substring(1, l.length - 1), id, name)
                        }
                    }
                }
                path = Environment.getExternalStorageDirectory().path + "/Android/data/com.mandarin.BCU/lang" + l + filed
                f = File(path)
                if (f.exists()) {
                    val qs = AssetData.getAsset(f).readLine()
                    if (qs != null) {
                        for (str in qs) {
                            val strs = str.trim { it <= ' ' }.split("\t").toTypedArray()
                            if (strs.size == 1) {
                                continue
                            }
                            val id = strs[0].trim { it <= ' ' }.toInt()
                            val name = strs[1].trim { it <= ' ' }
                            StaticStore.MEDEXP.put(l.substring(1, l.length - 1), id, name)
                        }
                    }
                }
            }
            StaticStore.medallang = 0
        }
    }
}