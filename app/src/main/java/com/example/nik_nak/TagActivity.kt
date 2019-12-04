package com.example.nik_nak

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class TagActivity : AppCompatActivity() {

    private val TAG = "Tag page"

    // Access a Cloud Firestore instance from your Activity
    private val db = FirebaseFirestore.getInstance()

    private var postTitleList = ArrayList<String>()
    private var postPointsList = ArrayList<String>()
    private var postIdList = ArrayList<String>()
    private var postUserIdList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag)

        // set up toolbar
        setSupportActionBar(findViewById(R.id.main_toolbar))

        // update toolbar title
        supportActionBar?.title = "Tag Page"

        // enable home navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fetchTags()
    }

    // handle soft back button event
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    // set up menu in action bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // reference to res/menu/new_post_menu.xml
        menuInflater.inflate(R.menu.tag_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    // actions on click menu items
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_search -> {
            searchTag()
            true
        }
        R.id.action_clear -> {
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
            overridePendingTransition(0, 0)
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun fetchTags () {
        var tagString = ""

        db.collection("tags")
            .orderBy("createdAt").limit(10)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "OnSuccessListener ${document.data["data"]}")
                    val tag = document.data["data"].toString()
                    tagString += "#$tag "
                }

                val tagText = this.findViewById<TextView>(R.id.tag)
                tagText.text = tagString
            }
    }

    private fun searchTag () {
        val edit = findViewById<TextInputEditText>(R.id.edit_text)
        // get string / remove white space
        val editText = edit.text.toString().toLowerCase().replace("\\s".toRegex(), "")

        if (editText.isNotBlank()) {
            db.collection("posts")
                .whereEqualTo("tag", editText)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
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
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        }
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
