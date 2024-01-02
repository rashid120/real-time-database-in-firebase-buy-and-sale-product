package com.example.realtimedbtest.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.realtimedbtest.ProductViewActivity
import com.example.realtimedbtest.R
import com.example.realtimedbtest.interfaces.ProductsCanDel
import com.example.realtimedbtest.model.BuyProductModel

class OrderListAdapter(private val cxt: Context, private val orderList: List<BuyProductModel>, val canDel: ProductsCanDel): RecyclerView.Adapter<OrderListAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val image: ImageView = itemView.findViewById(R.id.showOrderPImage)
        val pName: TextView = itemView.findViewById(R.id.showOrderPName)
        val pPrice: TextView = itemView.findViewById(R.id.showOrderPPrice)
        val cancelBtn: TextView = itemView.findViewById(R.id.showOrderPCancelTV)
        val deleteBtn: TextView = itemView.findViewById(R.id.showOrderPDeleteTV)
        val orderStatus: TextView = itemView.findViewById(R.id.showOrderPStatus)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.order_list_view, parent, false))
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val p0 = orderList[position]

        Glide.with(cxt).load(p0.pimageUri).into(holder.image)
        holder.pName.text = p0.pname
        holder.pPrice.text = "₹ "+p0.pprice.toString()

        if (p0.buystatus == 1){

            holder.deleteBtn.visibility = View.VISIBLE
            holder.cancelBtn.visibility = View.GONE
            holder.orderStatus.text = "Order : Canceled"
        }else{
            holder.deleteBtn.visibility = View.GONE
            holder.cancelBtn.visibility = View.VISIBLE
            holder.orderStatus.text = "Order : Conform"
        }

        holder.cancelBtn.setOnClickListener {
            val alertDialog = AlertDialog.Builder(cxt)

            alertDialog.setTitle("Alert!")
            alertDialog.setMessage("What's your problem ?")
            alertDialog.setPositiveButton("Back"){_,_ ->}
            alertDialog.setNegativeButton("Cancel"){_,_ ->
                canDel.cancelProduct(p0.pid!!)
            }
            val create = alertDialog.create()
            create.show()
        }

        holder.deleteBtn.setOnClickListener {
            val alertDialog = AlertDialog.Builder(cxt)

            alertDialog.setTitle("Alert!")
            alertDialog.setMessage("Delete conform?")
            alertDialog.setPositiveButton("No"){_,_ ->}
            alertDialog.setNegativeButton("Delete"){_,_ ->
                canDel.deleteProduct(p0.pid!!)
            }
            val create = alertDialog.create()
            create.show()
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