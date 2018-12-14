package com.balivo.recyclerviewitemtouchhelperswipe.SwipeUtil

import android.content.Context
import android.graphics.*
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import java.util.*
import kotlin.collections.ArrayList
import java.nio.file.Files.size
import com.balivo.recyclerviewitemtouchhelperswipe.SwipeUtil.SwipeUtil3.UnderlayButton




abstract class SwipeUtil3(val context: Context,
                          val recyclerView: RecyclerView,
                          val buttonWidth: Int,
                          val buttonText: Int)
    : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    companion object {
        val BUTTON_WIDTH = 100
        val BUTTON_TEXT = 20
    }
    private var buttons: ArrayList<UnderlayButton> = ArrayList()
    private lateinit var gestureDetector: GestureDetector
    private var swipedPos = -1
    private var swipeThreshold = 0.5f
    private lateinit var buttonsBuffer: HashMap<Int, ArrayList<UnderlayButton>>
    private var recoverQueue: Queue<Int> = PriorityQueue()

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            for (button in buttons!!) {
                if (button.onClick(e.x, e.y))
                    break
            }

            return true
        }
    }

    private val onTouchListener = object : View.OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if (swipedPos < 0) return false
            try {
                val point = Point(event.rawX.toInt(), event.rawY.toInt())

                val swipedViewHolder = recyclerView.findViewHolderForAdapterPosition(swipedPos)
                val swipedItem = swipedViewHolder.itemView
                val rect = Rect()
                swipedItem.getGlobalVisibleRect(rect)

                if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_MOVE) {
                    if (rect.top < point.y && rect.bottom > point.y)
                        gestureDetector.onTouchEvent(event)
                    else {
                        recoverQueue.add(swipedPos)
                        swipedPos = -1
                        recoverSwipedItem()
                    }
                }
            } catch (e2: Exception) {
                e2.printStackTrace()
            }

            return false
        }
    }

    init {
        try {
            gestureDetector = GestureDetector(context, gestureListener)
            recyclerView.setOnTouchListener(onTouchListener)
            buttonsBuffer = HashMap()
            recoverQueue = object : LinkedList<Int>() {
                fun add(o: Int?): Boolean {
                    return if (contains(o))
                        false
                    else
                        super.add(o!!)
                }
            }

            attachSwipe()
        }
        catch (e:java.lang.Exception){
            e.printStackTrace()
        }
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        try {
            val pos = viewHolder.getAdapterPosition();

            if (swipedPos != pos)
                recoverQueue.add(swipedPos);

            swipedPos = pos;

            if (buttonsBuffer.containsKey(swipedPos))
                buttons = buttonsBuffer.get(swipedPos)!!;
            else
                buttons.clear();

            buttonsBuffer.clear();
            swipeThreshold = 0.5f * buttons.size * BUTTON_WIDTH;
            recoverSwipedItem();
        }
        catch (e:java.lang.Exception)
        {
            e.printStackTrace();
        }
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder?): Float {
        return swipeThreshold
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return 0.1f * defaultValue
    }

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return 5.0f * defaultValue
    }

    private fun recoverSwipedItem(){
        while (!recoverQueue.isEmpty()){
            val pos = recoverQueue.poll();
            if (pos > -1) {
                recyclerView.getAdapter().notifyItemChanged(pos);
            }
        }
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        try {
            val pos = viewHolder.adapterPosition
            var translationX = dX
            val itemView = viewHolder.itemView

            if (pos < 0) {
                swipedPos = pos
                return
            }

            if (actionState === ItemTouchHelper.ACTION_STATE_SWIPE) {
                if (dX < 0) {
                    var buffer: ArrayList<UnderlayButton> = ArrayList()

                    if (!buttonsBuffer.containsKey(pos)) {
                        instantiateUnderlayButton(viewHolder, buffer)
                        buttonsBuffer[pos] = buffer
                    } else {
                        buffer = buttonsBuffer[pos]!!
                    }

                    translationX = dX * buffer.size * BUTTON_WIDTH / itemView.width
                    drawButtons(c, itemView, buffer, pos, translationX)
                }
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
        catch (e:java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun drawButtons(c:Canvas, itemView:View, buffer:List<UnderlayButton>, pos:Int, dX: Float)
    {
        var right:Float = itemView.getRight().toFloat();
        val dButtonWidth = (-1) * dX / buffer.size;

        for(button:UnderlayButton in buffer)
        {
            val left = right - dButtonWidth
            button.onDraw(c, RectF(left, itemView.top.toFloat(), right.toFloat(), itemView.bottom.toFloat()), pos)
            right = left
        }
    }

    fun attachSwipe() {
        val itemTouchHelper = ItemTouchHelper(this)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    abstract fun instantiateUnderlayButton(viewHolder: RecyclerView.ViewHolder, underlayButtons: ArrayList<UnderlayButton>)

    interface UnderlayButtonClickListener {
        fun onClick(pos: Int)
    }


    fun dpFromPx(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.density
    }

    fun pxFromDp(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    class UnderlayButton(private val text: String, private val imageResId: Int, bgColor: Int, txtColor: Int, private val clickListener: UnderlayButtonClickListener) {
        private var bgColor = Color.RED
        private var txtColor = Color.WHITE
        private var pos: Int = 0
        private var clickRegion: RectF? = null

        init {
            this.bgColor = bgColor
            this.txtColor = txtColor
        }

        fun onClick(x: Float, y: Float): Boolean {
            if (clickRegion != null && clickRegion!!.contains(x, y)) {
                clickListener.onClick(pos)
                return true
            }

            return false
        }

        fun onDraw(c: Canvas, rect: RectF, pos: Int) {
            val p = Paint()

            // Draw background
            p.color = bgColor
            c.drawRect(rect, p)

            // Draw Text
            p.color = txtColor
            p.textSize = BUTTON_TEXT.toFloat()

            val r = Rect()
            val cHeight = rect.height()
            val cWidth = rect.width()
            p.textAlign = Paint.Align.LEFT
            p.getTextBounds(text, 0, text.length, r)
            val x = cWidth / 2f - r.width() / 2f - r.left
            val y = cHeight / 2f + r.height() / 2f - r.bottom
            c.drawText(text, rect.left + x, rect.top + y, p)

            clickRegion = rect
            this.pos = pos
        }
    }
}