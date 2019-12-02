package com.example.nik_nak

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class UserPointActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_point)

        // set up toolbar
        setSupportActionBar(findViewById(R.id.main_toolbar))

        // update toolbar title
        supportActionBar?.title = "Yakarma"

        // enable home navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // handle soft back button event
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
