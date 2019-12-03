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
import android.widget.TextView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    private val TAG = "Home Fragment"
    private var TYPE = "createdAt"

    private var postTitleList = ArrayList<String>()
    private var postPointsList = ArrayList<String>()
    private var postIdList = ArrayList<String>()

    override fun onActivityCreated (savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Log.i(TAG, "onActivityCreated")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = context as MainActivity
        val refreshLayout = context.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)
        refreshLayout.setOnRefreshListener{
            Log.d(TAG,"Refreshing")

            val listView = context.findViewById<ListView>(R.id.post_list_view)
            listView.invalidateViews()

            refreshLayout.isRefreshing = false
        }

        Log.i(TAG, "onViewCreated")

        arguments?.let {
            val type = it.getString("type").toString()

            when (type) {
                "hot" -> TYPE = "points"
                "new" -> TYPE = "createdAt"
            }
        }

        createListView()
    }

    private fun createListView () {
        val context = context as MainActivity

        // Access a Cloud Firestore instance from your Activity
        val db = FirebaseFirestore.getInstance()
        db.collection("posts")
            .orderBy(TYPE, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    // Log.d(TAG, "${document.id} => ${document.data}")
                    postTitleList.add(document.data["data"].toString())
                    postPointsList.add(document.data["points"].toString())
                    postIdList.add(document.id)
                }

                val postTitle = arrayOfNulls<String>(postTitleList.size)
                postTitleList.toArray(postTitle)

                val postPoints = arrayOfNulls<String>(postPointsList.size)
                postPointsList.toArray(postPoints)

                val postId = arrayOfNulls<String>(postIdList.size)
                postIdList.toArray(postId)


                val listView = context.findViewById(R.id.post_list_view) as ListView
                val postAdapter = PostAdapter(context, postTitle, postPoints, postId)
                listView.adapter = postAdapter

            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

}
