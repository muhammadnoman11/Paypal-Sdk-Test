package com.example.paypalsdktest

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.example.paypalsdktest.databinding.ActivityMainBinding
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.Environment
import com.paypal.android.corepayments.PayPalSDKError
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutListener
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutRequest
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutResult
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    private val clientID = "" // add yours

    private val secretID = "" // add yours

    companion object {
         const val TAG = "MyTag"
         const val REQUEST_CODE_PAYPAL = 99

//        const val returnUrl = "your package name://paypaldemo"
//        const val cancelUrl = "your package name://cancel"

         const val returnUrl = "com.example.paypalsdktest://paypaldemo"
         const val cancelUrl = "com.example.paypalsdktest://cancel"

    }


    var accessToken = ""

    private lateinit var uniqueId: String

    private var orderid = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AndroidNetworking.initialize(applicationContext)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        fetchAccessToken()

        binding.startOrderBtn.setOnClickListener {
            val amount  = binding.amountText.text.toString()
            if (amount.isNotEmpty()) {
                startOrder(amount)
            }else{
                Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handlerOrderID(orderID: String) {
//        val config = CoreConfig(clientID, environment = Environment.SANDBOX)
//
//        val payPalWebCheckoutClient = PayPalWebCheckoutClient(this@MainActivity, config, returnUrl)
//        payPalWebCheckoutClient.listener = object : PayPalWebCheckoutListener {
//            override fun onPayPalWebSuccess(result: PayPalWebCheckoutResult) {
//                Log.d(TAG, "onPayPalWebSuccess: $result")
//            }
//
//            override fun onPayPalWebFailure(error: PayPalSDKError) {
//                Log.d(TAG, "onPayPalWebFailure: $error")
//            }
//
//            override fun onPayPalWebCanceled() {
//                Log.d(TAG, "onPayPalWebCanceled: ")
//            }
//        }

        orderid = orderID
//        val payPalWebCheckoutRequest =
//            PayPalWebCheckoutRequest(orderID, fundingSource = PayPalWebCheckoutFundingSource.PAYPAL)
//        payPalWebCheckoutClient.start(payPalWebCheckoutRequest)

        val checkoutUrl = "https://www.sandbox.paypal.com/checkoutnow?token=$orderID"

        val intent = Intent(this, PaypalWebViewActivity::class.java)
        intent.putExtra("url", checkoutUrl)
        startActivityForResult(intent, REQUEST_CODE_PAYPAL)


    }

    private fun startOrder(amount: String) {
        uniqueId = UUID.randomUUID().toString()

        val orderRequestJson = JSONObject().apply {
            put("intent", "CAPTURE")
            put("purchase_units", JSONArray().apply {
                put(JSONObject().apply {
                    put("reference_id", uniqueId)
                    put("amount", JSONObject().apply {
                        put("currency_code", "USD")
//                        put("value", "5.00")
                        put("value", amount)
                    })
                })
            })
            put("payment_source", JSONObject().apply {
                put("paypal", JSONObject().apply {
                    put("experience_context", JSONObject().apply {
                        put("payment_method_preference", "IMMEDIATE_PAYMENT_REQUIRED")
                        put("brand_name", "Test Mode Developer")
                        put("locale", "en-US")
                        put("landing_page", "LOGIN")
                        put("shipping_preference", "NO_SHIPPING")
                        put("user_action", "PAY_NOW")
                        put("return_url", returnUrl)
//                        put("cancel_url", "https://example.com/cancelUrl")
                        put("cancel_url", cancelUrl)
                    })
                })
            })
        }

        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v2/checkout/orders")
            .addHeaders("Authorization", "Bearer $accessToken")
            .addHeaders("Content-Type", "application/json")
            .addHeaders("PayPal-Request-Id", uniqueId)
            .addJSONObjectBody(orderRequestJson)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d(TAG, "Order Response : " + response.toString())
                    handlerOrderID(response.getString("id"))
                }

                override fun onError(error: ANError) {
                    Log.d(
                        TAG,
                        "Order Error : ${error.message} || ${error.errorBody} || ${error.response}"
                    )
                }
            })
    }

    private fun fetchAccessToken() {

        binding.progress.visibility = View.VISIBLE

        val authString = "$clientID:$secretID"
        val encodedAuthString = Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)

        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v1/oauth2/token")
            .addHeaders("Authorization", "Basic $encodedAuthString")
            .addHeaders("Content-Type", "application/x-www-form-urlencoded")
            .addBodyParameter("grant_type", "client_credentials")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    accessToken = response.getString("access_token")
                    Log.d(TAG, accessToken)

                    Toast.makeText(this@MainActivity, "Access Token Fetched!", Toast.LENGTH_SHORT)
                        .show()

                    binding.progress.visibility = View.GONE
                }

                override fun onError(error: ANError) {
                    Log.d(TAG, error.errorBody)
                    Toast.makeText(this@MainActivity, "Error Occurred!", Toast.LENGTH_SHORT).show()
                }
            })
    }

//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//        Log.d(TAG, "onNewIntent: $intent")
//        if (intent.data!!.getQueryParameter("opType") == "payment") {
//            captureOrder(orderid)
//        } else if (intent.data!!.getQueryParameter("opType") == "cancel") {
//            Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show()
//        }
//    }

    private fun captureOrder(orderID: String) {
        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v2/checkout/orders/$orderID/capture")
            .addHeaders("Authorization", "Bearer $accessToken")
            .addHeaders("Content-Type", "application/json")
            .addJSONObjectBody(JSONObject()) // Empty body
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d(TAG, "Capture Response : " + response.toString())
                    Toast.makeText(this@MainActivity, "Payment Successful", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: ANError) {
                    // Handle the error
                    Log.e(TAG, "Capture Error : " + error.errorDetail)
                }
            })
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PAYPAL) {
            when (resultCode) {
                RESULT_OK -> {
                    val isSuccess = data?.getBooleanExtra("isSuccess", false) ?: false
                    if (isSuccess) {
                        captureOrder(orderid) // Capture the order
                    } else {
                        Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show()
                    }
                }
                RESULT_CANCELED -> {
                    Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}