package com.example.nik_nak

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val TAG = "Main Activity"

    // Access a Cloud Firestore instance from your Activity
    private val db = FirebaseFirestore.getInstance()

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val data = Bundle()
        when (item.itemId) {
            R.id.navigation_home -> {
                loadFragment(HomeFragment(), data)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_peak -> {
                loadFragment(PeakFragment(), data)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_me -> {
                loadFragment(MeFragment(), data)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_more -> {
                loadFragment(MoreFragment(), data)
                return@OnNavigationItemSelectedListener true
            }
        }
        return@OnNavigationItemSelectedListener false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences(getString(R.string.user_id_key), Context.MODE_PRIVATE)
        val userId = sharedPref.getString(getString(R.string.user_id_key), "")

        if (userId!!.isEmpty()) {
            Log.i(TAG, "no user id -> create new user")

            registerNewUser(sharedPref)
        } else {
            Log.i(TAG, "user id exists -> fetch data from user $userId")

            fetchUserData(userId)
        }

        // set up toolbar
        setSupportActionBar(findViewById(R.id.main_toolbar))

        // update toolbar title
        supportActionBar?.title = "Nik Nak"

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        val data = Bundle()
        data.putString("type", "new")
        // initialize the page with home fragment
        loadFragment(HomeFragment(), data)

        checkLocationPermission()
    }

    private fun checkLocationPermission () {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            val permissionIntent = Intent(this, PermissionActivity::class.java)

            // Start the new activity.
            startActivity(permissionIntent)
        }
    }

    // set up menu in action bar
    override fun onCreateOptionsMenu (menu: Menu?): Boolean {
        // reference to res/menu/top_menu.xml
        menuInflater.inflate(R.menu.top_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    // actions on click top menu items
    override fun onOptionsItemSelected (item: MenuItem) = when (item.itemId) {
        R.id.action_create -> {
            // Create an Intent to start the NewPostActivity activity
            val newPostIntent = Intent(this, NewPostActivity::class.java)

            // Start the new activity
            startActivity(newPostIntent)
            true
        }
        R.id.action_points -> {
            // Create an Intent to start the UserPointActivity activity
            val userPointIntent = Intent(this, UserPointActivity::class.java)

            // Start the new activity
            startActivity(userPointIntent)
            true
        }
        R.id.action_hot -> {
            Log.i(TAG, "action_hot")
            val data = Bundle()
            data.putString("type", "hot")
            // initialize the page with home fragment
            loadFragment(HomeFragment(), data)

            true
        }
        R.id.action_new -> {
            Log.i(TAG, "action_new")
            val data = Bundle()
            data.putString("type", "new")
            // initialize the page with home fragment
            loadFragment(HomeFragment(), data)

            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun loadFragment (fragment: Fragment, bundle: Bundle) {

        fragment.arguments = bundle
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    private fun registerNewUser (sharedPref: SharedPreferences) {

        Log.i(TAG, "registerNewUser")

        val newUser = hashMapOf(
            "points" to 100,
            "gender" to "",
            "ageRange" to "",
            "isPrivate" to true,
            "createdAt" to Timestamp.now()
        )

        db.collection("users")
            .add(newUser)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")

                val editPref = sharedPref.edit()
                editPref.putString(getString(R.string.user_id_key), documentReference.id)
                editPref.apply()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error registering new user", e)
            }
    }

    private fun fetchUserData (userId: String) {
        val docRef = db.collection("users").document(userId)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: ${snapshot.data!!["points"]}")

                val points = snapshot.data!!["points"]?: 0

                // save user points in shared preference
                // TODO: fetch and update user points using real time database
                val sharedPref = getSharedPreferences(getString(R.string.user_points_key), Context.MODE_PRIVATE)
                val editPref = sharedPref.edit()
                editPref.putString(getString(R.string.user_points_key), points.toString())
                editPref.apply()

                val toolbar = this.findViewById<Toolbar>(R.id.main_toolbar)
                val menu = toolbar.menu
                menu.findItem(R.id.action_points)?.title = points.toString()
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }
}