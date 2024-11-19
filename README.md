# Paypal-Sdk-Test
An Android app demonstrating PayPal Web Checkout integration using WebView. It handles order creation, displays the PayPal UI in a WebView, and processes payment success or cancellation.

# Features
PayPal Order Creation: 
Create orders using PayPal's sandbox API.

WebView Integration: 
Load the PayPal checkout UI inside a WebView.

Redirect Handling:
Detect and handle success (returnUrl) and cancellation (cancelUrl) redirects.

Order Capture: 
Capture successful payments via the PayPal API.

# Requirements
PayPal Sandbox clientID and secretID.
Android Networking Library for API calls. (Option you can use retfrofit or volley)
