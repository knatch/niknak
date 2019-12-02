package com.example.nik_nak

import android.content.Context
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlin.system.exitProcess


class NewPostActivity : AppCompatActivity() {

    private val TAG = "New Post"

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post)

        // set up toolbar
        setSupportActionBar(findViewById(R.id.main_toolbar))

        // update toolbar title
        supportActionBar?.title = "Create new post"

        // enable home navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // auto focus edit box when activity is initialized
        val edit = findViewById<EditText>(R.id.edit_text)
        edit.requestFocus()

        // TODO: toggle soft keyboard automatically

        // create location service client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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
            val edit = findViewById<EditText>(R.id.edit_text)
            val editText = edit.text.toString()
            if (editText.isBlank()) {
                val myToast = Toast.makeText(this, "Post content cannot be empty!", Toast.LENGTH_SHORT)
                myToast.show()
            } else {
                // display posting status
                val myToast = Toast.makeText(this, "Sending...", Toast.LENGTH_SHORT)
                myToast.show()

                val currentTime = Timestamp.now()

                val sharedPref =
                    getSharedPreferences(getString(R.string.user_id_key), Context.MODE_PRIVATE)
                val userId = sharedPref.getString(getString(R.string.user_id_key), "")

                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        // Got last known location. In some rare situations this can be null.
                        val postLocation = GeoPoint(location!!.latitude, location.longitude)

                        val newPost = hashMapOf(
                            "data" to editText,
                            "createdAt" to currentTime,
                            "location" to postLocation,
                            "points" to 0,
                            "userId" to userId
                        )
                        // Access a Cloud Firestore instance from your Activity
                        val db = FirebaseFirestore.getInstance()

                        db.collection("posts")
                            .add(newPost)
                            .addOnSuccessListener { documentReference ->
                                Log.d(
                                    TAG,
                                    "DocumentSnapshot written with ID: ${documentReference.id}"
                                )

                                if (userId != null) {

                                    val userRef = db.collection("users").document(userId)

                                    val sharedPref = getSharedPreferences(
                                        getString(R.string.user_points_key),
                                        Context.MODE_PRIVATE
                                    )
                                    val userPoints = sharedPref.getString(
                                        getString(R.string.user_points_key),
                                        "0"
                                    )

                                    var points = userPoints.toString().toInt()

                                    if (userPoints!!.isEmpty()) {
                                        points = 150
                                    } else {
                                        points += 5
                                    }
                                    userRef.update("points", points)
                                        .addOnSuccessListener {
                                            Log.d(
                                                TAG,
                                                "User points successfully updated!"
                                            )
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w(
                                                TAG,
                                                "Error updating user points",
                                                e
                                            )
                                        }

                                }

                                // redirect back to main page
                                Handler().postDelayed({
                                    myToast.cancel()
                                    finish()
                                }, 1000)
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error adding document", e)
                                myToast.cancel()
                            }
                    }
            }

            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
}
