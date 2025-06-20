package com.example.vgp235final

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vgp235final.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabAddShape.setOnClickListener {
            showShapeSelectionDialog()
        }
    }

    private fun showShapeSelectionDialog() {
        val shapeOptions = arrayOf("Rectangle", "Circle")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Please select a shape")
            .setItems(shapeOptions) { dialog, which ->
                val selectedType = when (which) {
                    0 -> ShapeType.RECTANGLE
                    1 -> ShapeType.CIRCLE
                    else -> throw IllegalArgumentException("Unknown shape")
                }

                binding.flowchartCanvas.addShape(selectedType)
            }

        builder.create().show()
    }
}