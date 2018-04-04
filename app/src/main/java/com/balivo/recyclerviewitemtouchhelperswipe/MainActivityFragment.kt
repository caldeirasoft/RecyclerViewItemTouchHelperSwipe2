package com.balivo.recyclerviewitemtouchhelperswipe

import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.balivo.recyclerviewitemtouchhelperswipe.Adapter.RVAdapter
import com.balivo.recyclerviewitemtouchhelperswipe.SwipeUtil.SwipeUtil

import java.util.ArrayList


/**
 * A placeholder fragment containing a simple view.
 */
class MainActivityFragment : Fragment() {

    private var mRecyclerView: RecyclerView? = null

    private val data: ArrayList<String>
        get() {
            val modelList = ArrayList<String>()
            for (i in 0..9) {
                modelList.add("data item : $i")
            }
            return modelList
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val mView = inflater.inflate(R.layout.fragment_main, container, false)
        mRecyclerView = mView.findViewById(R.id.recyclerView)
        return mView
    }


    override fun onResume() {
        super.onResume()
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        mRecyclerView!!.layoutManager = linearLayoutManager

        val rvAdapter = RVAdapter(data)
        mRecyclerView!!.adapter = rvAdapter

        setSwipeForRecyclerView()
    }

    private fun setSwipeForRecyclerView() {

        val swipeHelper = object : SwipeUtil(0, ItemTouchHelper.LEFT, activity) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val swipedPosition = viewHolder.adapterPosition
                val adapter = mRecyclerView!!.adapter as RVAdapter
                adapter.pendingRemoval(swipedPosition)
            }

            override fun getSwipeDirs(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
                val position = viewHolder!!.adapterPosition
                val adapter = mRecyclerView!!.adapter as RVAdapter
                return if (adapter.isPendingRemoval(position)) {
                    0
                } else super.getSwipeDirs(recyclerView, viewHolder)
            }
        }

        val mItemTouchHelper = ItemTouchHelper(swipeHelper)
        mItemTouchHelper.attachToRecyclerView(mRecyclerView)

        //set swipe label
        swipeHelper.leftSwipeLable="Archive"
        //set swipe background-Color
        swipeHelper.leftcolorCode=ContextCompat.getColor(activity!!, R.color.swipebg)

    }
}