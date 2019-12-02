package com.example.nik_nak

import android.content.Context
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class NewReplyActivity : AppCompatActivity() {

    private val TAG = "New Reply"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_reply)

        // set up toolbar
        setSupportActionBar(findViewById(R.id.main_toolbar))

        // update toolbar title
        supportActionBar?.title = "Reply to post"

        // enable home navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // auto focus edit box when activity is initialized
        val edit = findViewById<EditText>(R.id.edit_text)
        edit.requestFocus()
    }

    // set up menu in action bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // reference to res/menu/new_post_menu.xml
        menuInflater.inflate(R.menu.new_post_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    // handle soft back button event
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    // actions on click menu items
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_create -> {
            // display posting status
            val myToast = Toast.makeText(this, "Sending...", Toast.LENGTH_SHORT)
            myToast.show()

            val edit = findViewById<EditText>(R.id.edit_text)
            val editText = edit.text.toString()

            val currentTime = Timestamp.now()

            val postId = intent.getStringExtra("postId")

            val sharedPref = getSharedPreferences(getString(R.string.user_id_key), Context.MODE_PRIVATE)
            val userId = sharedPref.getString(getString(R.string.user_id_key), "")

            val newPost = hashMapOf(
                "data" to editText,
                "createdAt" to currentTime,
                "points" to 0,
                "userId" to userId,
                "postId" to postId
            )
                // Access a Cloud Firestore instance from your Activity
                val db = FirebaseFirestore.getInstance()

                db.collection("replies")
                    .add(newPost)
                    .addOnSuccessListener { documentReference ->
                        Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")

                        if (userId != null) {

                            val userRef = db.collection("users").document(userId)

                            val sharedPref = getSharedPreferences(getString(R.string.user_points_key), Context.MODE_PRIVATE)
                            val userPoints = sharedPref.getString(getString(R.string.user_points_key), "0")

                            var points = userPoints.toString().toInt()

                            if (userPoints!!.isEmpty()) {
                                points = 100
                            } else {
                                points += 5
                            }
                            userRef.update("points", points)
                                .addOnSuccessListener { Log.d(TAG, "User points successfully updated!") }
                                .addOnFailureListener { e -> Log.w(TAG, "Error updating user points", e) }

                            myToast.cancel()
                        }

                        // redirect back to main page
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding document", e)
                        myToast.cancel()
                    }
            // val location = fusedLocationClient.lastLocation
            // val postLocation = GeoPoint(location.result!!.latitude, location.result!!.longitude)

            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
}
