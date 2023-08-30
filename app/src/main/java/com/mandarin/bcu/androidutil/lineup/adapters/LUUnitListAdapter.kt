package com.mandarin.bcu.androidutil.lineup.adapters

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.mandarin.bcu.R
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.lineup.LineUpView
import common.battle.BasisSet
import common.pack.Identifier
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.stage.Stage
import common.util.unit.Unit

class LUUnitListAdapter(context: Context, private val numbers: ArrayList<Identifier<Unit>>, private val stage: Stage? = null, star: Int = 0) : ArrayAdapter<Identifier<Unit>>(context, R.layout.listlayout, numbers.toTypedArray()) {
    inner class ViewHolder(row: View) {
        val layout = row.findViewById<ConstraintLayout>(R.id.listlayout)!!
        val id = row.findViewById<TextView>(R.id.unitID)!!
        val title = row.findViewById<TextView>(R.id.unitname)!!
        val image = row.findViewById<ImageView>(R.id.uniticon)!!
        private val fadeout = row.findViewById<View>(R.id.fadeout)!!

        fun updateComponents(position: Int) {
            val u = Identifier.get(numbers[position]) ?: return

            val unusable = isUnusableInStage(position)

            if (unusable > 0) {
                if (unusable == u.forms.size) {
                    layout.setBackgroundColor(StaticStore.getAttributeColor(context, R.attr.SemiWarningPrimary))
                } else {
                    layout.setBackgroundColor(StaticStore.getAttributeColor(context, R.attr.SemiCautionPrimary))
                }
            } else {
                layout.setBackgroundColor(StaticStore.getAttributeColor(context, R.attr.backgroundPrimary))
            }

            if (isEnabled(position)) {
                fadeout.visibility = View.GONE
            } else {
                fadeout.visibility = View.VISIBLE
            }

            if(position >= numbers.size) {
                title.visibility = View.GONE
                image.visibility = View.GONE

                return
            }

            val icon = u.forms[0].anim.uni?.img?.bimg()

            id.text = generateID(numbers[position])

            title.text = MultiLangCont.get(u.forms[0]) ?: u.forms[0].names.toString()

            if(icon != null) {
                image.setImageBitmap(StaticStore.makeIcon(context, icon as Bitmap , 48f))
            } else {
                image.setImageBitmap(StaticStore.makeIcon(context, null , 48f))
            }
        }
    }

    private val limit = stage?.getLim(star)
    lateinit var lineup: LineUpView

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if(convertView == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.listlayout, parent, false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = convertView
            holder =  row.tag as ViewHolder
        }

        holder.updateComponents(position)

        return row
    }
    override fun isEnabled(position: Int): Boolean {
        val u = Identifier.get(numbers[position]) ?: return false

        if (u.forms.any { f -> f.anim == null }) {
            return false
        }

        if (this::lineup.isInitialized) {
            val replaceForm = lineup.repform

            if (replaceForm?.unit != null && replaceForm.unit.id.equals(u.id))
                return false
        }

        return !BasisSet.current().sele.lu.fs.any { forms -> forms.filterNotNull().any { f -> f.unit != null && u.id.equals(f.unit.id) } }
    }

    private fun generateID(id: Identifier<Unit>) : String {
        return if(id.pack == Identifier.DEF) {
            context.getString(R.string.pack_default)+" - "+ Data.trio(id.id)
        } else {
            StaticStore.getPackName(id.pack) + " - " + Data.trio(id.id)
        }
    }

    private fun isUnusableInStage(position: Int): Int {
        stage ?: return 0

        limit ?: return 0
        val container = stage.cont ?: return 0

        val u = Identifier.get(numbers[position]) ?: return 0

        return u.forms.count { f -> limit.unusable(f.du, container.price) }
    }
}