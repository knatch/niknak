package com.example.nik_nak

import android.content.Context
import android.content.DialogInterface
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import android.util.Log
import android.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint


class NewPostActivity : AppCompatActivity() {

    private val TAG = "New Post"

    private val db = FirebaseFirestore.getInstance()

    private val words = listOf("bitch", "nigger", "nigga", "asshole", "fuck", "motherfucker", "doochbag", "idiot", "dick", "gay", "faggot", "handjob", "f**k", "bastard")

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
                var isClean = true
                var exists: Boolean

                for (word in words) {
                    val editTextLower = editText.toLowerCase()

                    exists = editTextLower.contains(word)
                    // Log.d(TAG, "word: $word, clean: $exists")
                    if (exists) isClean = false
                }

                if (!isClean) {
                    proceedDialog()
                    // val myToast = Toast.makeText(this, "You are posting inappropriate message.", Toast.LENGTH_SHORT)
                    // myToast.show()
                } else {
                    // display posting status
                    val myToast = Toast.makeText(this, "Sending...", Toast.LENGTH_SHORT)
                    myToast.show()

                    val currentTime = Timestamp.now()

                    val tag = findViewById<TextInputEditText>(R.id.tag_text)
                    // get string / remove white space
                    val tagText = tag.text.toString().toLowerCase().replace("\\s".toRegex(), "")

                    if (tagText.isNotEmpty()) {
                        val newTag = hashMapOf(
                            "data" to tagText,
                            "createdAt" to currentTime
                        )

                        db.collection("tags").add(newTag)
                            .addOnSuccessListener { documentReference ->  Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}") }
                            .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
                    }

                    val sharedPref = getSharedPreferences(getString(R.string.user_id_key), Context.MODE_PRIVATE)
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
                                "userId" to userId,
                                "tag" to tagText
                            )

                            db.collection("posts")
                                .add(newPost)
                                .addOnSuccessListener { documentReference ->
                                    Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")

                                    if (userId != null) {
                                        val userRef = db.collection("users").document(userId)

                                        val sharedPref = getSharedPreferences(getString(R.string.user_points_key), Context.MODE_PRIVATE)
                                        val userPoints = sharedPref.getString(getString(R.string.user_points_key),"0")
                                        var points = userPoints.toString().toInt()
                                        points += 5

                                        userRef.update("points", points)
                                            .addOnSuccessListener {
                                                Log.d(TAG, "User points successfully updated!")

                                                val editPref = sharedPref.edit()
                                                editPref.putString(getString(R.string.user_points_key), points.toString())
                                                editPref.apply()

                                                // redirect back to main page
                                                Handler().postDelayed({
                                                    myToast.cancel()
                                                    finish()
                                                }, 1000)
                                            }
                                            .addOnFailureListener { e ->
                                                Log.w(TAG, "Error updating user points", e)
                                            }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error adding document", e)
                                    myToast.cancel()
                                }
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

    private fun proceedDialog () {
        val dialogBuilder = AlertDialog.Builder(this)
        // set message of alert dialog
        dialogBuilder.setMessage("Your post contains offensive language. Your Yakarma point will be deducted. Are you sure you want to proceed?")
            // if the dialog is cancelable
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                    dialog, id -> updatePoints()
            })
            // negative button text and action
            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })

        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle("Warning")
        // show alert dialog
        alert.show()
    }

    private fun updatePoints () {

        val sharedPref = getSharedPreferences(getString(R.string.user_id_key), Context.MODE_PRIVATE)
        val userId = sharedPref.getString(getString(R.string.user_id_key), "")

        val pointSharedPref = getSharedPreferences(getString(R.string.user_points_key), Context.MODE_PRIVATE)
        val userPoints = pointSharedPref.getString(getString(R.string.user_points_key),"0")
        var points = userPoints.toString().toInt()
        Log.d(TAG, "updatePoints $points")
        if (points >= 10) {
            points -= 10
        } else {
            points = 0
        }
        Log.d(TAG, "updatePoints $points")
        val userRef = db.collection("users").document(userId.toString())
        userRef.update("points", points)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")
                val editPref = sharedPref.edit()
                editPref.putString(getString(R.string.user_points_key), points.toString())
                editPref.apply()

                // redirect back to main page
                Handler().postDelayed({
                    finish()
                }, 1000)
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }
}
