package com.example.realtimedbtest

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.realtimedbtest.adapter.ProductAdapter
import com.example.realtimedbtest.interfaces.OrderApiInterface
import com.example.realtimedbtest.interfaces.ProductsInterface
import com.example.realtimedbtest.model.BuyProductModel
import com.example.realtimedbtest.model.Notes
import com.example.realtimedbtest.model.OrderApiReq
import com.example.realtimedbtest.model.OrderApiRes
import com.example.realtimedbtest.model.ProductModel
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), ProductsInterface, PaymentResultListener {

    //google authentication code
    private val signAuth = FirebaseAuth.getInstance().currentUser
    private var auth = FirebaseAuth.getInstance()
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var mainPage: ConstraintLayout
    private lateinit var signUpPage: ConstraintLayout

    // main page code

    private var imageUri: Uri? = null
    private val db = FirebaseDatabase.getInstance().getReference("Products")

    private lateinit var productList: ArrayList<ProductModel>
    private lateinit var adapter: ProductAdapter
    private var productLinkShow: TextView? = null

    private var buyPId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Start Google authentication code
        val googleBtn: SignInButton = findViewById(R.id.googleBtn)
        mainPage = findViewById(R.id.mainPage)
        signUpPage = findViewById(R.id.singUpPage)
        checkSignIn()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this) { connectionResult ->
                Toast.makeText(this, "Connection Failed: $connectionResult", Toast.LENGTH_SHORT).show()
            }
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        googleBtn.setOnClickListener {
            signIn()
        }
        // for signOut
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)

        // end Google Authentication


        // Start main page code
        productList = arrayListOf()
        getProducts()

        val toolbar: Toolbar = findViewById(R.id.mainToolbar)
        val recyclerView: RecyclerView = findViewById(R.id.MainRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        val firstName = signAuth?.displayName.toString().split(" ")
        toolbar.title = firstName[0]
        toolbar.setOnMenuItemClickListener {

            when(it.itemId){

                R.id.addProduct -> addProductFun()
                R.id.Profile -> profileData()
                R.id.logOut -> signOut()
                R.id.orderList -> startActivity(Intent(this, OrderListActivity::class.java))
            }
            true
        }

        adapter = ProductAdapter(this, productList, this)
        recyclerView.adapter = adapter
    }

    private fun addProductFun() {

        val dialog = BottomSheetDialog(this)
        val layout = LayoutInflater.from(this).inflate(R.layout.add_product, null, false)

        val productName: EditText = layout.findViewById(R.id.productNameET)
        val productPrice: EditText = layout.findViewById(R.id.productPriceET)
        val productImage: TextView = layout.findViewById(R.id.chooseProductImage)
        val submitBtn: AppCompatButton = layout.findViewById(R.id.productSubmitBtn)
        productLinkShow = layout.findViewById(R.id.productLink)

        dialog.setContentView(layout)

        productImage.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 313)
        }

        submitBtn.setOnClickListener {

            if (productName.text.isEmpty()||productPrice.text.isEmpty() || imageUri == null){

                if (imageUri == null){

                    Toast.makeText(this, "Please select image Product", Toast.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }

            val storage = Firebase.storage.getReference("ProductImage")
            val imageName = System.currentTimeMillis().toString()

            storage.child(imageName)
                .putFile(imageUri!!)
                .addOnSuccessListener { task ->
                    task.storage.downloadUrl.addOnSuccessListener { uri ->
                        val data = ProductModel(imageName, uri.toString(), productName.text.toString(), productPrice.text.toString().toLong())

                        db.child(imageName)
                            .setValue(data)
                            .addOnSuccessListener {

                                Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show()
                            }
                        productList.clear()
                    }
                }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getProducts(){

        FirebaseDatabase.getInstance().reference.child("Products")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        snapshot.children.forEach { dataSnapshot ->
                            val data = dataSnapshot.getValue(ProductModel::class.java)
                            if (data != null) {
                                productList.add(data)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun profileData(){

        val dialog = BottomSheetDialog(this)
        val layout = LayoutInflater.from(this).inflate(R.layout.show_profile, null, false)

        val name: TextView = layout.findViewById(R.id.profileName)
        val email: TextView = layout.findViewById(R.id.profileGmail)
        val image: ShapeableImageView = layout.findViewById(R.id.profileImage)
        dialog.setContentView(layout)

        name.text = signAuth?.displayName.toString()
        email.text = signAuth?.email.toString()

        if (signAuth?.photoUrl == null) image.setImageResource(R.drawable.baseline_account_circle_24)
        else Glide.with(this).load(signAuth.photoUrl).into(image)

        dialog.show()
    }

    private fun signOut(){

        val dialog = AlertDialog.Builder(this)

        dialog.setMessage("Are you sure you want to sign out from this account?")
        dialog.setTitle("Alert!")
        dialog.setNegativeButton("No"){_,_ ->}
        dialog.setPositiveButton("Yes"){_,_ ->

            googleSignInClient.signOut().addOnCompleteListener {
                if (it.isSuccessful) {
                    auth.signOut()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                    finish()
                }
            }
        }
        dialog.show()
    }

    // Google Authentication
    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, 313)
    }

    // Handle the result of the Google Sign-In
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 313 && data != null) {
            
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result != null) {
                if (result.isSuccess) {
                    
                    val account = result.signInAccount
                    firebaseAuthWithGoogle(account!!)
                } else {
                    Log.d("GoogleSignIn", "Google Sign-In failed")
                }
            }
        }

        if (requestCode == 313 && resultCode == Activity.RESULT_OK && data != null) {

            imageUri = data.data
            productLinkShow?.text = imageUri.toString()
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("GoogleSignIn", "signInWithCredential:success")
                    // Here you can handle the successful sign-in
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                    finish()
                }
                else {
                    Log.w("GoogleSignIn", "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun checkSignIn(){

        if (signAuth?.uid != null){

            mainPage.visibility = View.VISIBLE
            signUpPage.visibility = View.GONE
        }
        else{
            mainPage.visibility = View.GONE
            signUpPage.visibility = View.VISIBLE
        }
    }

    // create order id
    override fun buyProduct(pAmount: Int, pId: String) {

        val headerMap = HashMap<String, String>()
        val header = "Basic cnpwX3Rlc3RfVzJ1YWVseDJxTW5pWDY6TThrUE94N1NDU0xXTVFmMGxCR2R0dGM1"
        headerMap["Authorization"] = header

        val orderData = OrderApiReq(pAmount, "INR", Notes("", ""), "Receipt")
        val call = OrderApiInterface.createOrderRet().postOrderFun(headerMap, orderData)
        call.enqueue(object : Callback<OrderApiRes>{
            override fun onResponse(call: Call<OrderApiRes>, response: Response<OrderApiRes>) {
                if (response.isSuccessful){

                    doPayment(response.body()!!.id)

                    buyPId = pId
                }
            }

            override fun onFailure(call: Call<OrderApiRes>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.message.toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun doPayment(orderId: String){
        val checkout = Checkout()

        checkout.setKeyID("rzp_test_W2uaelx2qMniX6")

        try {

            val options = JSONObject()
            options.put("name", signAuth?.displayName)
            options.put("description", "description is empty")
            options.put("theme.color", "#0A560E")
            options.put("currency", "INR")
            options.put("order_id", orderId)

            val retryOptions = JSONObject()
            retryOptions.put("enable", true)
            retryOptions.put("max_count", 5)

            options.put("retry", retryOptions)

            options.put("prefill.email", signAuth?.email)
            options.put("prefill.contact", signAuth?.phoneNumber)

            checkout.open(this@MainActivity, options)

        }catch (e: Exception){

            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentSuccess(p0: String?) {

        Toast.makeText(this, "Payment successful", Toast.LENGTH_SHORT).show()
        orderProductFun(buyPId)
    }

    override fun onPaymentError(p0: Int, p1: String?) {

        Toast.makeText(this, p1 + p0, Toast.LENGTH_SHORT).show()
    }

    private fun orderProductFun(pId: String?){

        if (pId != null) { // pId == ek product ka details find hoga
            FirebaseDatabase.getInstance().reference.child("Products")
                .child(pId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val productData = snapshot.getValue(ProductModel::class.java)

                        if (productData != null){
                            val time = System.currentTimeMillis().toString() + " ${signAuth?.displayName}"

                            val buyProductModel = BuyProductModel(signAuth?.uid.toString(), time, productData.pimageUri, productData.pname, productData.pprice)

                            FirebaseDatabase.getInstance().getReference("SoldOutProducts")
                                .child(time)
                                .setValue(buyProductModel)
                                .addOnSuccessListener {

                                    Toast.makeText(this@MainActivity, "Order successful", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@MainActivity, OrderListActivity::class.java))
                                }
                                .addOnFailureListener {

                                    Toast.makeText(this@MainActivity, "Order canceled", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}