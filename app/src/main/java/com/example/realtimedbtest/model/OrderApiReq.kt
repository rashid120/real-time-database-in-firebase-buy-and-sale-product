package com.example.realtimedbtest.model

data class OrderApiReq(
    val amount: Int,
    val currency: String,
    val notes: Notes,
    val receipt: String
)
