package com.mandarin.bcu

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.pack.PackConflict
import com.mandarin.bcu.androidutil.pack.conflict.adapters.PackConfListAdapter
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.nhaarman.supertooltips.ToolTip
import com.nhaarman.supertooltips.ToolTipRelativeLayout
import common.CommonStatic
import common.io.assets.AssetLoader
import common.util.Data
import java.util.*
import kotlin.collections.ArrayList

class PackConflictDetail : AppCompatActivity() {
    var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed: SharedPreferences.Editor

        if (!shared.contains("initial")) {
            ed = shared.edit()
            ed.putBoolean("initial", true)
            ed.putBoolean("theme", true)
            ed.apply()
        } else {
            if (!shared.getBoolean("theme", false)) {
                setTheme(R.style.AppTheme_night)
            } else {
                setTheme(R.style.AppTheme_day)
            }
        }

        LeakCanaryManager.initCanary(shared)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        setContentView(R.layout.activity_pack_conflict_detail)

        val bundle = intent.extras

        if(bundle != null) {
            position = bundle.getInt("position")

            if(position < 0 || position >= PackConflict.conflicts.size)
                return

            val pc = PackConflict.conflicts[position]

            if(pc.confPack.size == 0)
                return

            val title = findViewById<TextView>(R.id.packconftitle)

            if(pc.confPack.size >= 1)
                title.text = pc.confPack[0]
            else {
                val name = "PACK_"+ Data.trio(position)
                title.text = name
            }

            val bck = findViewById<FloatingActionButton>(R.id.packconfdbck)

            bck.setOnClickListener {
                setResult(PackConflictSolve.RESULT_OK)
                finish()
            }

            val action = findViewById<Spinner>(R.id.packconfaction)
            val sticon = findViewById<ImageView>(R.id.packconfstatus)
            val status = findViewById<TextView>(R.id.packconfresult)
            val desc = findViewById<TextView>(R.id.packconfdesc)
            val path = findViewById<TextView>(R.id.packconfpath)
            val file = findViewById<TextView>(R.id.packconffile)

            val detail = findViewById<TextView>(R.id.packconfgdesc)
            val ac = findViewById<TextView>(R.id.packconfacdel)
            val acdesc = findViewById<TextView>(R.id.packconfacdeldesc)
            val ac2 = findViewById<TextView>(R.id.packconfacign)
            val acdesc2 = findViewById<TextView>(R.id.packconfacigndesc)

            if(pc.isSolvable) {
                when(pc.id) {
                    PackConflict.ID_CORRUPTED -> {
                        action.visibility = View.GONE

                        desc.setText(R.string.pack_conf_desc_corr)

                        detail.setText(R.string.pack_conf_guide_corr)

                        ac.visibility = View.GONE
                        acdesc.visibility = View.GONE
                        ac2.visibility = View.GONE
                        acdesc2.visibility = View.GONE

                        status.setText(R.string.pack_conf_deleted)

                        path.text = getFilePath(pc.confPack[0])

                        sticon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_approve))
                    }
                    PackConflict.ID_PARENT -> {
                        desc.text = getParentPackList(pc)

                        path.text = getFilePath(pc.confPack[0])

                        detail.setText(R.string.pack_conf_guide_parent)

                        ac.setText(R.string.pack_conf_guide_del)
                        acdesc.setText(R.string.pack_conf_guide_deldesc)
                        ac2.setText(R.string.pack_conf_guide_ign)
                        acdesc2.setText(R.string.pack_conf_guide_igndesc)

                        val name = ArrayList<String>()

                        name.add(getString(R.string.pack_conf_select))
                        name.add(getString(R.string.pack_conf_guide_del))
                        name.add(getString(R.string.pack_conf_guide_ign))

                        when(pc.action) {
                            PackConflict.ACTION_NONE -> {
                                setAnimationDrawable(sticon, R.drawable.notsolve_solve, false)
                                sticon.tag = PackConfListAdapter.NOTSOLVED
                                status.setText(R.string.pack_conf_nosolv)
                            }
                            PackConflict.ACTION_DELETE -> {
                                setAnimationDrawable(sticon, R.drawable.solve_notsolve, false)
                                sticon.tag = PackConfListAdapter.SOLVED
                                status.setText(R.string.pack_conf_solved)
                            }
                            PackConflict.ACTION_IGNORE -> {
                                setAnimationDrawable(sticon, R.drawable.warning_notsolve, false)
                                sticon.tag = PackConfListAdapter.CAUTION
                                status.setText(R.string.pack_conf_warn)
                            }
                        }

                        val adapter = object : ArrayAdapter<String>(this, R.layout.spinneradapter, name.toTypedArray()) {
                            override fun getView(position: Int, converView: View?, parent: ViewGroup): View {
                                val v = super.getView(position, converView, parent)

                                (v as TextView).setTextColor(StaticStore.getAttributeColor(context,R.attr.TextPrimary))

                                val eight = StaticStore.dptopx(8f, context)

                                v.setPadding(eight, eight, eight, eight)

                                return v
                            }

                            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val v = super.getDropDownView(position, convertView, parent)

                                if(isValid(position, pc)) {
                                    (v as TextView).setTextColor(StaticStore.getAttributeColor(context,R.attr.TextPrimary))
                                } else {
                                    (v as TextView).setTextColor(StaticStore.getAttributeColor(context, R.attr.HintPrimary))
                                }

                                return v
                            }

                            override fun isEnabled(position: Int): Boolean {
                                return isValid(position, pc)
                            }
                        }

                        action.adapter = adapter

                        when(pc.action) {
                            PackConflict.ACTION_NONE -> action.setSelection(0, false)
                            PackConflict.ACTION_DELETE -> action.setSelection(1, false)
                            PackConflict.ACTION_IGNORE -> action.setSelection(2, false)
                        }

                        action.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(parent: AdapterView<*>?) {}

                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                val t = sticon.tag

                                when(position) {
                                    0 -> {
                                        if(t != null) {
                                            when(t) {
                                                PackConfListAdapter.SOLVED -> setAnimationDrawable(sticon, R.drawable.solve_notsolve, true)
                                                PackConfListAdapter.CAUTION -> setAnimationDrawable(sticon, R.drawable.warning_notsolve, true)
                                            }
                                        } else {
                                            setAnimationDrawable(sticon, R.drawable.notsolve_solve, false)
                                        }

                                        sticon.tag = PackConfListAdapter.NOTSOLVED

                                        pc.action = PackConflict.ACTION_NONE

                                        status.setText(R.string.pack_conf_nosolv)
                                    }

                                    1 -> {
                                        if(t != null) {
                                            when(t) {
                                                PackConfListAdapter.NOTSOLVED -> setAnimationDrawable(sticon, R.drawable.notsolve_solve, true)
                                                PackConfListAdapter.CAUTION -> setAnimationDrawable(sticon, R.drawable.warning_solve, true)
                                            }
                                        } else {
                                            setAnimationDrawable(sticon, R.drawable.solve_notsolve, false)
                                        }

                                        sticon.tag = PackConfListAdapter.SOLVED

                                        pc.action = PackConflict.ACTION_DELETE

                                        status.setText(R.string.pack_conf_solved)
                                    }

                                    2 -> {
                                        if(t != null) {
                                            when(t) {
                                                PackConfListAdapter.NOTSOLVED -> setAnimationDrawable(sticon, R.drawable.notsolve_warning, true)
                                                PackConfListAdapter.SOLVED -> setAnimationDrawable(sticon, R.drawable.solve_warning, true)
                                            }
                                        } else {
                                            setAnimationDrawable(sticon, R.drawable.warning_notsolve, false)
                                        }

                                        sticon.tag = PackConfListAdapter.CAUTION

                                        val tool = ToolTip()
                                                .withText(R.string.pack_conf_warning)
                                                .withTextColor(StaticStore.getAttributeColor(this@PackConflictDetail, R.attr.TextPrimary))
                                                .withColor(StaticStore.getAttributeColor(this@PackConflictDetail, R.attr.ButtonPrimary))
                                                .withShadow()
                                                .withAnimationType(ToolTip.AnimationType.FROM_TOP)

                                        val toolv = findViewById<ToolTipRelativeLayout>(R.id.packconftooltip)

                                        val too = toolv.showToolTipForView(tool, sticon)

                                        too.setOnToolTipViewClickedListener {
                                            too.remove()
                                        }

                                        too.postDelayed({
                                            too.remove()
                                        }, 3000)

                                        status.setText(R.string.pack_conf_warn)

                                        pc.action = PackConflict.ACTION_IGNORE
                                    }
                                }
                            }
                        }
                    }

                    PackConflict.ID_SAME_ID -> {
                        desc.setText(R.string.pack_conf_desc_sameid)

                        path.text = getFilePath(pc.confPack[0])

                        ac.visibility = View.GONE
                        acdesc.visibility = View.GONE
                        ac2.visibility = View.GONE
                        acdesc2.visibility = View.GONE

                        when(pc.action) {
                            PackConflict.ACTION_NONE -> {
                                setAnimationDrawable(sticon, R.drawable.notsolve_solve, false)
                                sticon.tag = PackConfListAdapter.NOTSOLVED
                            }
                            else -> {
                                setAnimationDrawable(sticon, R.drawable.solve_notsolve, false)
                                sticon.tag = PackConfListAdapter.SOLVED
                            }
                        }

                        detail.setText(R.string.pack_conf_guide_sameid)

                        val name = ArrayList<String>()

                        name.add(getString(R.string.pack_conf_select))

                        for(pack in pc.confPack) {
                            name.add(pack)
                        }

                        val adapter = object : ArrayAdapter<String>(this, R.layout.spinneradapter, name.toTypedArray()) {
                            override fun getView(position: Int, converView: View?, parent: ViewGroup): View {
                                val v = super.getView(position, converView, parent)

                                (v as TextView).setTextColor(StaticStore.getAttributeColor(context,R.attr.TextPrimary))

                                val eight = StaticStore.dptopx(8f, context)

                                v.setPadding(eight, eight, eight, eight)

                                return v
                            }

                            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val v = super.getDropDownView(position, convertView, parent)

                                if(isValid(position, pc)) {
                                    (v as TextView).setTextColor(StaticStore.getAttributeColor(context,R.attr.TextPrimary))
                                } else {
                                    (v as TextView).setTextColor(StaticStore.getAttributeColor(context, R.attr.HintPrimary))
                                }

                                return v
                            }

                            override fun isEnabled(position: Int): Boolean {
                                return isValid(position, pc)
                            }
                        }

                        action.adapter = adapter

                        if(pc.action != PackConflict.ACTION_NONE) {
                            action.setSelection(pc.action+1, false)
                        } else {
                            action.setSelection(0, false)
                        }

                        action.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(parent: AdapterView<*>?) {}

                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                val t = sticon.tag

                                when(position) {
                                    0 -> {
                                        if(t != null) {
                                            when(t) {
                                                PackConfListAdapter.SOLVED -> setAnimationDrawable(sticon, R.drawable.solve_notsolve, true)
                                            }
                                        } else {
                                            setAnimationDrawable(sticon, R.drawable.notsolve_solve, false)
                                        }

                                        sticon.tag = PackConfListAdapter.NOTSOLVED

                                        pc.action = PackConflict.ACTION_NONE

                                        status.setText(R.string.pack_conf_nosolv)
                                    }

                                    else -> {
                                        if(t != null) {
                                            when(t) {
                                                PackConfListAdapter.NOTSOLVED -> setAnimationDrawable(sticon, R.drawable.notsolve_solve, true)
                                            }
                                        } else {
                                            setAnimationDrawable(sticon, R.drawable.solve_notsolve, false)
                                        }

                                        sticon.tag = PackConfListAdapter.SOLVED

                                        pc.action = position-1

                                        status.setText(R.string.pack_conf_solved)
                                    }
                                }
                            }
                        }
                    }

                    PackConflict.ID_UNSUPPORTED_CORE_VERSION -> {
                        desc.text = getVersions(pc)

                        path.text = getFilePath(pc.confPack[0])

                        var d = getString(R.string.pack_conf_guide_unsupp)

                        d = if(pc.confPack.size != 2) {
                            d.replace("-", AssetLoader.CORE_VER).replace("_", "Unknown")
                        } else {
                            d.replace("-", AssetLoader.CORE_VER).replace("_", pc.confPack[1])
                        }

                        detail.text = d

                        ac.setText(R.string.pack_conf_guide_del)
                        acdesc.setText(R.string.pack_conf_guide_deldesc)
                        ac2.setText(R.string.pack_conf_guide_ign)
                        acdesc2.setText(R.string.pack_conf_guide_igndesc)

                        val name = ArrayList<String>()

                        name.add(getString(R.string.pack_conf_select))
                        name.add(getString(R.string.pack_conf_guide_del))
                        name.add(getString(R.string.pack_conf_guide_ign))

                        when(pc.action) {
                            PackConflict.ACTION_NONE -> {
                                setAnimationDrawable(sticon, R.drawable.notsolve_solve, false)
                                sticon.tag = PackConfListAdapter.NOTSOLVED
                                status.setText(R.string.pack_conf_nosolv)
                            }
                            PackConflict.ACTION_DELETE -> {
                                setAnimationDrawable(sticon, R.drawable.solve_notsolve, false)
                                sticon.tag = PackConfListAdapter.SOLVED
                                status.setText(R.string.pack_conf_solved)
                            }
                            PackConflict.ACTION_IGNORE -> {
                                setAnimationDrawable(sticon, R.drawable.warning_notsolve, false)
                                sticon.tag = PackConfListAdapter.CAUTION
                                status.setText(R.string.pack_conf_warn)
                            }
                        }

                        val adapter = object : ArrayAdapter<String>(this, R.layout.spinneradapter, name.toTypedArray()) {
                            override fun getView(position: Int, converView: View?, parent: ViewGroup): View {
                                val v = super.getView(position, converView, parent)

                                (v as TextView).setTextColor(StaticStore.getAttributeColor(context,R.attr.TextPrimary))

                                val eight = StaticStore.dptopx(8f, context)

                                v.setPadding(eight, eight, eight, eight)

                                return v
                            }

                            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val v = super.getDropDownView(position, convertView, parent)

                                if(isValid(position, pc)) {
                                    (v as TextView).setTextColor(StaticStore.getAttributeColor(context,R.attr.TextPrimary))
                                } else {
                                    (v as TextView).setTextColor(StaticStore.getAttributeColor(context, R.attr.HintPrimary))
                                }

                                return v
                            }

                            override fun isEnabled(position: Int): Boolean {
                                return isValid(position, pc)
                            }
                        }

                        action.adapter = adapter

                        when(pc.action) {
                            PackConflict.ACTION_NONE -> action.setSelection(0, false)
                            PackConflict.ACTION_DELETE -> action.setSelection(1, false)
                            PackConflict.ACTION_IGNORE -> action.setSelection(2, false)
                        }

                        action.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onNothingSelected(parent: AdapterView<*>?) {}

                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                val t = sticon.tag

                                when(position) {
                                    0 -> {
                                        if(t != null) {
                                            when(t) {
                                                PackConfListAdapter.SOLVED -> setAnimationDrawable(sticon, R.drawable.solve_notsolve, true)
                                                PackConfListAdapter.CAUTION -> setAnimationDrawable(sticon, R.drawable.warning_notsolve, true)
                                            }
                                        } else {
                                            setAnimationDrawable(sticon, R.drawable.notsolve_solve, false)
                                        }

                                        sticon.tag = PackConfListAdapter.NOTSOLVED

                                        pc.action = PackConflict.ACTION_NONE

                                        status.setText(R.string.pack_conf_nosolv)
                                    }

                                    1 -> {
                                        if(t != null) {
                                            when(t) {
                                                PackConfListAdapter.NOTSOLVED -> setAnimationDrawable(sticon, R.drawable.notsolve_solve, true)
                                                PackConfListAdapter.CAUTION -> setAnimationDrawable(sticon, R.drawable.warning_solve, true)
                                            }
                                        } else {
                                            setAnimationDrawable(sticon, R.drawable.solve_notsolve, false)
                                        }

                                        sticon.tag = PackConfListAdapter.SOLVED

                                        pc.action = PackConflict.ACTION_DELETE

                                        status.setText(R.string.pack_conf_solved)
                                    }

                                    2 -> {
                                        if(t != null) {
                                            when(t) {
                                                PackConfListAdapter.NOTSOLVED -> setAnimationDrawable(sticon, R.drawable.notsolve_warning, true)
                                                PackConfListAdapter.SOLVED -> setAnimationDrawable(sticon, R.drawable.solve_warning, true)
                                            }
                                        } else {
                                            setAnimationDrawable(sticon, R.drawable.warning_notsolve, false)
                                        }

                                        sticon.tag = PackConfListAdapter.CAUTION

                                        val tool = ToolTip()
                                                .withText(R.string.pack_conf_warning)
                                                .withTextColor(StaticStore.getAttributeColor(this@PackConflictDetail, R.attr.TextPrimary))
                                                .withColor(StaticStore.getAttributeColor(this@PackConflictDetail, R.attr.ButtonPrimary))
                                                .withShadow()
                                                .withAnimationType(ToolTip.AnimationType.FROM_TOP)

                                        val toolv = findViewById<ToolTipRelativeLayout>(R.id.packconftooltip)

                                        val too = toolv.showToolTipForView(tool, sticon)

                                        too.setOnToolTipViewClickedListener {
                                            too.remove()
                                        }

                                        too.postDelayed({
                                            too.remove()
                                        }, 3000)

                                        status.setText(R.string.pack_conf_warn)

                                        pc.action = PackConflict.ACTION_IGNORE
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                action.visibility = View.GONE
                sticon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_notsolve))
                status.setText(R.string.pack_conf_cantsolve)
                desc.setText(R.string.pack_conf_desc_cantsolve)
                path.visibility = View.GONE
                file.visibility = View.GONE

                ac.visibility = View.GONE
                acdesc.visibility = View.GONE
                ac2.visibility = View.GONE
                acdesc2.visibility = View.GONE
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val bck = findViewById<FloatingActionButton>(R.id.packconfdbck)

                bck.performClick()
            }
        })
    }

    public override fun onDestroy() {
        super.onDestroy()

        StaticStore.toast = null
    }

    override fun attachBaseContext(newBase: Context) {
        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val lang = shared?.getInt("Language",0) ?: 0

        val config = Configuration()
        var language = StaticStore.lang[lang]
        var country = ""

        if(language == "") {
            language = Resources.getSystem().configuration.locales.get(0).language
            country = Resources.getSystem().configuration.locales.get(0).country
        }

        val loc = if(country.isNotEmpty()) {
            Locale(language, country)
        } else {
            Locale(language)
        }

        config.setLocale(loc)
        applyOverrideConfiguration(config)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
    }

    private fun getFilePath(pack: String) : String {
        return when {
            pack.endsWith(".pack.bcuzip") -> {
                StaticStore.getExternalPack(this)+pack
            }
            else -> {
                "Invalid File"
            }
        }
    }

    private fun getParentPackList(pc: PackConflict) : String {
        if(pc.confPack.size <= 1) {
            return getString(R.string.pack_conf_desc_parent).replace(" : _", "")
        }

        return getString(R.string.pack_conf_desc_parent).replace("_", pc.confPack[1])
    }

    private fun getVersions(pc: PackConflict) : String {
        return if(pc.confPack.size != 2) {
            getString(R.string.pack_desc_unsupported).replace("-", AssetLoader.CORE_VER).replace("_", "Unknown")
        } else {
            getString(R.string.pack_desc_unsupported).replace("-", AssetLoader.CORE_VER).replace("_", pc.confPack[1])
        }
    }

    private fun isValid(position: Int, pc: PackConflict) : Boolean {
        return if(position == 0)
            true
        else {
            if(pc.id == PackConflict.ID_SAME_ID) {
                pc.isValid(position-1)
            } else {
                pc.isValid(position)
            }
        }
    }

    private fun setAnimationDrawable(v: ImageView, id: Int, start: Boolean) {
        var anim: AnimatedVectorDrawable

        v.apply {
            setImageDrawable(ContextCompat.getDrawable(context, id))
            anim = drawable as AnimatedVectorDrawable
        }

        if(start) {
            anim.start()
        }
    }

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
    }
}