package com.example.nik_nak

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyRepliesActivity : AppCompatActivity() {

    private val TAG = "My Replies Activity"

    private var postTitleList = ArrayList<String>()
    private var postIdList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_replies)

        // set up toolbar
        setSupportActionBar(findViewById(R.id.main_toolbar))

        // update toolbar title
        supportActionBar?.title = "My Replies Page"

        // enable home navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val sharedPref = getSharedPreferences(getString(R.string.user_id_key), Context.MODE_PRIVATE)
        val userId = sharedPref.getString(getString(R.string.user_id_key), "")

        // Access a Cloud Firestore instance from your Activity
        val db = FirebaseFirestore.getInstance()

        db.collection("replies")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                Log.i(TAG, "on success listener")
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    postTitleList.add(document.data["data"].toString())
                    postIdList.add(document.data["postId"].toString())
                }

                // construct list
                createListView()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    // handle soft back button event
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun createListView () {
        Log.i(TAG, "CreateListView function")
        val listView = this.findViewById<ListView>(R.id.reply_list_view)

        val postTitle = arrayOfNulls<String>(postTitleList.size)
        postTitleList.toArray(postTitle)

        val postId = arrayOfNulls<String>(postIdList.size)
        postIdList.toArray(postId)

        val replyAdapter = ReplyAdapter(this, postTitle, postId)

        listView?.adapter = replyAdapter

        listView.setOnItemClickListener { parent, view, position, id ->
            // val itemTitle = postTitleList[position]
            val itemPostId = postIdList[position]

            // start PostDetailActivity sending postId
            val newIntent = Intent(this, PostDetailActivity::class.java)
            newIntent.putExtra("postId", itemPostId)
            // newIntent.putExtra("postTitle", itemTitle)
            startActivity(newIntent)
        }
    }
}
