package com.example.EShopProject.Controller;

import com.example.EShopProject.entity.*;
import com.razorpay.RazorpayException;
import com.example.EShopProject.Repository.*;
import com.example.EShopProject.Service.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class ShopController {

    @Autowired private ProductService productService;
    @Autowired private ProductRepository productRepo;
    @Autowired private CategoryRepository categoryRepo;
    @Autowired private CartService cartService;
    @Autowired private UserRepository userRepository;
    @Autowired
    private RazorpayService razorpayService;

    @ModelAttribute
    public void commonData(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            int cartCount = cartService.getCartItems(currentUser).size();
            model.addAttribute("cartCount", cartCount);
        } else {
            model.addAttribute("cartCount", 0); // Prevent null in navbar
        }
    }

//    @GetMapping("/home")
//    public String home(Model model) {
//        model.addAttribute("products", productRepo.findAll());
//        return "index";
//    }
    
    @GetMapping("/home")
    public String home(@RequestParam(value = "sort", required = false) String sort, Model model) {
        List<Product> products = (sort == null)
            ? productRepo.findAll()
            : productService.getAllProductsSorted(sort);

        model.addAttribute("productlist", products); // ✅ fix this key
        return "index";
    }



    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("productId") int productId,
                            @RequestParam("quantity") int quantity,
                            HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/login";

        Product product = productService.getSingleProduct(productId);
        if (product.getQuantity() <= 0) {
            session.setAttribute("cartError", "Product is out of stock!");
            return "redirect:/home";
        }

        cartService.addToCart(currentUser, productId, quantity);
        return "redirect:/cart/view";
    }

    @GetMapping("/cart/view")
    public String viewCart(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/login";

        List<CartItem> cartItems = cartService.getCartItems(currentUser);
        double total = cartItems.stream().mapToDouble(CartItem::getTotalPrice).sum();

        model.addAttribute("cartProducts", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("orderForm", new OrderForm());

        return "cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/login";

        cartService.clearCart(currentUser);
        model.addAttribute("cartCount", 0); // force refresh
        return "redirect:/cart/view";
    }

    @PostMapping("/cart/checkout")
    public String checkout(@ModelAttribute("orderForm") OrderForm orderForm,
                           HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/login";

        cartService.checkout(currentUser, orderForm);
        model.addAttribute("cartCount", 0); // force refresh
        return "order_success";
    }


    @GetMapping("/products-by-category")
    public String productsByCategory(Model model) {
        model.addAttribute("categories", categoryRepo.findAll());
        return "products_by_category";
    }

//    @GetMapping("/category/{id}")
//    public String viewProductsInCategory(@PathVariable Integer id, Model model) {
//        Category category = categoryRepo.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));
//        model.addAttribute("category", category);
//        model.addAttribute("products", productRepo.findByCategory(category));
//        return "category_products";
//    }
    
    @GetMapping("/category/{id}")
    public String viewProductsInCategory(@PathVariable Integer id,
                                         @RequestParam(value = "sort", required = false) String sort,
                                         Model model) {
        Category category = categoryRepo.findById(id).orElseThrow();

        List<Product> products = productRepo.findByCategory(category);
        if (sort != null) {
            switch (sort) {
                case "priceHigh" -> products.sort(Comparator.comparingDouble(Product::getPrice).reversed());
                case "priceLow" -> products.sort(Comparator.comparingDouble(Product::getPrice));
                case "nameAsc" -> products.sort(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER));
            }
        }

        model.addAttribute("category", category);
        model.addAttribute("products", products);
        return "category_products";
    }


    @GetMapping("/product/search")
    public String searchProducts(@RequestParam("keyword") String keyword, Model model) {
        model.addAttribute("productlist", productService.searchProduct(keyword));
        model.addAttribute("keyword", keyword);
        return "index";
    }

    @GetMapping("/cart/remove/{productId}")
    public String removeItem(@PathVariable int productId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/login";

        cartService.removeItem(currentUser, productId);
        return "redirect:/cart/view";
    }

    @PostMapping("/cart/update")
    public String updateQuantity(@RequestParam("productId") int productId,
                                 @RequestParam("action") String action,
                                 HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) return "redirect:/login";

        List<CartItem> items = cartService.getCartItems(currentUser);
        CartItem item = items.stream()
                .filter(c -> c.getProduct().getId() == productId)
                .findFirst()
                .orElse(null);

        if (item != null) {
            int currentQty = item.getQuantity();
            if ("increase".equals(action)) {
                cartService.updateQuantity(currentUser, productId, currentQty + 1);
            } else if ("decrease".equals(action) && currentQty > 1) {
                cartService.updateQuantity(currentUser, productId, currentQty - 1);
            }
        }

        return "redirect:/cart/view";
    }

    @PostMapping("/cart/update-ajax")
    @ResponseBody
    public Map<String, Object> updateCartAjax(@RequestParam int productId,
                                              @RequestParam String action,
                                              HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("error", "User not logged in");
            return result;
        }

        List<CartItem> items = cartService.getCartItems(currentUser);
        CartItem item = items.stream()
                .filter(c -> c.getProduct().getId() == productId)
                .findFirst()
                .orElse(null);

        if (item != null) {
            int currentQty = item.getQuantity();
            if ("increase".equals(action)) {
                cartService.updateQuantity(currentUser, productId, currentQty + 1);
            } else if ("decrease".equals(action) && currentQty > 1) {
                cartService.updateQuantity(currentUser, productId, currentQty - 1);
            }
        }

        double total = items.stream().mapToDouble(CartItem::getTotalPrice).sum();

        result.put("success", true);
        result.put("quantity", item != null ? item.getQuantity() : 0);
        result.put("subtotal", item != null ? item.getTotalPrice() : 0.0);
        result.put("total", total);

        return result;
    }
    @GetMapping("/proceed-to-payment")
    public String paymentPage(Model model, HttpSession session) throws RazorpayException {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "redirect:/login";

        // Check if cart is empty
        List<CartItem> cartItems = cartService.getCartItems(user);
        if (cartItems.isEmpty()) {
            return "redirect:/cart/view";
        }

        OrderForm orderForm = (OrderForm) session.getAttribute("orderForm");
        if (orderForm == null) return "redirect:/address";

        double amount = cartService.getTotalPrice(user);

        String razorOrderJson = razorpayService.createRazorpayOrder(amount);
        JSONObject json = new JSONObject(razorOrderJson);

        model.addAttribute("razorpayOrderId", json.getString("id"));
        model.addAttribute("amount", (int)(amount * 100));
        model.addAttribute("key", razorpayService.getKey()); // Provide the Razorpay test key

        return "payment"; // payment.html
    }
    
    
//    @GetMapping("/proceed-to-payment")
//    public String paymentPage(Model model, HttpSession session)   {
//    	
//    	try {
//        User user = (User) session.getAttribute("currentUser");
//        if (user == null) return "redirect:/login";
//
//        // Check if cart is empty
//        List<CartItem> cartItems = cartService.getCartItems(user);
//        if (cartItems.isEmpty()) {
//            return "redirect:/cart/view";
//        }
//
//        OrderForm orderForm = (OrderForm) session.getAttribute("orderForm");
//        if (orderForm == null) return "redirect:/address";
//
//        double amount = cartService.getTotalPrice(user);
//
//        String razorOrderJson = razorpayService.createRazorpayOrder(amount);
//        JSONObject json = new JSONObject(razorOrderJson);
//
//        model.addAttribute("razorpayOrderId", json.getString("id"));
//        model.addAttribute("amount", (int)(amount * 100));
//        model.addAttribute("key", razorpayService.getKey()); // Provide the Razorpay test key
//
//        return "payment"; // payment.html
//    	}
//    	catch(RazorpayException e) {
//    		return "Server issue pls try again later";
//    	}
//    }


    @GetMapping("/order-success")
    public String orderSuccessPage() {
        return "order_success";
    }
    
 // Show address form page
    @GetMapping("/address")
    public String showAddressForm(Model model, HttpSession session) {
        model.addAttribute("orderForm", new OrderForm());
        return "address";
    }

    // Handle address form POST (store in session & redirect to payment)
    @PostMapping("/address")
    public String processAddressForm(@Valid @ModelAttribute("orderForm") OrderForm orderForm, HttpSession session) {
        session.setAttribute("orderForm", orderForm); // store in session for later
        return "redirect:/proceed-to-payment";
    }
    
    @PostMapping("/payment-success")
    @ResponseBody
    public String handlePaymentSuccess(@RequestBody Map<String, Object> razorpayData, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return "User not logged in";

        // 1. Retrieve orderForm and cart items from session
        OrderForm orderForm = (OrderForm) session.getAttribute("orderForm");
        if (orderForm == null) return "Address not found";

        List<CartItem> cartItems = cartService.getCartItems(user);
        if (cartItems.isEmpty()) return "Cart is empty";

        // 2. Save order using cartService.checkout()
        cartService.checkout(user, orderForm);

        // 3. Clear session values (optional but clean)
        session.removeAttribute("orderForm");

        return "success"; // can return JSON if needed
    }



}
