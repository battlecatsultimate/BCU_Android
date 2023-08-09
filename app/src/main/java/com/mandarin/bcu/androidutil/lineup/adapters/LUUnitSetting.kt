package com.mandarin.bcu.androidutil.lineup.adapters

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mandarin.bcu.R
import com.mandarin.bcu.UnitInfo
import com.mandarin.bcu.androidutil.GetStrings
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.lineup.LineUpView
import com.mandarin.bcu.androidutil.supports.AnimatorConst
import com.mandarin.bcu.androidutil.supports.AutoMarquee
import com.mandarin.bcu.androidutil.supports.ScaleAnimator
import com.mandarin.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.battle.BasisSet
import common.io.json.JsonEncoder
import common.util.unit.Form
import common.util.unit.Level

class LUUnitSetting : Fragment() {
    companion object {
        fun newInstance(line: LineUpView): LUUnitSetting {
            val unitSetting = LUUnitSetting()
            unitSetting.setVariable(line)

            return unitSetting
        }
    }

    private lateinit var line: LineUpView
    private lateinit var talent: Array<Spinner>
    private lateinit var superTalent: Array<Spinner>
    private var level = Level(8)

    private val talentIndex = ArrayList<Int>()
    private val superTalentIndex = ArrayList<Int>()

    private var fid = 0

    var f: Form? = null

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, bundle: Bundle?): View {
        val view = inflater.inflate(R.layout.lineup_unit_set, group, false)

        update()

        return view
    }

    fun update() {
        val v = view ?: return

        val spinners = arrayOf(v.findViewById(R.id.lineuplevspin), v.findViewById<Spinner>(R.id.lineuplevpspin))
        val plus = v.findViewById<TextView>(R.id.lineuplevplus)
        val row = v.findViewById<TableRow>(R.id.lineupunittable)
        val tal = v.findViewById<TableRow>(R.id.lineuppcoin)
        val supernprow = v.findViewById<TableRow>(R.id.lineupsuperpcoin)
        val t = v.findViewById<CheckBox>(R.id.lineuptalent)
        val hp = v.findViewById<TextView>(R.id.lineupunithp)
        val atk = v.findViewById<TextView>(R.id.lineupunitatk)
        val chform = v.findViewById<Button>(R.id.lineupchform)
        val levt = v.findViewById<TextView>(R.id.lineupunitlevt)

        f = if (StaticStore.position[0] == -1)
            null
        else if (StaticStore.position[0] == 100)
            line.repform
        else {
            if (StaticStore.position[0] * 5 + StaticStore.position[1] >= StaticStore.currentForms.size)
                null
            else
                StaticStore.currentForms[StaticStore.position[0] * 5 + StaticStore.position[1]]
        }

        if (f == null) {
            setDisappear(spinners[0], spinners[1], plus, row, t, tal, supernprow, chform, levt)
        } else {
            if (context == null)
                return

            var f = this.f ?: return

            level = BasisSet.current().sele.lu.getLv(f) ?: return

            BasisSet.synchronizeOrb(f.unit)

            setAppear(spinners[0], spinners[1], plus, row, t, tal, supernprow, chform, levt)

            val s = GetStrings(requireContext())

            fid = f.fid

            if (f.unit.maxp == 0)
                setDisappear(spinners[1], plus)

            if(this::talent.isInitialized && talent.isNotEmpty()) {
                tal.removeAllViews()
                talentIndex.clear()
            }

            if(this::superTalent.isInitialized && superTalent.isNotEmpty()) {
                supernprow.removeAllViews()
                superTalentIndex.clear()
            }

            if (f.du.pCoin != null) {
                val max = f.du.pCoin.max

                for(i in f.du.pCoin.info.indices) {
                    if(f.du.pCoin.info[i][13] == 1)
                        superTalentIndex.add(i)
                    else
                        talentIndex.add(i)
                }

                talent = Array(talentIndex.size) {
                    val spin = Spinner(context)

                    val param = TableRow.LayoutParams(0, StaticStore.dptopx(56f, context), (1.0 / (talentIndex.size)).toFloat())

                    spin.layoutParams = param
                    spin.setPopupBackgroundResource(R.drawable.spinner_popup)
                    spin.setBackgroundResource(androidx.appcompat.R.drawable.abc_spinner_mtrl_am_alpha)

                    tal.addView(spin)

                    spin
                }

                superTalent = Array(superTalentIndex.size) {
                    val spin = Spinner(context)

                    val param = TableRow.LayoutParams(0, StaticStore.dptopx(56f, context), (1.0 / (superTalentIndex.size)).toFloat())

                    spin.layoutParams = param
                    spin.setPopupBackgroundResource(R.drawable.spinner_popup)
                    spin.setBackgroundResource(androidx.appcompat.R.drawable.abc_spinner_mtrl_am_alpha)

                    supernprow.addView(spin)

                    spin
                }

                for(i in talent.indices) {
                    if(talentIndex[i] >= f.du.pCoin.info.size) {
                        talent[i].isEnabled = false
                        continue
                    }

                    val talentLevels = ArrayList<Int>()

                    for(j in 0 until max[talentIndex[i]] + 1)
                        talentLevels.add(j)

                    val adapter = ArrayAdapter(requireContext(), R.layout.spinneradapter, talentLevels)

                    talent[i].adapter = adapter
                    talent[i].setSelection(getIndex(talent[i], level.talents[talentIndex[i]]))

                    talent[i].onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                            level.talents[talentIndex[i]] = position

                            if (t.isChecked) {
                                hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                                atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                            } else {
                                removePCoin()
                                hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                                atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                            }

                            BasisSet.current().sele.lu.setLv(f.unit, level)

                            if(this@LUUnitSetting::line.isInitialized) {
                                line.updateUnitOrb()
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
                }

                for(i in superTalent.indices) {
                    if(superTalentIndex[i] >= f.du.pCoin.info.size) {
                        superTalent[i].isEnabled = false
                        continue
                    }

                    val superTalentLevels = java.util.ArrayList<Int>()

                    for(j in 0 until max[superTalentIndex[i]] + 1)
                        superTalentLevels.add(j)

                    val adapter = ArrayAdapter(requireContext(), R.layout.spinneradapter, superTalentLevels)

                    superTalent[i].adapter = adapter
                    superTalent[i].setSelection(getIndex(superTalent[i], level.talents[superTalentIndex[i]]))

                    superTalent[i].onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                            level.talents[superTalentIndex[i]] = position

                            if (t.isChecked) {
                                hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                                atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                            } else {
                                removePCoin()
                                hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                                atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                            }

                            BasisSet.current().sele.lu.setLv(f.unit, level)

                            if(this@LUUnitSetting::line.isInitialized) {
                                line.updateUnitOrb()
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
                }

                if(superTalent.isEmpty())
                    supernprow.visibility = View.GONE
            } else {
                talent = arrayOf()
                superTalent = arrayOf()

                for(i in level.talents.indices)
                    level.talents[i] = 0

                setDisappear(t, tal, supernprow)
            }

            val info = v.findViewById<ImageButton>(R.id.lineupunitinfo)

            info.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    val uid = f.unit.id

                    val intent = Intent(context, UnitInfo::class.java)

                    intent.putExtra("Data", JsonEncoder.encode(uid).toString())
                    requireContext().startActivity(intent)
                }
            })

            val levels = ArrayList<Int>()
            val plusLevels = ArrayList<Int>()

            for (i in 1 until (f.unit.max) + 1)
                levels.add(i)

            for (i in 0 until (f.unit.maxp) + 1)
                plusLevels.add(i)

            val adapter = ArrayAdapter(requireContext(), R.layout.spinneradapter, levels)
            val adapter1 = ArrayAdapter(requireContext(), R.layout.spinneradapter, plusLevels)

            spinners[0].adapter = adapter
            spinners[1].adapter = adapter1

            spinners[0].setSelection(getIndex(spinners[0], BasisSet.current().sele.lu.getLv(f).lv))
            spinners[1].setSelection(getIndex(spinners[1], BasisSet.current().sele.lu.getLv(f).plusLv))

            spinners[0].onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                    val lev = spinners[0].selectedItem as Int
                    val levp = spinners[1].selectedItem as Int

                    level.setLevel(lev)

                    if (t.isChecked) {
                        hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                        atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                    } else {
                        removePCoin()
                        hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                        atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                    }

                    BasisSet.current().sele.lu.setLv(f.unit, level)

                    if(this@LUUnitSetting::line.isInitialized) {
                        line.updateUnitOrb()
                    }

                    if(CommonStatic.getConfig().realLevel) {
                        for(i in superTalent.indices) {
                            changeSpinner(superTalent[i], lev + levp >= 60)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            spinners[1].onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                    val lev = spinners[0].selectedItem as Int
                    val levp = spinners[1].selectedItem as Int

                    level.setPlusLevel(levp)

                    if (t.isChecked) {
                        hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                        atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                    } else {
                        removePCoin()
                        hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                        atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                    }

                    if(CommonStatic.getConfig().realLevel) {
                        for(i in superTalent.indices) {
                            changeSpinner(superTalent[i], lev + levp >= 60)
                        }
                    }

                    BasisSet.current().sele.lu.setLv(f.unit, level)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
            atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)

            t.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    val anim = ScaleAnimator(tal, AnimatorConst.Dimension.HEIGHT, 300, AnimatorConst.Accelerator.DECELERATE, 0, StaticStore.dptopx(64f, requireContext()))
                    anim.start()

                    if(superTalentIndex.isNotEmpty()) {
                        val anim2 = ScaleAnimator(supernprow, AnimatorConst.Dimension.HEIGHT, 300, AnimatorConst.Accelerator.DECELERATE, 0, StaticStore.dptopx(64f, requireContext()))
                        anim2.start()
                    }

                    val lev = spinners[0].selectedItem as Int
                    val levp1 = spinners[1].selectedItem as Int

                    level.setLevel(lev)
                    level.setPlusLevel(levp1)

                    BasisSet.current().sele.lu.setLv(f.unit, level)

                    if(this::line.isInitialized) {
                        line.updateUnitOrb()
                    }

                    hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                    atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                } else {
                    val anim = ScaleAnimator(tal, AnimatorConst.Dimension.HEIGHT, 300, AnimatorConst.Accelerator.DECELERATE, StaticStore.dptopx(64f, requireContext()), 0)
                    anim.start()

                    if(superTalentIndex.isNotEmpty()) {
                        val anim2 = ScaleAnimator(supernprow, AnimatorConst.Dimension.HEIGHT, 300, AnimatorConst.Accelerator.DECELERATE, StaticStore.dptopx(64f, requireContext()), 0)
                        anim2.start()
                    }

                    val lev = spinners[0].selectedItem as Int
                    val levp1 = spinners[1].selectedItem as Int

                    level.setLevel(lev)
                    level.setPlusLevel(levp1)

                    removePCoin()

                    BasisSet.current().sele.lu.setLv(f.unit, level)
                    line.updateUnitOrb()

                    hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                    atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                }
            }

            if (f.du.pCoin != null) {
                var talentExist = false

                for(i in level.talents.indices)
                    if(level.talents[i] > 0) {
                        talentExist = true
                        break
                    }

                if(!talentExist) {
                    t.isChecked = false

                    val params = tal.layoutParams
                    params.height = 0
                    tal.layoutParams = params

                    if(supernprow.visibility == View.VISIBLE) {
                        val superParams = supernprow.layoutParams
                        superParams.height = 0
                        supernprow.layoutParams = superParams
                    }
                } else {
                    t.isChecked = true
                }
            }

            chform.setOnClickListener {
                fid++

                if (StaticStore.position[0] != 100 && StaticStore.position[1] != 100 && StaticStore.position[0] != -1 && StaticStore.position[1] != -1)
                    BasisSet.current().sele.lu.fs[StaticStore.position[0]][StaticStore.position[1]] = f.unit.forms[fid % f.unit.forms.size]
                else
                    line.repform = f.unit.forms[fid % f.unit.forms.size]

                this.f = f.unit.forms[fid % f.unit.forms.size]

                line.updateLineUp()
                line.toFormArray()

                val lev = spinners[0].selectedItem as Int
                val levp1 = spinners[1].selectedItem as Int

                level.setLevel(lev)
                level.setPlusLevel(levp1)

                f = this.f ?: return@setOnClickListener

                hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)

                if (f.du.pCoin == null) {
                    setDisappear(t, tal, supernprow)
                } else {
                    setAppear(t, tal)

                    if (superTalentIndex.isNotEmpty())
                        setAppear(supernprow)

                    val max = f.du.pCoin.max

                    for(i in talentIndex.indices) {
                        val list = ArrayList<Int>()

                        for (j in 0 until max[talentIndex[i]] + 1)
                            list.add(j)

                        val adapter2 = ArrayAdapter(requireContext(), R.layout.spinneradapter, list)

                        talent[i].adapter = adapter2

                        talent[i].setSelection(getIndex(talent[i], level.talents[talentIndex[i]]))

                        talent[i].setOnLongClickListener {
                            talent[i].isClickable = false
                            StaticStore.showShortMessage(context, s.getTalentName(talentIndex[i], f, requireActivity()))

                            true
                        }

                        talent[i].onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                                level.talents[talentIndex[i]] = position

                                if (t.isChecked) {
                                    hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                                    atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                                } else {
                                    removePCoin()
                                    hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                                    atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                                }

                                BasisSet.current().sele.lu.setLv(f.unit, level)

                                if(this@LUUnitSetting::line.isInitialized) {
                                    line.updateUnitOrb()
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }
                    }

                    for(i in superTalent.indices) {
                        val list = ArrayList<Int>()

                        for (j in 0 until max[superTalentIndex[i]] + 1)
                            list.add(j)

                        val adapter2 = ArrayAdapter(requireContext(), R.layout.spinneradapter, list)

                        superTalent[i].adapter = adapter2

                        superTalent[i].setSelection(getIndex(superTalent[i], level.talents[superTalentIndex[i]]))

                        superTalent[i].setOnLongClickListener {
                            superTalent[i].isClickable = false
                            StaticStore.showShortMessage(context, s.getTalentName(superTalentIndex[i], f, requireActivity()))

                            true
                        }

                        superTalent[i].onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                                level.talents[superTalentIndex[i]] = position

                                if (t.isChecked) {
                                    hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                                    atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                                } else {
                                    removePCoin()
                                    hp.text = s.getHP(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                                    atk.text = s.getAtk(f, BasisSet.current().t(), f.du.pCoin != null && t.isChecked, level)
                                }

                                BasisSet.current().sele.lu.setLv(f.unit, level)

                                if(this@LUUnitSetting::line.isInitialized) {
                                    line.updateUnitOrb()
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }

                        if(CommonStatic.getConfig().realLevel) {
                            changeSpinner(superTalent[i], level.lv + level.plusLv >= 60)
                        }
                    }

                    if (allZero(level.talents)) {
                        t.isChecked = false

                        val params = tal.layoutParams
                        params.height = 0

                        tal.layoutParams = params

                        if(supernprow.visibility == View.VISIBLE) {
                            val superParams = supernprow.layoutParams
                            superParams.height = 0

                            supernprow.layoutParams = superParams
                        }
                    } else {
                        t.isChecked = true
                    }
                }

                line.invalidate()

                line.updateUnitOrb()
            }
        }
    }

    private fun setDisappear(vararg views: View) {
        for (v in views)
            v.visibility = View.GONE
    }

    private fun setAppear(vararg views: View) {
        for (v in views)
            v.visibility = View.VISIBLE
    }

    fun setVariable(line: LineUpView) {
        this.line = line
    }

    private fun getIndex(spinner: Spinner, lev: Int): Int {
        var index = 0

        for (i in 0 until spinner.count)
            if (lev == spinner.getItemAtPosition(i) as Int)
                index = i

        return index
    }

    private fun removePCoin() {
        for(i in level.talents.indices) {
            level.talents[i] = 0
        }
    }

    private fun allZero(l: IntArray) : Boolean {
        for(e in l)
            if(e != 0)
                return false

        return true
    }

    private fun changeSpinner(spinner: Spinner, enable: Boolean) {
        spinner.isEnabled = enable
        spinner.background.alpha = if(enable)
            255
        else
            64

        if(spinner.childCount >= 1 && spinner.getChildAt(0) is AutoMarquee) {
            (spinner.getChildAt(0) as AutoMarquee).setTextColor((spinner.getChildAt(0) as AutoMarquee).textColors.withAlpha(if(enable) 255 else 64))
        }
    }
}
