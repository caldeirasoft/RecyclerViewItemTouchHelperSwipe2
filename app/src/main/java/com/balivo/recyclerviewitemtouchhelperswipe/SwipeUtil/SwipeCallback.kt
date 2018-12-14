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
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.VectorDrawable
import android.support.annotation.ColorInt
import android.support.v4.graphics.ColorUtils
import android.view.MotionEvent
import org.w3c.dom.Text
import kotlin.math.absoluteValue


abstract class SwipeCallback(val swipeIcon: Drawable,
                             val swipeText: String,
                             @ColorInt val backgroundColor: Int,
                             swipeDirs: Int,
                             private val context: Context)
    : ItemTouchHelper.SimpleCallback(0, swipeDirs) {

    interface ItemSwipeCallback {
        /**
         * Called when an item has been swiped
         *
         * @param position  position of item in the adapter
         * @param direction direction the item was swiped
         */
        fun itemSwiped(position: Int, direction: Int)

    }

    var background: ColorDrawable
    //val backgroundColor:Int = Color.parseColor("#D32F2F")
    val disabledBackgroundColor:Int
    //val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_black_24dp)
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    init {
        background = ColorDrawable()
        disabledBackgroundColor = Color.parseColor("#DDDDDD")
    }

    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
        return false
    }

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        Log.d("convertToAbsolute:", flags.toString())
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val icon: Bitmap
        //Log.d("onChildDraw:", dX.toString())
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

            val itemView = viewHolder.itemView
            val itemHeight = itemView.bottom - itemView.top
            val itemWidth = itemView.right - itemView.left
            val isCanceled = dX == 0f && !isCurrentlyActive

            if (isCanceled) {
                clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                return
            }

            val isAboveThresold = checkIfAboveThreshold(recyclerView, viewHolder, dX)
            val bgColor = if (isAboveThresold) backgroundColor else disabledBackgroundColor
            val fgColor = if (isDark(bgColor)) Color.WHITE else Color.BLACK

            var backgroundBounds = Rect()
            var swipeIconBounds = Rect()
            var swipeTextBounds = Rect()

            val textPaint = Paint( )
            textPaint.color = fgColor
            textPaint.style = Paint.Style.FILL
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.textSize = 35F
            val bounds = Rect()
            textPaint.getTextBounds(swipeText, 0, swipeText.length, bounds)

            if (dX < 0) {
                // swipe left
                backgroundBounds.set(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)

                // Calculate position of delete icon
                val deleteIconTop = itemView.top + (itemHeight - swipeIcon.intrinsicHeight) / 2
                val deleteIconMargin = (itemHeight - swipeIcon.intrinsicHeight) / 2
                val deleteIconLeft = itemView.right - deleteIconMargin - swipeIcon.intrinsicWidth
                val deleteIconRight = itemView.right - deleteIconMargin
                val deleteIconBottom = deleteIconTop + swipeIcon.intrinsicWidth

                swipeIconBounds.set(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)

                val textTop = (itemView.top.toFloat() + bounds.height()) + (itemHeight - bounds.height()) / 2
                val textLeft = deleteIconLeft - bounds.width() - 10
                swipeTextBounds.set(textLeft, textTop.toInt(), 0, 0)
            }
            else {
                // swipe right
                backgroundBounds.set(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)

                // Calculate position of delete icon
                val deleteIconTop = itemView.top + (itemHeight - swipeIcon.intrinsicHeight) / 2
                val deleteIconMargin = (itemHeight - swipeIcon.intrinsicHeight) / 2
                val deleteIconLeft = itemView.left + deleteIconMargin
                val deleteIconRight = itemView.left + deleteIconMargin + swipeIcon.intrinsicWidth
                val deleteIconBottom = deleteIconTop + swipeIcon.intrinsicWidth

                swipeIconBounds.set(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)

                val textTop = (itemView.top.toFloat() + bounds.height()) + (itemHeight - bounds.height()) / 2
                val textLeft = deleteIconRight + 10
                swipeTextBounds.set(textLeft, textTop.toInt(), 0, 0)

            }

            // set background color
            background.color = bgColor
            background.setBounds(backgroundBounds)
            background.draw(c)

            // Draw the delete icon
            swipeIcon.setTint(fgColor)
            swipeIcon.setBounds(swipeIconBounds)
            swipeIcon.draw(c)

            c.drawText(swipeText, swipeTextBounds.left.toFloat(), swipeTextBounds.top.toFloat(),textPaint)
        }
        else {
            Log.d("actionState:", actionState.toString());
        }
        setTouchListener(recyclerView, viewHolder, dX)
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX:Float) =
            recyclerView.setOnTouchListener { v, event ->
                var swipeBack : Boolean =
                        event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
                if (swipeBack) {
                    Log.d("swipe back", dX.toString())
                    checkIfAboveThreshold(recyclerView, viewHolder, dX)
                }
                 false

            }

    private fun checkIfAboveThreshold(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX:Float): Boolean
    {
        val threshold = getSwipeThreshold(viewHolder)
        val x = dX.toInt()
        if (Math.abs(x - viewHolder.itemView.getLeft()) < viewHolder.itemView.getWidth() * threshold) {
            Log.d("swipeutil", "is below thresold")
            return false
        }
        else {
            Log.d("swipeutil", "is above thresold")
            return true
        }
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder?): Float {
        return super.getSwipeThreshold(viewHolder)
    }

    fun isDark(color:Int): Boolean {
        return ColorUtils.calculateLuminance(color) < 0.5
    }

    private fun getBitmap(vectorDrawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return bitmap
    }
}