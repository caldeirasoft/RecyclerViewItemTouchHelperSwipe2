package com.balivo.recyclerviewitemtouchhelperswipe.Adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.balivo.recyclerviewitemtouchhelperswipe.R

class ItemViewHolder(view:View):RecyclerView.ViewHolder(view) {

    var regularLayout:LinearLayout
    var swipeLayout:LinearLayout
    var listItem:TextView
    var undo:TextView

    init{
        regularLayout = view.findViewById(R.id.regularLayout) as LinearLayout
        listItem = view.findViewById(R.id.list_item) as TextView
        swipeLayout = view.findViewById(R.id.swipeLayout) as LinearLayout
        undo = view.findViewById(R.id.undo) as TextView
    }
}