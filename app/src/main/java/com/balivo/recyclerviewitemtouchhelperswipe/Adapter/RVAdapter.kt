package com.balivo.recyclerviewitemtouchhelperswipe.Adapter

import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.balivo.recyclerviewitemtouchhelperswipe.R
import java.util.ArrayList
import java.util.HashMap

class RVAdapter(dataList:ArrayList<String>):RecyclerView.Adapter<ItemViewHolder>() {

    private val dataList:ArrayList<String>
    private val itemsPendingRemoval:ArrayList<String>

    private val handler = Handler() // hanlder for running delayed runnables

    internal var pendingRunnables = HashMap<String, Runnable>() // map of items to pending runnables, so we can cancel a removal if need be

    init{
        this.dataList = dataList
        itemsPendingRemoval = ArrayList<String>()
    }
    override fun onCreateViewHolder(parent:ViewGroup, viewType:Int):ItemViewHolder {
        val itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false)
        return ItemViewHolder(itemView)
    }
    override fun onBindViewHolder(holder:ItemViewHolder, position:Int) {
        val data = dataList.get(position)
        if (itemsPendingRemoval.contains(data))
        {
            /** {show swipe layout} and {hide regular layout} */
            holder.regularLayout.setVisibility(View.GONE)
            holder.swipeLayout.setVisibility(View.VISIBLE)
            holder.undo.setOnClickListener(object:View.OnClickListener {
                override fun onClick(v:View) {
                    undoOpt(data)
                }
            })
        }
        else
        {
            /** {show regular layout} and {hide swipe layout} */
            holder.regularLayout.setVisibility(View.VISIBLE)
            holder.swipeLayout.setVisibility(View.GONE)
            holder.listItem.setText(data)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    private fun undoOpt(customer:String) {
        val pendingRemovalRunnable = pendingRunnables.get(customer)
        pendingRunnables.remove(customer)
        if (pendingRemovalRunnable != null)
            handler.removeCallbacks(pendingRemovalRunnable)
        itemsPendingRemoval.remove(customer)
        // this will rebind the row in "normal" state
        notifyItemChanged(dataList.indexOf(customer))
    }

    fun pendingRemoval(position:Int) {
        val data = dataList.get(position)
        if (!itemsPendingRemoval.contains(data))
        {
            itemsPendingRemoval.add(data)
            // this will redraw row in "undo" state
            notifyItemChanged(position)
            // let's create, store and post a runnable to remove the data
            val pendingRemovalRunnable = object:Runnable {
                public override fun run() {
                    remove(dataList.indexOf(data))
                }
            }
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT.toLong())
            pendingRunnables.put(data, pendingRemovalRunnable)
        }
    }
    fun remove(position:Int) {
        val data = dataList.get(position)
        if (itemsPendingRemoval.contains(data))
        {
            itemsPendingRemoval.remove(data)
        }
        if (dataList.contains(data))
        {
            dataList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
    fun isPendingRemoval(position:Int):Boolean {
        val data = dataList.get(position)
        return itemsPendingRemoval.contains(data)
    }
    companion object {
        private val PENDING_REMOVAL_TIMEOUT = 3000 // 3sec
    }
}