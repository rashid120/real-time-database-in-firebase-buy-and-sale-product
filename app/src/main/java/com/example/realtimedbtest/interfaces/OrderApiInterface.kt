package com.example.realtimedbtest.interfaces

import com.example.realtimedbtest.model.OrderApiReq
import com.example.realtimedbtest.model.OrderApiRes
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface OrderApiInterface {

//  https://api.razorpay.com/v1/orders

    @POST("orders")
    fun postOrderFun(@HeaderMap header: HashMap<String, String>, @Body orderDetails: OrderApiReq): Call<OrderApiRes>

    companion object Factory{
        fun createOrderRet(): OrderApiInterface{

            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.razorpay.com/v1/")
                .build()

            return (retrofit.create(OrderApiInterface::class.java))
        }
    }
}