package com.mandarin.bcu

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StatFilterElement
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.androidutil.supports.adapter.StatFilterAdapter
import common.CommonStatic
import java.util.Locale

class StatSearchFilter : AppCompatActivity() {
    private lateinit var adapter: StatFilterAdapter
    private var unit = true
    
    private val option = intArrayOf(R.string.stg_sch_greater, R.string.stg_sch_same, R.string.stg_sch_less)

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
                setTheme(R.style.AppTheme_designNight)
            } else {
                setTheme(R.style.AppTheme_designDay)
            }
        }

        LeakCanaryManager.initCanary(shared, application)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        setContentView(R.layout.activity_stat_search_filter)

        val bundle = intent.extras ?: return

        unit = bundle.getBoolean("unit", true)

        adapter = StatFilterAdapter(this, unit)

        val group = findViewById<RadioGroup>(R.id.statschgroup)
        val or = findViewById<RadioButton>(R.id.statschor)
        val and = findViewById<RadioButton>(R.id.statschand)
        val list = findViewById<RecyclerView>(R.id.statschlist)
        val add = findViewById<FloatingActionButton>(R.id.statschadd)
        val remove = findViewById<FloatingActionButton>(R.id.statschdelete)
        val select = findViewById<FloatingActionButton>(R.id.statschselect)
        val bck = findViewById<FloatingActionButton>(R.id.statschbck)

        bck.setOnClickListener {
            finish()
        }

        group.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
            when(id) {
                R.id.statschor -> StatFilterElement.orand = true
                R.id.statschand -> StatFilterElement.orand = false
            }
        }

        if(StatFilterElement.orand) {
            or.isChecked = true
        } else {
            and.isChecked = true
        }

        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        ViewCompat.setNestedScrollingEnabled(list, false)

        select.hide()

        remove.setOnClickListener {
            if(StatFilterElement.show) {
                StatFilterElement.show = false
                select.hide()

                var index = 0

                list.children.forEach { child ->
                    child.clearFocus()
                }

                StatFilterElement.statFilter.removeIf {
                    val deleted = it.delete

                    if (deleted) {
                        adapter.notifyItemRemoved(index)
                    } else {
                        index++
                    }

                    deleted
                }

                adapter.notifyItemRangeChanged(0, StatFilterElement.statFilter.size)
            } else {
                StatFilterElement.show = true
                select.show()

                adapter.notifyItemRangeChanged(0, StatFilterElement.statFilter.size)
            }
        }

        select.setOnClickListener {
            for(element in StatFilterElement.statFilter) {
                element.delete = true
            }

            adapter.notifyItemRangeChanged(0, StatFilterElement.statFilter.size)
        }

        add.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v : View?) {
                if(unit) {
                    var t = StatFilterElement.HP
                    var o = StatFilterElement.OPTION_GREAT
                    var l = shared.getInt("default_level", 50) - 1

                    val builder = AlertDialog.Builder(this@StatSearchFilter)
                    val inflater = LayoutInflater.from(this@StatSearchFilter)

                    val view = inflater.inflate(R.layout.stat_add_dialog, null)

                    builder.setView(view)

                    val dialog = builder.create()

                    val addb = view.findViewById<Button>(R.id.statschaddb)
                    val cancel = view.findViewById<Button>(R.id.statschcancel)
                    val typespin = view.findViewById<Spinner>(R.id.statschtypespin)
                    val optspin = view.findViewById<Spinner>(R.id.statschoptspin)
                    val levspin = view.findViewById<Spinner>(R.id.statschlevspin)

                    val type = ArrayList<String>()
                    val opt = ArrayList<String>()
                    val lev = ArrayList<String>()

                    for(id in StatFilterAdapter.unitType) {
                        type.add(getString(id))
                    }

                    for(id in option) {
                        opt.add(getString(id))
                    }

                    for(level in 1..60) {
                        lev.add(level.toString())
                    }

                    typespin.adapter = object : ArrayAdapter<String>(this@StatSearchFilter, R.layout.spinneradapter, type.toTypedArray()) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val v1 = super.getView(position, convertView, parent)

                            if(StatFilterElement.canBeAdded(position)) {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.TextPrimary))
                            } else {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.HintPrimary))
                            }

                            return v1
                        }

                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val v1 = super.getView(position, convertView, parent)

                            if(StatFilterElement.canBeAdded(position)) {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.TextPrimary))
                            } else {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.HintPrimary))
                            }

                            return v1
                        }

                        override fun isEnabled(position: Int): Boolean {
                            return StatFilterElement.canBeAdded(position)
                        }
                    }

                    typespin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            t = position

                            while(true) {
                                if(StatFilterElement.canBeAdded(StatFilterAdapter.unitData[typespin.selectedItemPosition], o)) {
                                    optspin.setSelection(o, false)
                                    break
                                } else {
                                    o++
                                }
                            }

                            while(true) {
                                if(l >= lev.size) {
                                    l = 0
                                }

                                if(StatFilterElement.canBeAdded(StatFilterAdapter.unitData[typespin.selectedItemPosition], optspin.selectedItemPosition, l + 1)) {
                                    levspin.setSelection(l, false)
                                    break
                                } else {
                                    l++
                                }
                            }
                        }
                    }

                    optspin.adapter = object : ArrayAdapter<String>(this@StatSearchFilter, R.layout.spinneradapter, opt.toTypedArray()) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val v1 = super.getView(position, convertView, parent)

                            if(StatFilterElement.canBeAdded(typespin.selectedItemPosition, position)) {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.TextPrimary))
                            } else {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.HintPrimary))
                            }

                            return v1
                        }

                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val v1 = super.getView(position, convertView, parent)

                            if(StatFilterElement.canBeAdded(typespin.selectedItemPosition, position)) {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.TextPrimary))
                            } else {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.HintPrimary))
                            }

                            return v1
                        }

                        override fun isEnabled(position: Int): Boolean {
                            return StatFilterElement.canBeAdded(typespin.selectedItemPosition, position)
                        }
                    }

                    optspin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            o = position

                            while(true) {
                                if(l >= lev.size) {
                                    l = 0
                                }

                                if(StatFilterElement.canBeAdded(StatFilterAdapter.unitData[typespin.selectedItemPosition], optspin.selectedItemPosition, l + 1)) {
                                    levspin.setSelection(l, false)
                                    break
                                } else {
                                    l++
                                }
                            }
                        }
                    }

                    levspin.adapter = object : ArrayAdapter<String>(this@StatSearchFilter, R.layout.spinneradapter, lev.toTypedArray()) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val v1 = super.getView(position, convertView, parent)

                            if(StatFilterElement.canBeAdded(StatFilterAdapter.unitData[typespin.selectedItemPosition], optspin.selectedItemPosition, position+1)) {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.TextPrimary))
                            } else {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.HintPrimary))
                            }

                            return v1
                        }

                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val v1 = super.getView(position, convertView, parent)

                            if(StatFilterElement.canBeAdded(StatFilterAdapter.unitData[typespin.selectedItemPosition], optspin.selectedItemPosition, position+1)) {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.TextPrimary))
                            } else {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.HintPrimary))
                            }

                            return v1
                        }

                        override fun isEnabled(position: Int): Boolean {
                            return StatFilterElement.canBeAdded(StatFilterAdapter.unitData[typespin.selectedItemPosition], optspin.selectedItemPosition, position+1)
                        }
                    }

                    levspin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            l = position
                        }
                    }

                    while(true) {
                        if(StatFilterElement.canBeAdded(t)) {
                            typespin.setSelection(StatFilterAdapter.unitData[typespin.selectedItemPosition])
                            break
                        } else {
                            t++
                        }
                    }

                    while(true) {
                        if(StatFilterElement.canBeAdded(StatFilterAdapter.unitData[typespin.selectedItemPosition], o)) {
                            optspin.setSelection(o)
                            break
                        } else {
                            o++
                        }
                    }

                    while(true) {
                        if(l >= lev.size) {
                            l = 0
                        }

                        if(StatFilterElement.canBeAdded(StatFilterAdapter.unitData[typespin.selectedItemPosition], optspin.selectedItemPosition, l + 1)) {
                            levspin.setSelection(l)
                            break
                        } else {
                            l++
                        }
                    }

                    addb.setOnClickListener(object : SingleClick() {
                        override fun onSingleClick(v: View?) {
                            StatFilterElement(StatFilterAdapter.unitData[t], o, l+1)

                            adapter.notifyItemInserted(StatFilterElement.statFilter.size-1)
                            dialog.dismiss()
                        }
                    })

                    cancel.setOnClickListener(object : SingleClick() {
                        override fun onSingleClick(v: View?) {
                            dialog.dismiss()
                        }
                    })

                    dialog.show()
                } else {
                    var t = StatFilterElement.HP
                    var o = StatFilterElement.OPTION_GREAT
                    var m = 100

                    val builder = AlertDialog.Builder(this@StatSearchFilter)
                    val inflater = LayoutInflater.from(this@StatSearchFilter)

                    val view = inflater.inflate(R.layout.stat_sch_e_layout, null)

                    builder.setView(view)

                    val dialog = builder.create()

                    val addb = view.findViewById<Button>(R.id.statschaddb)
                    val cancel = view.findViewById<Button>(R.id.statschcancel)
                    val typespin = view.findViewById<Spinner>(R.id.statschtypespin)
                    val optspin = view.findViewById<Spinner>(R.id.statschoptspin)
                    val multi = view.findViewById<TextInputLayout>(R.id.statschmulti)
                    val multie = view.findViewById<TextInputEditText>(R.id.statschmultiedit)

                    val type = ArrayList<String>()
                    val opt = ArrayList<String>()

                    for(id in StatFilterAdapter.enemyType) {
                        type.add(getString(id))
                    }

                    for(id in option) {
                        opt.add(getString(id))
                    }

                    typespin.adapter = object : ArrayAdapter<String>(this@StatSearchFilter, R.layout.spinneradapter, type.toTypedArray()) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val v1 = super.getView(position, convertView, parent)

                            if(StatFilterElement.canBeAdded(position)) {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.TextPrimary))
                            } else {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.HintPrimary))
                            }

                            return v1
                        }

                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val v1 = super.getView(position, convertView, parent)

                            if(StatFilterElement.canBeAdded(position)) {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.TextPrimary))
                            } else {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.HintPrimary))
                            }

                            return v1
                        }

                        override fun isEnabled(position: Int): Boolean {
                            return StatFilterElement.canBeAdded(position)
                        }
                    }

                    typespin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            t = position

                            while(true) {
                                if(StatFilterElement.canBeAdded(StatFilterAdapter.enemyData[typespin.selectedItemPosition], o)) {
                                    optspin.setSelection(o, false)
                                    break
                                } else {
                                    o++
                                }
                            }

                            while(true) {
                                if(m == Int.MAX_VALUE) {
                                    m = 0
                                }

                                if(StatFilterElement.canBeAdded(StatFilterAdapter.enemyData[typespin.selectedItemPosition], optspin.selectedItemPosition, m)) {
                                    multie.setText(m.toString())
                                    break
                                } else {
                                    m++
                                }
                            }
                        }
                    }

                    optspin.adapter = object : ArrayAdapter<String>(this@StatSearchFilter, R.layout.spinneradapter, opt.toTypedArray()) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val v1 = super.getView(position, convertView, parent)

                            if(StatFilterElement.canBeAdded(typespin.selectedItemPosition, position)) {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.TextPrimary))
                            } else {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.HintPrimary))
                            }

                            return v1
                        }

                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val v1 = super.getView(position, convertView, parent)

                            if(StatFilterElement.canBeAdded(typespin.selectedItemPosition, position)) {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.TextPrimary))
                            } else {
                                (v1 as TextView).setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.HintPrimary))
                            }

                            return v1
                        }

                        override fun isEnabled(position: Int): Boolean {
                            return StatFilterElement.canBeAdded(typespin.selectedItemPosition, position)
                        }
                    }

                    optspin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            o = position

                            while(true) {
                                if(StatFilterElement.canBeAdded(StatFilterAdapter.enemyData[typespin.selectedItemPosition], optspin.selectedItemPosition, m)) {
                                    multie.setText(m.toString())
                                    break
                                } else {
                                    m++
                                }
                            }
                        }
                    }

                    while(true) {
                        if(StatFilterElement.canBeAdded(t)) {
                            typespin.setSelection(StatFilterAdapter.enemyData[typespin.selectedItemPosition])
                            break
                        } else {
                            t++
                        }
                    }

                    while(true) {
                        if(StatFilterElement.canBeAdded(StatFilterAdapter.enemyData[typespin.selectedItemPosition], o)) {
                            optspin.setSelection(o)
                            break
                        } else {
                            o++
                        }
                    }

                    while(true) {
                        if(m == Int.MAX_VALUE) {
                            m = 0
                        }

                        if(StatFilterElement.canBeAdded(StatFilterAdapter.enemyData[typespin.selectedItemPosition], optspin.selectedItemPosition, m)) {
                            multie.setText(m.toString())
                            break
                        } else {
                            m++
                        }
                    }

                    addb.setOnClickListener(object : SingleClick() {
                        override fun onSingleClick(v: View?) {
                            StatFilterElement(StatFilterAdapter.enemyData[t], o, m+1)

                            adapter.notifyItemInserted(StatFilterElement.statFilter.size-1)
                            dialog.dismiss()
                        }
                    })

                    cancel.setOnClickListener(object : SingleClick() {
                        override fun onSingleClick(v: View?) {
                            dialog.dismiss()
                        }
                    })

                    multie.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            val res = s.toString()

                            if(res.isEmpty()) {
                                if(!multi.isErrorEnabled) {
                                    multi.isErrorEnabled = true
                                    addb.isEnabled = false
                                    addb.setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.HintPrimary))
                                }

                                multi.error = getString(R.string.sch_stat_empty)
                            } else {
                                val value = try {
                                    res.toInt()
                                } catch (e: NumberFormatException) {
                                    multie.setText(Int.MAX_VALUE.toString())
                                    multie.setSelection(Int.MAX_VALUE.toString().length)

                                    Int.MAX_VALUE
                                }

                                if(!StatFilterElement.canBeAdded(StatFilterAdapter.enemyData[typespin.selectedItemPosition], optspin.selectedItemPosition, value)) {
                                    if(!multi.isErrorEnabled) {
                                        multi.isErrorEnabled = true
                                        addb.isEnabled = false
                                        addb.setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.HintPrimary))
                                    }

                                    multi.error = getString(R.string.sch_stat_exist)

                                    return
                                } else {
                                    if(multi.isErrorEnabled) {
                                        multi.isErrorEnabled = false
                                        multi.error = null
                                        addb.isEnabled = true
                                        addb.setTextColor(StaticStore.getAttributeColor(this@StatSearchFilter, R.attr.TextPrimary))
                                    }

                                    m = value
                                }
                            }
                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                    })

                    multie.post {
                        multie.setSelection(m.toString().length)
                    }

                    dialog.show()
                }
            }
        })

        StatFilterElement.started = true
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

    override fun onDestroy() {
        super.onDestroy()
        StaticStore.toast = null
        StatFilterElement.started = false
    }

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
    }
}