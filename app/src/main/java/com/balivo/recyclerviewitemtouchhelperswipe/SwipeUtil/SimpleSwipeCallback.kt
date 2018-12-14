package com.balivo.recyclerviewitemtouchhelperswipe.SwipeUtil

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log

import com.balivo.recyclerviewitemtouchhelperswipe.R
import android.R.string.cancel
import android.app.AlertDialog
import android.content.DialogInterface
import com.balivo.recyclerviewitemtouchhelperswipe.R.id.recyclerView
import android.R.drawable.ic_delete
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import kotlin.math.absoluteValue
import android.support.annotation.ColorInt
import com.balivo.recyclerviewitemtouchhelperswipe.R.id.recyclerView


class SimpleSwipeCallback(var itemSwipeCallback: ItemSwipeCallback)
    : ItemTouchHelper.SimpleCallback(0, 0) {

    interface ItemSwipeCallback {
        /**
         * Called when an item has been swiped
         *
         * @param position  position of item in the adapter
         * @param direction direction the item was swiped
         */
        fun itemSwiped(position: Int, direction: Int)

    }

    private var bgColorLeft: Int = 0
    private var bgColorRight: Int = 0
    private var leaveBehindDrawableLeft: Drawable? = null
    private var leaveBehindDrawableRight: Drawable? = null
    private var bgPaint: Paint = Paint()
    private var horizontalMargin = Integer.MAX_VALUE

    fun withLeaveBehindSwipeLeft(d: Drawable?): SimpleSwipeCallback {
        this.leaveBehindDrawableLeft = d
        setDefaultSwipeDirs(super.getSwipeDirs(null, null) or ItemTouchHelper.LEFT)
        return this
    }

    fun withLeaveBehindSwipeRight(d: Drawable?): SimpleSwipeCallback {
        this.leaveBehindDrawableRight = d
        setDefaultSwipeDirs(super.getSwipeDirs(null, null) or ItemTouchHelper.RIGHT)
        return this
    }

    fun withHorizontalMarginDp(ctx: Context, dp: Int): SimpleSwipeCallback {
        return withHorizontalMarginPx((ctx.getResources().getDisplayMetrics().density * dp).toInt())
    }

    fun withHorizontalMarginPx(px: Int): SimpleSwipeCallback {
        horizontalMargin = px
        return this
    }

    fun withBackgroundSwipeLeft(@ColorInt bgColor: Int): SimpleSwipeCallback {
        bgColorLeft = bgColor
        return this
    }

    fun withBackgroundSwipeRight(@ColorInt bgColor: Int): SimpleSwipeCallback {
        bgColorRight = bgColor
        return this
    }

    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        if (position != RecyclerView.NO_POSITION)
            itemSwipeCallback.itemSwiped(position, direction)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        if (viewHolder.adapterPosition == RecyclerView.NO_POSITION) {
            return
        }
        if (Math.abs(dX) > Math.abs(dY)) {
            itemView.elevation = 5f

            val itemWidth = itemView.right - itemView.left
            bgPaint.color = bgColorLeft
            val background = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
            c.drawRect(background, bgPaint)

            leaveBehindDrawableLeft?.let { drawable ->
                val intrinsicWidth = drawable.intrinsicWidth
                val intrinsicHeight = drawable.intrinsicHeight
                val xMargin = (itemHeight - intrinsicHeight) / 2
                val xMarkLeft = itemView.right - xMargin - intrinsicWidth
                val xMarkRight = itemView.right - xMargin
                val xMarkTop = itemView.top + (itemView.height - intrinsicHeight) / 2
                val xMarkBottom = xMarkTop + intrinsicHeight

                //Setting Swipe Icon
                leaveBehindDrawableLeft!!.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom)
                leaveBehindDrawableLeft!!.draw(c)

            }

            /*
            val isLeft = dX < 0
            if (bgPaint == null) {
                bgPaint = Paint()
                if (horizontalMargin == Integer.MAX_VALUE) {
                    withHorizontalMarginDp(recyclerView.context, 16)
                }
            }
            bgPaint?.setColor(if (isLeft) bgColorLeft else bgColorRight)

            if (bgPaint?.getColor() != Color.TRANSPARENT) {
                val left = if (isLeft) itemView.right + dX.toInt() else itemView.left
                val right = if (isLeft) itemView.right else itemView.left + dX.toInt()
                val background = Rect(left, itemView.top, right, itemView.bottom)
                c.drawRect(background, bgPaint)
            }

            val drawable = if (isLeft) leaveBehindDrawableLeft else leaveBehindDrawableRight
            drawable?.let {
                val itemHeight = itemView.bottom - itemView.top
                val itemWidth = itemView.right - itemView.left

                val x = if (dX < 0F) itemView.right - itemWidth / 6 - drawable.intrinsicWidth / 2
                else itemView.left + itemWidth / 6 - drawable.intrinsicWidth / 2

                val y = (itemHeight - drawable.intrinsicHeight) / 2 + itemView.top

                c.save()
                drawable.setBounds(x, y, x + drawable.intrinsicWidth, y + drawable.intrinsicHeight)
                drawable.draw(c)
                c.restore()

            }
            */
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX:Float) =
            recyclerView.setOnTouchListener { v, event ->
                var swipeBack : Boolean =
                        event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
                if (swipeBack) {
                    Log.d("swipe back", dX.toString())
                    checkIfAboveThreshold(recyclerView, viewHolder, dX)
                    viewHolder.itemView.elevation = 0f
                }
                 false

            }

    private fun checkIfAboveThreshold(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX:Float)
    {
        val threshold = getSwipeThreshold(viewHolder)
        val x = dX.toInt()
        if (Math.abs(x - viewHolder.itemView.getLeft()) < viewHolder.itemView.getWidth() * threshold) {
            Log.d("swipeutil", "is below thresold")
        }
        else {
            Log.d("swipeutil", "is above thresold")
        }
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder?): Float {
        return super.getSwipeThreshold(viewHolder)
    }
}