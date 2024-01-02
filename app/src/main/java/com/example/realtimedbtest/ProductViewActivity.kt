package com.example.realtimedbtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide

class ProductViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_view)

        val pImage: ImageView = findViewById(R.id.productImage)
        val pName: TextView = findViewById(R.id.productName)
        val pPrice: TextView = findViewById(R.id.productPrice)
        val backBtn: AppCompatButton = findViewById(R.id.backHomeBtn)

        backBtn.setOnClickListener {
            onBackPressed()
            finish()
        }

        val imageUrl = intent.extras?.getString("pImage")
        val productName = intent.extras?.getString("pName")
        val productPrice = intent.extras?.getString("pPrice")

        Glide.with(this).load(imageUrl).into(pImage)
        pName.text = productName.toString()
        pPrice.text = "₹ " + productPrice.toString()

    }
}