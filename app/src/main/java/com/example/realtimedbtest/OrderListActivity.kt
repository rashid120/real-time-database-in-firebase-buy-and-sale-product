package com.example.realtimedbtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.realtimedbtest.adapter.OrderListAdapter
import com.example.realtimedbtest.interfaces.ProductsCanDel
import com.example.realtimedbtest.model.BuyProductModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OrderListActivity : AppCompatActivity(), ProductsCanDel {

    private val auth = FirebaseAuth.getInstance().currentUser
    private lateinit var orderList: ArrayList<BuyProductModel>
    private lateinit var orderListAdapter: OrderListAdapter
    private lateinit var emptyText: TextView
    private val realTimeDb = FirebaseDatabase.getInstance().reference.child("SoldOutProducts")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_list)

        val recyclerView: RecyclerView = findViewById(R.id.orderListRecyclerView)
        val toolbar: Toolbar = findViewById(R.id.orderListToolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
            finish()
        }

        orderList = arrayListOf()
        getBuyProduct()
        recyclerView.layoutManager = GridLayoutManager(this, 1)
        orderListAdapter = OrderListAdapter(this, orderList, this)
        recyclerView.adapter = orderListAdapter

        emptyText = findViewById(R.id.orderListEmptyTV)
    }

    private fun getBuyProduct(){

        realTimeDb.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    orderList.clear()
                    if (snapshot.exists()){
                        snapshot.children.forEach {

                            val products = it.getValue(BuyProductModel::class.java)
                            if (products?.userid == auth?.uid) {
                                orderList.add(products!!)
                                orderListAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    if (orderList.isEmpty() || orderList.size <= 0){
                        emptyText.visibility = View.VISIBLE
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@OrderListActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun cancelProduct(pId: String) {

       val updateData = HashMap<String, Int>()
        updateData["buystatus"] = 1

        realTimeDb.child(pId)
            .updateChildren(updateData as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {

                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun deleteProduct(pId: String) {
        realTimeDb.child(pId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Delete", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {

                Toast.makeText(this, "Something wrong", Toast.LENGTH_SHORT).show()
            }
    }
}