package com.mandarin.bcu.androidutil.pack.conflict.adapters

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.mandarin.bcu.PackConflictSolve
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.pack.PackConflict
import com.nhaarman.supertooltips.ToolTip
import com.nhaarman.supertooltips.ToolTipRelativeLayout
import common.util.Data

class PackConfListAdapter(context: Context, name: ArrayList<String>) : ArrayAdapter<String?>(context, R.layout.pack_conflict_list_layout, name.toTypedArray()) {
    companion object {
        const val NOTSOLVED = "Not_Solved"
        const val CAUTION = "Caution"
        const val SOLVED = "Solved"
    }

    private class ViewHoler constructor(row: View) {
        val title: TextView = row.findViewById(R.id.packconftitle)
        val desc: TextView = row.findViewById(R.id.packconfdesc)
        val remove: TextView = row.findViewById(R.id.packconfremove)
        val action: Spinner = row.findViewById(R.id.packconfaction)
        val status: ImageView = row.findViewById(R.id.packconfstatus)
        val tooltip: ToolTipRelativeLayout = row.findViewById(R.id.packconftooltip)
    }

    override fun getView(posit: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHoler
        val row:View

        if(convertView == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.pack_conflict_list_layout, convertView, false)
            holder = ViewHoler(row)
            row.tag = holder
        } else {
            row = convertView
            holder = row.tag as ViewHoler
        }

        if(posit >= PackConflict.conflicts.size)
            return row

        val pc = PackConflict.conflicts[posit]

        if(pc.isSolvable) {
            when(pc.id) {
                PackConflict.ID_CORRUPTED -> {
                    if(pc.confPack.size >= 1)
                        holder.title.text = pc.confPack[0]
                    else {
                        val name = "PACK_"+ Data.trio(posit)
                        holder.title.text = name
                    }

                    holder.desc.setText(R.string.pack_conf_corrupt)

                    holder.action.visibility = View.GONE
                    holder.remove.visibility = View.VISIBLE

                    holder.status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_approve))
                    holder.status.tag = SOLVED
                }

                PackConflict.ID_PARENT -> {
                    if(pc.confPack.size >= 1)
                        holder.title.text = pc.confPack[0]
                    else {
                        val name = "PACK_"+Data.trio(posit)
                        holder.title.text = name
                    }

                    holder.desc.setText(R.string.pack_conf_parent)

                    holder.action.visibility = View.VISIBLE
                    holder.remove.visibility = View.GONE

                    val names = ArrayList<String>()

                    names.add(context.getString(R.string.pack_conf_select))
                    names.add(context.getString(R.string.pack_conf_guide_del))
                    names.add(context.getString(R.string.pack_conf_guide_ign))

                    setAnimationDrawable(holder.status, R.drawable.solve_notsolve)
                    holder.status.tag = NOTSOLVED

                    val adapter = object : ArrayAdapter<String>(context, R.layout.spinneradapter, names.toTypedArray()) {
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

                    holder.action.adapter = adapter

                    holder.action.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            val t = holder.status.tag

                            when(position) {
                                0 -> {
                                    if(t != null) {
                                        when(t) {
                                            SOLVED -> setAnimationDrawable(holder.status, R.drawable.solve_notsolve)
                                            CAUTION -> setAnimationDrawable(holder.status, R.drawable.warning_notsolve)
                                        }
                                    } else {
                                        setAnimationDrawable(holder.status, R.drawable.solve_notsolve)
                                    }

                                    holder.status.tag = NOTSOLVED

                                    pc.action = PackConflict.ACTION_NONE
                                }

                                1 -> {
                                    if(t != null) {
                                        when(t) {
                                            NOTSOLVED -> setAnimationDrawable(holder.status, R.drawable.notsolve_solve)
                                            CAUTION -> setAnimationDrawable(holder.status, R.drawable.warning_solve)
                                        }
                                    } else {
                                        setAnimationDrawable(holder.status, R.drawable.notsolve_solve)
                                    }

                                    holder.status.tag = SOLVED

                                    pc.action = PackConflict.ACTION_DELETE
                                }

                                2 -> {
                                    if(t != null) {
                                        when(t) {
                                            NOTSOLVED -> setAnimationDrawable(holder.status, R.drawable.notsolve_warning)
                                            SOLVED -> setAnimationDrawable(holder.status, R.drawable.solve_warning)
                                        }
                                    } else {
                                        setAnimationDrawable(holder.status, R.drawable.solve_warning)
                                    }

                                    holder.status.tag = CAUTION

                                    val tool = ToolTip()
                                            .withText(R.string.pack_conf_warning)
                                            .withTextColor(StaticStore.getAttributeColor(context, R.attr.TextPrimary))
                                            .withColor(StaticStore.getAttributeColor(context, R.attr.ButtonPrimary))
                                            .withShadow()
                                            .withAnimationType(ToolTip.AnimationType.FROM_TOP)

                                    val toolv = holder.tooltip.showToolTipForView(tool, holder.status)

                                    toolv.setOnToolTipViewClickedListener {
                                        toolv.remove()
                                    }

                                    toolv.postDelayed( {
                                        toolv.remove()
                                    }, 3000)

                                    pc.action = PackConflict.ACTION_IGNORE
                                }
                            }

                            PackConflictSolve.data[posit] = pc.action
                        }
                    }

                    when(pc.action) {
                        PackConflict.ACTION_IGNORE -> holder.action.setSelection(2)
                        PackConflict.ACTION_DELETE -> holder.action.setSelection(1)
                        PackConflict.ACTION_NONE -> holder.action.setSelection(0)
                    }
                }

                PackConflict.ID_SAME_ID -> {
                    if(pc.confPack.size >= 1)
                        holder.title.text = pc.confPack[0]
                    else {
                        val name = "PACK_"+Data.trio(posit)
                        holder.title.text = name
                    }

                    holder.desc.setText(R.string.pack_conf_sameID)

                    setAnimationDrawable(holder.status, R.drawable.solve_notsolve)
                    holder.status.tag = NOTSOLVED

                    holder.action.visibility = View.VISIBLE
                    holder.remove.visibility = View.GONE

                    if(pc.confPack.size < 2)
                        return row

                    val names = ArrayList<String>()

                    names.add(context.getString(R.string.pack_conf_select))

                    for(n in pc.confPack) {
                        names.add(n)
                    }

                    val adapter = object : ArrayAdapter<String>(context, R.layout.spinneradapter, names.toTypedArray()) {
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

                    holder.action.adapter = adapter

                    holder.action.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            val t = holder.status.tag

                            when (position) {
                                0 -> {
                                    if (t != null) {
                                        when (t) {
                                            SOLVED -> setAnimationDrawable(holder.status, R.drawable.solve_notsolve)
                                        }
                                    } else {
                                        setAnimationDrawable(holder.status, R.drawable.solve_notsolve)
                                    }

                                    holder.status.tag = NOTSOLVED

                                    pc.action = PackConflict.ACTION_NONE
                                }

                                else -> {
                                    if (t != null) {
                                        when (t) {
                                            NOTSOLVED -> setAnimationDrawable(holder.status, R.drawable.notsolve_solve)
                                        }
                                    } else {
                                        setAnimationDrawable(holder.status, R.drawable.notsolve_solve)
                                    }

                                    holder.status.tag = SOLVED

                                    pc.action = position-1
                                }
                            }

                            PackConflictSolve.data[posit] = pc.action
                        }
                    }

                    if(pc.action != -1)
                        holder.action.setSelection(pc.action+1)
                    else
                        holder.action.setSelection(0)
                }

                PackConflict.ID_UNSUPPORTED_CORE_VERSION -> {
                    if(pc.confPack.size >= 1)
                        holder.title.text = pc.confPack[0]
                    else {
                        val name = "PACK_"+Data.trio(posit)
                        holder.title.text = name
                    }

                    holder.desc.setText(R.string.pack_conf_unsupported)

                    holder.action.visibility = View.VISIBLE
                    holder.remove.visibility = View.GONE

                    val names = ArrayList<String>()

                    names.add(context.getString(R.string.pack_conf_select))
                    names.add(context.getString(R.string.pack_conf_guide_del))
                    names.add(context.getString(R.string.pack_conf_guide_ign))

                    setAnimationDrawable(holder.status, R.drawable.solve_notsolve)
                    holder.status.tag = NOTSOLVED

                    val adapter = object : ArrayAdapter<String>(context, R.layout.spinneradapter, names.toTypedArray()) {
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

                    holder.action.adapter = adapter

                    holder.action.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {}

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            val t = holder.status.tag

                            when(position) {
                                0 -> {
                                    if(t != null) {
                                        when(t) {
                                            SOLVED -> setAnimationDrawable(holder.status, R.drawable.solve_notsolve)
                                            CAUTION -> setAnimationDrawable(holder.status, R.drawable.warning_notsolve)
                                        }
                                    } else {
                                        setAnimationDrawable(holder.status, R.drawable.solve_notsolve)
                                    }

                                    holder.status.tag = NOTSOLVED

                                    pc.action = PackConflict.ACTION_NONE
                                }

                                1 -> {
                                    if(t != null) {
                                        when(t) {
                                            NOTSOLVED -> setAnimationDrawable(holder.status, R.drawable.notsolve_solve)
                                            CAUTION -> setAnimationDrawable(holder.status, R.drawable.warning_solve)
                                        }
                                    } else {
                                        setAnimationDrawable(holder.status, R.drawable.notsolve_solve)
                                    }

                                    holder.status.tag = SOLVED

                                    pc.action = PackConflict.ACTION_DELETE
                                }

                                2 -> {
                                    if(t != null) {
                                        when(t) {
                                            NOTSOLVED -> setAnimationDrawable(holder.status, R.drawable.notsolve_warning)
                                            SOLVED -> setAnimationDrawable(holder.status, R.drawable.solve_warning)
                                        }
                                    } else {
                                        setAnimationDrawable(holder.status, R.drawable.solve_warning)
                                    }

                                    holder.status.tag = CAUTION

                                    val tool = ToolTip()
                                            .withText(R.string.pack_conf_warning)
                                            .withTextColor(StaticStore.getAttributeColor(context, R.attr.TextPrimary))
                                            .withColor(StaticStore.getAttributeColor(context, R.attr.ButtonPrimary))
                                            .withShadow()
                                            .withAnimationType(ToolTip.AnimationType.FROM_TOP)

                                    val toolv = holder.tooltip.showToolTipForView(tool, holder.status)

                                    toolv.setOnToolTipViewClickedListener {
                                        toolv.remove()
                                    }

                                    toolv.postDelayed( {
                                        toolv.remove()
                                    }, 3000)

                                    pc.action = PackConflict.ACTION_IGNORE
                                }
                            }

                            PackConflictSolve.data[posit] = pc.action
                        }
                    }

                    when(pc.action) {
                        PackConflict.ACTION_IGNORE -> holder.action.setSelection(2)
                        PackConflict.ACTION_DELETE -> holder.action.setSelection(1)
                        PackConflict.ACTION_NONE -> holder.action.setSelection(0)
                    }
                }
            }
        } else {
            holder.desc.visibility = View.GONE
            holder.remove.setText(R.string.pack_conf_cantsolve)
            holder.action.visibility = View.GONE
        }

        return row
    }

    private fun setAnimationDrawable(v: ImageView, id: Int) {
        var anim: AnimatedVectorDrawable

        v.apply {
            setImageDrawable(ContextCompat.getDrawable(context, id))
            anim = drawable as AnimatedVectorDrawable
        }

        anim.start()
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
}