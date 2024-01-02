package com.example.realtimedbtest.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.realtimedbtest.ProductViewActivity
import com.example.realtimedbtest.R
import com.example.realtimedbtest.interfaces.ProductsInterface
import com.example.realtimedbtest.model.ProductModel
import com.google.android.material.imageview.ShapeableImageView
import kotlin.math.roundToInt

class ProductAdapter(val cxt: Context, val product: ArrayList<ProductModel>, val onclick: ProductsInterface): RecyclerView.Adapter<ProductAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val image: ShapeableImageView = itemView.findViewById(R.id.showPImage)
        val pName: TextView = itemView.findViewById(R.id.showPName)
        val price: TextView = itemView.findViewById(R.id.showPPrice)
        val buyBtn: AppCompatButton = itemView.findViewById(R.id.butProductBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.product_view, parent, false))
    }

    override fun getItemCount(): Int {
        return product.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val p0 = product[position]

        Glide.with(cxt).load(p0.pimageUri).into(holder.image)
        holder.pName.text = p0.pname.toString()
        holder.price.text = "₹ " + p0.pprice.toString()

        holder.buyBtn.setOnClickListener {
            val amount = (p0.pprice.toString().toFloat() * 100).roundToInt()
            onclick.buyProduct(amount, p0.pid!!)
        }

        holder.image.setOnClickListener {

            val intent = Intent(cxt, ProductViewActivity::class.java)
                intent.putExtra("pImage", p0.pimageUri.toString())
                intent.putExtra("pName", p0.pname.toString())
                intent.putExtra("pPrice", p0.pprice.toString())
            cxt.startActivity(intent)
        }
    }
}