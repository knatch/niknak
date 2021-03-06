package com.example.nik_nak


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import kotlinx.android.synthetic.main.fragment_me.*

/**
 * A simple [Fragment] subclass.
 */
class MeFragment : Fragment() {

    private val TAG = "MeFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_me, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context as MainActivity
        val menuList = arrayOf("My Top Posts", "My posts", "My replies")

        val lv = context.findViewById(R.id.me_list) as ListView
        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, menuList)

        lv.setOnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> {
                    // start MyPostsActivity
                    val newIntent = Intent(context, MyTopPostsActivity::class.java)
                    startActivity(newIntent)
                }
                1 -> {
                    // start MyPostsActivity
                    val newIntent = Intent(context, MyPostsActivity::class.java)
                    startActivity(newIntent)
                }
                2 -> {
                    // start MyRepliesActivity
                    val newIntent = Intent(context, MyRepliesActivity::class.java)
                    startActivity(newIntent)
                }
            }
        }

        lv.adapter = adapter
    }
}
