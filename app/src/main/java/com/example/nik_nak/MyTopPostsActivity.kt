package com.example.nik_nak

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyTopPostsActivity : AppCompatActivity() {

    private val TAG = "My Top Post Activity"

    private var postTitleList = ArrayList<String>()
    private var postPointsList = ArrayList<String>()
    private var postIdList = ArrayList<String>()
    private var postUserIdList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_top_posts)

        // set up toolbar
        setSupportActionBar(findViewById(R.id.main_toolbar))

        // update toolbar title
        supportActionBar?.title = "My Top Posts"

        // enable home navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val sharedPref = getSharedPreferences(getString(R.string.user_id_key), Context.MODE_PRIVATE)
        val userId = sharedPref.getString(getString(R.string.user_id_key), "")

        // Access a Cloud Firestore instance from your Activity
        val db = FirebaseFirestore.getInstance()

        db.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("points", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                Log.i(TAG, "fetchPosts on success listener")
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    postTitleList.add(document.data["data"].toString())
                    postPointsList.add(document.data["points"].toString())
                    postIdList.add(document.id)
                    postUserIdList.add(document.data["userId"].toString())
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
        val listView = this.findViewById<ListView>(R.id.post_list_view)

        val postTitle = arrayOfNulls<String>(postTitleList.size)
        postTitleList.toArray(postTitle)

        val postPoints = arrayOfNulls<String>(postPointsList.size)
        postPointsList.toArray(postPoints)

        val postId = arrayOfNulls<String>(postIdList.size)
        postIdList.toArray(postId)

        val postUserId = arrayOfNulls<String>(postUserIdList.size)
        postUserIdList.toArray(postUserId)

        val postAdapter = PostAdapter(this, postTitle, postPoints, postId, postUserId)

        listView?.adapter = postAdapter
    }
}