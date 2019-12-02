package com.example.nik_nak

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class ReplyAdapter (private val context: Activity, private val title: Array<String?>, private val postId: Array<String?>)
    : ArrayAdapter<String>(context, R.layout.list_reply, title) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.list_reply, null, true)

        val titleText = rowView.findViewById<TextView>(R.id.title)
        // val postIdText = rowView.findViewById<TextView>(R.id.post_id)

        titleText.text = title[position]
        titleText.tag = postId[position]
        // postIdText.text = postId[position]

        return rowView
    }
}