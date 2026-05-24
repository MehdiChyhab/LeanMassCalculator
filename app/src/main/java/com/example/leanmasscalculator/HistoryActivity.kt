package com.example.leanmasscalculator

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.leanmasscalculator.databinding.ActivityHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val items = mutableListOf<HistoryAdapter.HistoryItem>()
        adapter = HistoryAdapter(items) { item, position ->
            deleteItem(item, position)
        }

        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter

        loadHistory(items)
    }

    private fun loadHistory(items: MutableList<HistoryAdapter.HistoryItem>) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("history")
            .orderBy("date")
            .get()
            .addOnSuccessListener { documents ->
                items.clear()
                for (doc in documents) {
                    val item = HistoryAdapter.HistoryItem(
                        id = doc.id,
                        date = doc.getString("date") ?: "",
                        gender = doc.getString("gender") ?: "",
                        weight = doc.getDouble("weight") ?: 0.0,
                        height = doc.getDouble("height") ?: 0.0,
                        lbm = doc.getDouble("lbm") ?: 0.0,
                        status = doc.getString("status") ?: ""
                    )
                    items.add(item)
                }
                adapter.notifyDataSetChanged()

                if (items.isEmpty()) {
                    Toast.makeText(this, "Aucun historique disponible", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteItem(item: HistoryAdapter.HistoryItem, position: Int) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("history")
            .document(item.id)
            .delete()
            .addOnSuccessListener {
                adapter.removeItem(position)
                Toast.makeText(this, "Supprimé avec succès", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur de suppression", Toast.LENGTH_SHORT).show()
            }
    }
}