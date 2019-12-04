package com.example.nik_nak

import android.content.ClipData
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PostDetailActivity : AppCompatActivity() {

    private val TAG = "Post Detail Activity"
    private val db = FirebaseFirestore.getInstance()

    private var replyTitleList = ArrayList<String>()
    private var postIdList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        // set up toolbar
        setSupportActionBar(findViewById(R.id.main_toolbar))

        // update toolbar title
        supportActionBar?.title = "Post Detail Page"

        // enable home navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val postId = intent.getStringExtra("postId")
        //
        fetchPost(postId)
        fetchReplies(postId)
    }

    // handle soft back button event
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun fetchPost (postId: String) {
        Log.i(TAG, "fetchPost function $postId")

        val docRef = db.collection("posts").document(postId)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.data != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    val titleTextView: TextView = this.findViewById(R.id.post_title)
                    titleTextView.text = document.data?.get("data").toString()
                } else {
                    Log.d(TAG, "No such document")

                    // build alert dialog
                    val dialogBuilder = AlertDialog.Builder(this)

                    // set message of alert dialog
                    dialogBuilder.setMessage("This post no longer exists in our database.")
                        // if the dialog is cancelable
                        .setCancelable(false)
                        // positive button text and action
                        .setPositiveButton("Back to My Replies", DialogInterface.OnClickListener {
                                dialog, id -> finish()
                        })
                        // negative button text and action
                        // .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                                // dialog, id -> dialog.cancel()
                        // })

                    // create dialog box
                    val alert = dialogBuilder.create()
                    // set title for alert dialog box
                    alert.setTitle("No data found")
                    // show alert dialog
                    alert.show()
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    private fun fetchReplies (postId: String) {
        db.collection("replies")
            .whereEqualTo("postId", postId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                // Log.i(TAG, "on success listener")
                for (document in result) {
                    // Log.d(TAG, "${document.id} => ${document.data}")
                    replyTitleList.add(document.data["data"].toString())
                    postIdList.add(document.data["postId"].toString())
                }

                // construct list
                createListView()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    private fun createListView () {
        Log.i(TAG, "CreateListView function")
        val listView = this.findViewById<ListView>(R.id.reply_list_view)

        val postTitle = arrayOfNulls<String>(replyTitleList.size)
        replyTitleList.toArray(postTitle)

        val postId = arrayOfNulls<String>(postIdList.size)
        postIdList.toArray(postId)

        val replyAdapter = ReplyAdapter(this, postTitle, postId)

        listView?.adapter = replyAdapter
    }

    // set up menu in action bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // reference to res/menu/new_post_menu.xml
        menuInflater.inflate(R.menu.new_reply_menu, menu)

        val deleteButton = menu!!.findItem(R.id.action_delete)
        deleteButton.isVisible = false

        val postUserId = intent.getStringExtra("postUserId")
        val sharedPref = getSharedPreferences(getString(R.string.user_id_key), Context.MODE_PRIVATE)
        val userId = sharedPref.getString(getString(R.string.user_id_key), "")

        if (postUserId == userId) {
            deleteButton.isVisible = true
        }
        return super.onCreateOptionsMenu(menu)
    }

    // actions on click menu items
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_reply -> {
            val postId = intent.getStringExtra("postId")
            // start PostDetailActivity
            val newIntent = Intent(this, NewReplyActivity::class.java)
            newIntent.putExtra("postId", postId)
            startActivityForResult(newIntent, 0)
            true
        }
        R.id.action_delete -> {
            createAlertDialog()
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        finish()
        startActivity(this.intent)
    }

    private fun createAlertDialog () {
        val dialogBuilder = AlertDialog.Builder(this)

        // set message of alert dialog
        dialogBuilder.setMessage("Are you sure you want to delete this post?")
            // if the dialog is cancelable
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                    dialog, id -> deletePost()
            })
            // negative button text and action
            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })

        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle("Delete dialog")
        // show alert dialog
        alert.show()
    }

    private fun deletePost () {
        val postId = intent.getStringExtra("postId")

        db.collection("posts").document(postId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully deleted!")
                val myToast = Toast.makeText(this, "Post deleted successfully", Toast.LENGTH_SHORT)
                myToast.show()

                finish()
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
        finish()
    }
}
