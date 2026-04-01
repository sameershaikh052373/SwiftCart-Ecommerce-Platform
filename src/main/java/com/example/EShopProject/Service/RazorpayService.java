package com.example.EShopProject.Service;

import com.razorpay.*;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {

	private static final String KEY = "rzp_test_XhwcKLapXCzK69"; 
    private static final String SECRET = "GzZcBeUMwhjt9yQWrBF1RgE5"; 

    public String createRazorpayOrder(double amount) throws RazorpayException {
        RazorpayClient client = new RazorpayClient(KEY, SECRET);

        JSONObject options = new JSONObject();
        options.put("amount", (int)(amount * 100)); // Razorpay works in paise
        options.put("currency", "INR");
        options.put("receipt", "order_rcptid_" + System.currentTimeMillis());

        Order order = client.orders.create(options);
        return order.toString();
        // You can return orderId or the full object
    }
    
    public String getKey() {
        return KEY;
    }
}
