package com.mandarin.bcu.androidutil.enemy.asynchs

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.enemy.adapters.DynamicEmExplanation
import com.mandarin.bcu.androidutil.enemy.adapters.EnemyRecycle
import common.system.MultiLangCont
import java.lang.ref.WeakReference

class EInfoLoader : AsyncTask<Void?, Int?, Void?> {
    private val weakReference: WeakReference<Activity>
    private val id: Int
    private var multi = -1

    constructor(activity: Activity, id: Int) {
        weakReference = WeakReference(activity)
        this.id = id
    }

    constructor(activity: Activity, id: Int, multi: Int) {
        weakReference = WeakReference(activity)
        this.id = id
        this.multi = multi
    }

    override fun onPreExecute() {
        val activity = weakReference.get() ?: return
        if (MultiLangCont.EEXP.getCont(StaticStore.enemies[id]) == null) {
            val view1 = activity.findViewById<View>(R.id.enemviewtop)
            val view2 = activity.findViewById<View>(R.id.enemviewbot)
            val viewPager: ViewPager = activity.findViewById(R.id.eneminfexp)
            val exptext = activity.findViewById<TextView>(R.id.eneminfexptx)
            val eanim = activity.findViewById<Button>(R.id.eanimanim)
            if (view1 != null) {
                view1.visibility = View.GONE
                view2.visibility = View.GONE
                viewPager.visibility = View.GONE
                exptext.visibility = View.GONE
                eanim.visibility = View.GONE
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun doInBackground(vararg voids: Void?): Void? {
        val activity = weakReference.get() ?: return null
        val recyclerView: RecyclerView = activity.findViewById(R.id.eneminftable)
        val enemyRecycle: EnemyRecycle
        enemyRecycle = if (multi != -1) EnemyRecycle(activity, id, multi) else EnemyRecycle(activity, id)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = enemyRecycle
        ViewCompat.setNestedScrollingEnabled(recyclerView, false)
        val explain = DynamicEmExplanation(activity, id)
        val viewPager: ViewPager = activity.findViewById(R.id.eneminfexp)
        viewPager.adapter = explain
        viewPager.offscreenPageLimit = 1
        val treasure: FloatingActionButton = activity.findViewById(R.id.enemtreasure)
        val main: ConstraintLayout = activity.findViewById(R.id.enemmainlayout)
        val treasurelay: ConstraintLayout = activity.findViewById(R.id.enemtreasuretab)
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val set = AnimatorSet()
        treasure.setOnClickListener {
            if (!StaticStore.EisOpen) {
                val slider = ValueAnimator.ofInt(0, treasurelay.width).setDuration(300)
                slider.addUpdateListener { animation ->
                    treasurelay.translationX = -(animation.animatedValue as Int).toFloat()
                    treasurelay.requestLayout()
                }
                set.play(slider)
                set.interpolator = DecelerateInterpolator()
                set.start()
                StaticStore.EisOpen = true
            } else {
                val view = activity.currentFocus
                if (view != null) {
                    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    treasurelay.clearFocus()
                }
                val slider = ValueAnimator.ofInt(treasurelay.width, 0).setDuration(300)
                slider.addUpdateListener { animation ->
                    treasurelay.translationX = -(animation.animatedValue as Int).toFloat()
                    treasurelay.requestLayout()
                }
                set.play(slider)
                set.interpolator = AccelerateInterpolator()
                set.start()
                StaticStore.EisOpen = false
            }
        }
        treasurelay.setOnTouchListener { _, _ ->
            main.isClickable = false
            true
        }
        return null
    }

    override fun onPostExecute(result: Void?) {
        val activity = weakReference.get() ?: return
        val back: FloatingActionButton = activity.findViewById(R.id.eneminfbck)
        val eanim = activity.findViewById<Button>(R.id.eanimanim)
        back.setOnClickListener {
            StaticStore.EisOpen = false
            activity.finish()
        }
        val scrollView = activity.findViewById<ScrollView>(R.id.eneminfscroll)
        val prog = activity.findViewById<ProgressBar>(R.id.eneminfprog)
        scrollView.visibility = View.VISIBLE
        eanim.visibility = View.VISIBLE
        prog.visibility = View.GONE
    }
}