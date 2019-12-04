package com.example.nik_nak

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter (private val context: Activity,
                   private val title: Array<String?>,
                   private val points: Array<String?>,
                   private val postId: Array<String?>,
                   private val postUserId: Array<String?>)
    : ArrayAdapter<String>(context, R.layout.list_post, title) {

    private val TAG = "Post Adapter"
    private val db = FirebaseFirestore.getInstance()

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.list_post, null, true)

        val pointText = rowView.findViewById<TextView>(R.id.points)
        pointText.tag = postUserId[position]
        pointText.text = points[position]

        val titleText = rowView.findViewById<TextView>(R.id.title)
        titleText.text = title[position]
        titleText.tag = postId[position]
        titleText.setOnClickListener {
            val newIntent = Intent(context, PostDetailActivity::class.java)
            newIntent.putExtra("postId", postId[position])
            newIntent.putExtra("postUserId", postUserId[position])
            context.startActivity(newIntent)
        }

        val id = postId[position].toString()
        val postRef = db.collection("posts").document(id)

        val likeBtn = rowView.findViewById<ImageButton>(R.id.button_like)
        likeBtn.setOnClickListener {
            val point = points[position]?.toInt()

            Toast.makeText(context, "Liked me id: $id $point", Toast.LENGTH_SHORT).show()

            postRef
                .update("points", point?.plus(1))
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully updated!")
                    points[position] = point?.plus(1).toString()
                    pointText.text = points[position]
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
        }

        val disLikeBtn = rowView.findViewById<ImageButton>(R.id.button_dislike)
        disLikeBtn.setOnClickListener {
            val point = points[position]?.toInt()

            Toast.makeText(context, "Disliked me id: ${postId[position]}", Toast.LENGTH_SHORT).show()

            postRef
                .update("points", point?.minus(1))
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully updated!")
                    points[position] = point?.minus(1).toString()
                    pointText.text = points[position]
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
        }

        val sharedPref = getContext().getSharedPreferences(context.getString(R.string.user_id_key), Context.MODE_PRIVATE)
        val userId = sharedPref.getString(context.getString(R.string.user_id_key), "")

        if (postUserId[position].equals(userId)) {
            likeBtn.visibility = View.GONE
            disLikeBtn.visibility = View.GONE
        }
        return rowView
    }
}