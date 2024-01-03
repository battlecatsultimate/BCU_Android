package com.mandarin.bcu.androidutil.io

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import common.CommonStatic
import common.io.assets.AssetLoader
import common.io.assets.UpdateCheck
import common.util.Data
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class AssetDownloadService : Service() {
    companion object {
        const val PROGRESS_TEXT = "com.mandarin.bcu::Progress-Text"
        const val PROGRESS_PROGRESSION = "com.mandarin.bcu::Progress-Progression"
        const val SUCCESS = "com.mandarin.bcu::Success"
        const val FAILED = "com.mandarin.bcu::Failed"

        const val NOTIFICATION_ID = "com.mandarin.bcu::Download-Notification"

        const val WAKE_UP = 0
    }

    private val langFolder = arrayOf("en/", "jp/", "kr/", "zh/", "fr/", "it/", "es/", "de/", "th/")

    private val jobManager = SupervisorJob()
    private val viewScope = CoroutineScope(Dispatchers.IO + jobManager)

    private val communicator = Messenger(MessageReceiver(this))

    private lateinit var notifyManager: NotificationManager
    private val notifyBuilder = NotificationCompat.Builder(this, NOTIFICATION_ID)

    private lateinit var broadcastCenter: LocalBroadcastManager

    private var status = ""
    private var message = ""

    private var lastNotificationTime = 0L

    override fun onBind(p0: Intent?): IBinder {
        return communicator.binder
    }

    override fun onCreate() {
        super.onCreate()

        broadcastCenter = LocalBroadcastManager.getInstance(this)

        notifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notifyBuilder.setSmallIcon(R.drawable.ic_baseline_arrow_downward_24)
        notifyBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        notifyBuilder.setOnlyAlertOnce(true)
        notifyBuilder.setOngoing(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val desc = getString(R.string.main_notif_down)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_ID, name, importance).apply {
                description = desc
            }

            notifyManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connectivityManager.activeNetwork

        if (networkInfo == null) {
            sendBroadcast(FAILED, getString(R.string.main_internet_check_fail))

            stopSelf()

            return START_NOT_STICKY
        }

        val capability = connectivityManager.getNetworkCapabilities(networkInfo)

        if (capability == null) {
            sendBroadcast(FAILED, getString(R.string.main_internet_check_fail))

            stopSelf()

            return START_NOT_STICKY
        }

        if (
            !capability.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
            !capability.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) &&
            !capability.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            ) {
            sendBroadcast(FAILED, getString(R.string.main_internet_no))

            stopSelf()

            return START_NOT_STICKY
        }

        viewScope.launch {
            try {
                val langShared = getSharedPreferences(StaticStore.LANG, Context.MODE_PRIVATE)
                val musicShared = getSharedPreferences(StaticStore.MUSIC, Context.MODE_PRIVATE)

                val updateJson = UpdateCheck.checkUpdate()

                //Read Language File
                val langFiles = ArrayList<String>()

                langFiles.add("Difficulty.txt")

                CommonStatic.getConfig().localLangMap["Difficulty.txt"] = langShared.getString("Difficulty.txt", "")

                for(lang in langFolder) {
                    for(l in StaticStore.langfile) {
                        langFiles.add("$lang$l")

                        CommonStatic.getConfig().localLangMap["$lang$l"] = langShared.getString("$lang$l", "")
                    }
                }

                //Read Music File
                val musicCount = updateJson?.music ?: 0

                for(i in 0 until musicCount) {
                    CommonStatic.getConfig().localMusicMap[i] = musicShared.getString(Data.trio(i), "")
                }

                val assetList = UpdateCheck.checkAsset(updateJson, "android")
                val langList = UpdateCheck.checkLang(langFiles.toTypedArray()).get()
                val musicList = UpdateCheck.checkMusic(musicCount).get()

                if(assetList.isNotEmpty()) {
                    for(asset in assetList) {
                        sendBroadcast(PROGRESS_TEXT, getString(R.string.down_state_doing, asset.target.name))

                        notifyBuilder.setContentTitle(getString(R.string.main_notif_down))
                        notifyBuilder.setContentText(asset.target.name).setOngoing(true)

                        notifyManager.notify(NOTIFICATION_ID, R.id.downloadnotification, notifyBuilder.build())

                        asset.run { p ->
                            sendBroadcast(PROGRESS_PROGRESSION, p)

                            notifyBuilder.setProgress(10000, (p * 10000).toInt(), false)

                            if (System.currentTimeMillis() - lastNotificationTime >= 1000L / 10) {
                                notifyManager.notify(NOTIFICATION_ID, R.id.downloadnotification, notifyBuilder.build())
                            }
                        }
                    }
                }

                if(musicList.isNotEmpty()) {
                    val editor = musicShared.edit()

                    for(music in musicList) {
                        sendBroadcast(PROGRESS_TEXT, getString(R.string.down_state_music, music.target.name))

                        val name = music.target.name.replace(".ogg", "")

                        notifyBuilder.setContentTitle(getString(R.string.main_notif_music))
                        notifyBuilder.setContentText(music.target.name).setOngoing(true)

                        notifyManager.notify(NOTIFICATION_ID, R.id.downloadnotification, notifyBuilder.build())

                        music.run { p ->
                            sendBroadcast(PROGRESS_PROGRESSION, p)

                            notifyBuilder.setProgress(10000, (p * 10000).toInt(), false)

                            if (System.currentTimeMillis() - lastNotificationTime >= 1000L / 10) {
                                notifyManager.notify(NOTIFICATION_ID, R.id.downloadnotification, notifyBuilder.build())
                            }
                        }

                        editor.putString(name, CommonStatic.getConfig().localMusicMap[name.toInt()])
                    }

                    editor.apply()
                }

                if(langList.isNotEmpty()) {
                    val editor = langShared.edit()

                    for(lang in langList) {
                        var fileName = (lang.target.parentFile?.name ?: "") + "/" + lang.target.name

                        if(fileName.startsWith("lang"))
                            fileName = lang.target.name

                        sendBroadcast(PROGRESS_TEXT, getString(R.string.down_state_doing, fileName))

                        notifyBuilder.setContentTitle(getString(R.string.main_notif_down))
                        notifyBuilder.setContentText(fileName).setOngoing(true)

                        notifyManager.notify(NOTIFICATION_ID, R.id.downloadnotification, notifyBuilder.build())

                        lang.run { p ->
                            sendBroadcast(PROGRESS_PROGRESSION, p)

                            notifyBuilder.setProgress(10000, (p * 10000).toInt(), false)

                            if (System.currentTimeMillis() - lastNotificationTime >= 1000L / 10) {
                                notifyManager.notify(NOTIFICATION_ID, R.id.downloadnotification, notifyBuilder.build())
                            }
                        }

                        editor.putString(fileName, CommonStatic.getConfig().localLangMap[fileName])

                        editor.apply()
                    }
                }

                sendBroadcast(PROGRESS_TEXT, getString(R.string.main_file_merge))

                AssetLoader.merge()

                sendBroadcast(SUCCESS, "")

                notifyBuilder.setContentText(null)
                    .setOngoing(false)
                    .setContentIntent(null)
                    .setProgress(0, 0, false)
                    .setContentTitle(getString(R.string.down_state_ok))

                delay(100L)

                notifyManager.notify(NOTIFICATION_ID, R.id.downloadnotification, notifyBuilder.build())
            } catch (_: Exception) {
                sendBroadcast(FAILED, getString(R.string.main_asset_fail))
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        notifyManager.cancelAll()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        notifyManager.cancelAll()
    }

    private fun sendBroadcast(code: String, content: String) {
        status = code
        this.message = content

        val message = Intent(code)

        message.putExtra("content", content)

        broadcastCenter.sendBroadcast(message)
    }

    private fun sendBroadcast(code: String, value: Double) {
        val message = Intent(code)

        message.putExtra("value", value)

        broadcastCenter.sendBroadcast(message)
    }

    private class MessageReceiver(service: AssetDownloadService) : Handler(Looper.getMainLooper()) {
        private val serviceReference = WeakReference(service)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            val service = serviceReference.get() ?: return

            if (msg.what == WAKE_UP) {
                if (service.status.isBlank())
                    return

                service.sendBroadcast(service.status, service.message)
            }
        }
    }
}