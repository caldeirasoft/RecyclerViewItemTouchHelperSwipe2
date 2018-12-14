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
import com.balivo.recyclerviewitemtouchhelperswipe.SwipeUtil.SwipeCallback

import java.util.ArrayList
import android.app.AlertDialog
import android.graphics.Color
import com.balivo.recyclerviewitemtouchhelperswipe.SwipeUtil.SimpleSwipeCallback
import com.balivo.recyclerviewitemtouchhelperswipe.SwipeUtil.SwipeExtendCallback


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

        val swipeHelper2 = object :
                SwipeExtendCallback(
                        ItemTouchHelper.START or ItemTouchHelper.END,
                        requireContext())
        {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val adapter = mRecyclerView!!.adapter as RVAdapter

                if (direction == ItemTouchHelper.START || direction == ItemTouchHelper.END) {
                    val builder = AlertDialog.Builder(this@MainActivityFragment.context)
                    builder.setMessage("Are you sure you want to remove this service form shopping cart?")
                            .setCancelable(true)
                            .setPositiveButton("Yes", { dialog, id ->
                                adapter.pendingRemoval(position)
                                dialog.cancel()
                            })
                            .setNegativeButton("No", { dialog, id ->
                                adapter.notifyDataSetChanged()
                                dialog.cancel()
                            })
                    val alert = builder.create()
                    alert.show()
                } else if (direction == ItemTouchHelper.ANIMATION_TYPE_SWIPE_CANCEL) {
                    adapter.notifyDataSetChanged()
                }
            }
        }
        swipeHelper2.withSwipeAction(ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete_black_24dp)!!,
                "Delete",
                Color.RED,
                0.3f,
                object : SwipeExtendCallback.ItemSwipeCallback {
                    override fun itemSwiped(position: Int, direction: Int) {
                        val adapter = mRecyclerView!!.adapter as RVAdapter
                        adapter.remove(position)
                    }
                })

        val swipeCallback = object : SimpleSwipeCallback.ItemSwipeCallback {
            override fun itemSwiped(position: Int, direction: Int) {
                (mRecyclerView!!.adapter as RVAdapter).pendingRemoval(position)
            }
        }
        val simpleCallback = SimpleSwipeCallback(swipeCallback)
                .withBackgroundSwipeLeft(Color.RED)
                .withBackgroundSwipeRight(Color.RED)
                .withLeaveBehindSwipeLeft(ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_delete))
                .withLeaveBehindSwipeLeft(ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_add))


        ItemTouchHelper(swipeHelper2).attachToRecyclerView(mRecyclerView)
        //ItemTouchHelper(simpleCallback).attachToRecyclerView(mRecyclerView)

    }
}