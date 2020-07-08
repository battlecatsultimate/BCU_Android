package com.mandarin.bcu.androidutil

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.*

object LocaleManager {
    @SuppressLint("ObsoleteSdkInt")
    fun langChange(context: Context, position: Int): ContextWrapper {
        var lang: String = if(position < StaticStore.lang.size)
            StaticStore.lang[position]
        else
            StaticStore.lang[0]

        var country = ""

        if(lang == "") {
            lang = Resources.getSystem().configuration.locales.get(0).language
            country = Resources.getSystem().configuration.locales.get(0).country
        }

        val config = context.resources.configuration

        if(lang != "") {
            val loc = if(country.isNotEmpty()) {
                Locale(lang, country)
            } else {
                Locale(lang)
            }

            Locale.setDefault(loc)
            config.setLocale(loc)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setSystemLocale(config,loc)
            } else {
                setSystemLocaleLegacy(config,loc)
            }
        }

        var c = context

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            c = context.createConfigurationContext(config)
        } else {
            context.resources.updateConfiguration(config,context.resources.displayMetrics)
        }

        return ContextWrapper(c)
    }

    private fun setSystemLocaleLegacy(config: Configuration, loc: Locale) {
        config.locale = loc
    }

    @TargetApi(Build.VERSION_CODES.N)
    fun setSystemLocale(config: Configuration, loc: Locale) {
        config.setLocale(loc)
    }
}