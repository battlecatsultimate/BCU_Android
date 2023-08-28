package com.mandarin.bcu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mandarin.bcu.androidutil.Definer
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.enemy.adapters.EnemyListPager
import com.mandarin.bcu.androidutil.fakeandroid.BMBuilder
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import com.mandarin.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.pack.Identifier
import common.pack.PackData
import common.pack.UserProfile
import common.system.fake.ImageBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

open class EnemyList : AppCompatActivity() {
    enum class Mode {
        INFO,
        SELECTION
    }

    private var mode = Mode.INFO

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                supportFragmentManager.fragments.forEach {
                    if (it is EnemyListPager) {
                        it.validate()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed: Editor

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

        LeakCanaryManager.initCanary(shared, application)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        setContentView(R.layout.activity_enemy_list)

        ImageBuilder.builder = BMBuilder()

        val extra = intent.extras

        if(extra != null) {
            mode = Mode.valueOf(extra.getString("mode", "INFO"))
        }

        lifecycleScope.launch {
            //Prepare
            val tab = findViewById<TabLayout>(R.id.enlisttab)
            val pager = findViewById<ViewPager2>(R.id.enlistpager)
            val search = findViewById<FloatingActionButton>(R.id.enlistsch)
            val searchBar = findViewById<TextInputEditText>(R.id.enemlistschname)
            val searchBarLayout = findViewById<TextInputLayout>(R.id.enemlistschnamel)
            val back = findViewById<FloatingActionButton>(R.id.enlistbck)
            val st = findViewById<TextView>(R.id.status)
            val progression = findViewById<ProgressBar>(R.id.prog)

            StaticStore.setDisappear(tab, pager, search, searchBar, searchBarLayout)
            search.hide()

            progression.isIndeterminate = true

            //Load Data
            withContext(Dispatchers.IO) {
                Definer.define(this@EnemyList, { _ -> }, { t -> runOnUiThread { st.text = t }})
            }

            //Load UI
            if(StaticStore.entityname != "") {
                searchBar.setText(StaticStore.entityname)
            }

            searchBar.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    StaticStore.entityname = s.toString()

                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            supportFragmentManager.fragments.forEach {
                                if (it is EnemyListPager) {
                                    it.validate()
                                }
                            }
                        }
                    }
                }
            })

            pager.isSaveEnabled = false
            pager.isSaveFromParentEnabled = false

            pager.adapter = EnemyListTab()
            pager.offscreenPageLimit = UserProfile.getAllPacks().size

            val keys = getExistingPack()

            TabLayoutMediator(tab, pager) { t, position ->
                t.text = if(position == 0) {
                    getString(R.string.pack_default)
                } else {
                    val pack = UserProfile.getPack(keys[position])

                    if(pack == null) {
                        keys[position]
                    }

                    val name = when (pack) {
                        is PackData.DefPack -> {
                            getString(R.string.pack_default)
                        }
                        is PackData.UserPack -> {
                            pack.desc.names.toString()
                        }
                        else -> {
                            ""
                        }
                    }

                    name.ifEmpty {
                        keys[position]
                    }
                }
            }.attach()

            back.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    StaticStore.filterReset()
                    StaticStore.entityname = ""
                    finish()
                }
            })

            search.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    val intent = Intent(this@EnemyList, EnemySearchFilter::class.java)

                    resultLauncher.launch(intent)
                }
            })

            onBackPressedDispatcher.addCallback(
                this@EnemyList,
                object: OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        back.performClick()
                    }
                }
            )

            if(getEnemyPackNumber() > 1) {
                tab.visibility = View.VISIBLE
            } else {
                val collapse = findViewById<CollapsingToolbarLayout>(R.id.enemcollapse)

                val param = collapse.layoutParams as AppBarLayout.LayoutParams

                param.scrollFlags = 0

                collapse.layoutParams = param
            }

            StaticStore.setAppear(pager, search, searchBar, searchBarLayout)
            search.show()

            StaticStore.setDisappear(st, progression)
        }
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
    }

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
    }

    private fun getExistingPack() : ArrayList<String> {
        val packs = UserProfile.getAllPacks()

        val res = ArrayList<String>()

        for(p in packs) {
            if(p.enemies.list.isNotEmpty()) {
                if(p is PackData.DefPack) {
                    res.add(Identifier.DEF)
                } else if(p is PackData.UserPack) {
                    res.add(p.desc.id)
                }
            }
        }

        return res
    }

    private fun getEnemyPackNumber() : Int {
        val packs = UserProfile.getAllPacks()

        var res = 0

        for(p in packs) {
            if(p.enemies.list.isNotEmpty()) {
                res++
            }
        }

        return res
    }

    inner class EnemyListTab : FragmentStateAdapter(supportFragmentManager, lifecycle) {
        private val keys = getExistingPack()

        override fun getItemCount(): Int {
            return keys.size
        }

        override fun createFragment(position: Int): Fragment {
            return EnemyListPager.newInstance(keys[position], position, mode)
        }
    }
}