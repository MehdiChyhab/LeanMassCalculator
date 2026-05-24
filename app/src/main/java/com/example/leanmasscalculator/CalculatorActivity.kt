package com.example.leanmasscalculator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.leanmasscalculator.databinding.ActivityCalculatorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalculatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalculatorBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnCalculate.setOnClickListener {
            calculate()
        }

        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun calculate() {
        val weightStr = binding.etWeight.text.toString().trim()
        val heightStr = binding.etHeight.text.toString().trim()

        if (weightStr.isEmpty() || heightStr.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            return
        }

        val weight = weightStr.toDouble()
        val height = heightStr.toDouble()
        val isMale = binding.rbMale.isChecked

        // Formule de Boer
        val lbm = if (isMale) {
            (0.407 * weight) + (0.267 * height) - 19.2
        } else {
            (0.252 * weight) + (0.473 * height) - 48.3
        }

        // Normes
        val minLbm = if (isMale) 38.0 else 24.0
        val isOk = lbm >= minLbm

        // Affichage résultat
        binding.layoutResult.visibility = View.VISIBLE
        binding.tvResultValue.text = String.format("LBM = %.2f kg", lbm)

        if (isOk) {
            binding.tvResultStatus.text = "Résultat satisfaisant ✓"
            binding.tvResultStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            binding.ivResultIcon.setImageResource(android.R.drawable.checkbox_on_background)
        } else {
            binding.tvResultStatus.text = "Résultat à surveiller ⚠"
            binding.tvResultStatus.setTextColor(getColor(android.R.color.holo_orange_dark))
            binding.ivResultIcon.setImageResource(android.R.drawable.ic_dialog_alert)
        }

        // Sauvegarder dans Firestore
        saveToFirestore(weight, height, isMale, lbm, isOk)
    }

    private fun saveToFirestore(
        weight: Double,
        height: Double,
        isMale: Boolean,
        lbm: Double,
        isOk: Boolean
    ) {
        val userId = auth.currentUser?.uid ?: return
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        val record = hashMapOf(
            "date" to date,
            "weight" to weight,
            "height" to height,
            "gender" to if (isMale) "Homme" else "Femme",
            "lbm" to lbm,
            "status" to if (isOk) "Satisfaisant" else "À surveiller"
        )

        db.collection("users")
            .document(userId)
            .collection("history")
            .add(record)
            .addOnSuccessListener {
                Toast.makeText(this, "Résultat sauvegardé", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erreur de sauvegarde", Toast.LENGTH_SHORT).show()
            }
    }
}