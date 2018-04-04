package com.balivo.recyclerviewitemtouchhelperswipe.SwipeUtil

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

import com.balivo.recyclerviewitemtouchhelperswipe.R

abstract class SwipeUtil(dragDirs: Int, swipeDirs: Int, private val context: FragmentActivity?) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

    private var background: Drawable? = null
    private var deleteIcon: Drawable? = null

    private var xMarkMargin: Int = 0

    private var initiated: Boolean = false

    var leftcolorCode: Int = 0
    var leftSwipeLable: String? = null


    private fun init() {
        background = ColorDrawable()
        xMarkMargin = context!!.resources.getDimension(R.dimen.ic_clear_margin).toInt()
        deleteIcon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_delete)
        deleteIcon!!.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        initiated = true
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    abstract override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)

    override fun getSwipeDirs(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {

        return super.getSwipeDirs(recyclerView, viewHolder)
    }


    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                             dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

        val itemView = viewHolder.itemView
        if (!initiated) {
            init()
        }

        val itemHeight = itemView.bottom - itemView.top

        //Setting Swipe Background
        (background as ColorDrawable).color = leftcolorCode
        background!!.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        background!!.draw(c)

        val intrinsicWidth = deleteIcon!!.intrinsicWidth
        val intrinsicHeight = deleteIcon!!.intrinsicWidth

        val xMarkLeft = itemView.right - xMarkMargin - intrinsicWidth
        val xMarkRight = itemView.right - xMarkMargin
        val xMarkTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val xMarkBottom = xMarkTop + intrinsicHeight


        //Setting Swipe Icon
        deleteIcon!!.setBounds(xMarkLeft, xMarkTop + 16, xMarkRight, xMarkBottom)
        deleteIcon!!.draw(c)

        //Setting Swipe Text
        val paint = Paint()
        paint.color = Color.WHITE
        paint.textSize = 48f
        paint.textAlign = Paint.Align.CENTER
        c.drawText(leftSwipeLable!!, (xMarkLeft + 40).toFloat(), (xMarkTop + 10).toFloat(), paint)


        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}