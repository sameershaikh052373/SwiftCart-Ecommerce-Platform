package com.example.EShopProject.Controller;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.EShopProject.Repository.CategoryRepository;
import com.example.EShopProject.Repository.OrderRepository;
import com.example.EShopProject.Repository.ProductRepository;
import com.example.EShopProject.Repository.SuggestionRepository;
import com.example.EShopProject.Service.ProductService;
import com.example.EShopProject.entity.Category;
import com.example.EShopProject.entity.Order;
import com.example.EShopProject.entity.Product;
import com.example.EShopProject.entity.Suggestion;

@Controller
public class AdminController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService service;

    @Autowired
    private SuggestionRepository suggestionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    // Admin dashboard
//    @GetMapping("/admin/home")
//    public String showAdminHomePage(Model m) {
//        Collection<Product> plist = service.getAllProduct();
//        m.addAttribute("productlist", plist);
//        return "admin/index";
//    }
    
    @GetMapping("/admin/home")
    public String showAdminHomePage(@RequestParam(value = "sort", required = false) String sort, Model m) {
        List<Product> products = (sort == null) ?
                service.getAllProduct() :
                service.getAllProductsSorted(sort);

        m.addAttribute("productlist", products);
        return "admin/index";
    }


    // Show Add Product
    @GetMapping("/admin/add")
    public String showAddProductForm(Model m) {
        Product p = new Product();
        m.addAttribute("newproduct", p);
        return "/admin/addproduct";
    }

    // Handle Add Product
    @PostMapping("/admin/add")
    public String addProduct(@ModelAttribute Product newproduct) {
        service.addProduct(newproduct);
        return "redirect:/admin/home";
    }

    // Delete Product
    @GetMapping("/admin/delete/{id}")
    public String deleteProduct(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            service.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "✅ Product deleted successfully.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error", "❌ Cannot delete this product. It is part of one or more existing orders.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "⚠️ Unexpected error occurred while deleting the product.");
        }
        return "redirect:/admin/home";
    }

    // Show Update Product
    @GetMapping("/admin/update/{id}")
    public String showUpdatePage(@PathVariable Integer id, Model m) {
        Product p = service.getSingleProduct(id);
        m.addAttribute("product", p);
        return "/admin/updateproduct";
    }

    // Handle Update Product
    @PostMapping("/admin/update/{id}")
    public String updateProduct(@PathVariable Integer id, @ModelAttribute Product uproduct) {
        service.updateProduct(id, uproduct);
        return "redirect:/admin/home";
    }

    // View All Orders
    @GetMapping("/admin/all-orders")
    public String viewAllOrders(Model model) {
        List<Order> allOrders = orderRepository.findAll();
        model.addAttribute("orders", allOrders);
        return "admin/admin_orders";
    }

    // View Suggestions
    @GetMapping("/admin/view-suggestions")
    public String viewAllSuggestions(Model model) {
        List<Suggestion> suggestions = suggestionRepository.findAll();
        model.addAttribute("suggestions", suggestions);
        return "admin/admin_suggestions";
    }

    // View products of a category (clicked from admin_categories)
//    @GetMapping("/admin/category/{id}")
//    public String viewAdminCategory(@PathVariable Integer id, Model model) {
//        Category category = categoryRepository.findById(id).orElseThrow();
//        List<Product> products = productRepository.findByCategory(category);
//        model.addAttribute("category", category);
//        model.addAttribute("products", products);
//        return "admin/admin_category_products";
//    }
    
    @GetMapping("/admin/category/{id}")
    public String viewAdminCategory(@PathVariable Integer id,
                                    @RequestParam(value = "sort", required = false) String sort,
                                    Model model) {
        Category category = categoryRepository.findById(id).orElseThrow();
        List<Product> products = productRepository.findByCategory(category);

        if (sort != null) {
            switch (sort) {
                case "priceHigh" -> products.sort(Comparator.comparingDouble(Product::getPrice).reversed());
                case "priceLow" -> products.sort(Comparator.comparingDouble(Product::getPrice));
                case "nameAsc" -> products.sort(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER));
            }
        }

        model.addAttribute("category", category);
        model.addAttribute("products", products);
        return "admin/admin_category_products";
    }


    // ✅ Step 1: Admin Categories (grid style, with "Manage Categories" button)
    @GetMapping("/admin/categories")
    public String viewAllAdminCategories(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "admin/admin_categories";
    }

    // ✅ Step 2: Manage Categories Page (edit/delete/add)
    @GetMapping("/admin/manage-categories")
    public String showManageCategories(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "admin/manage_categories";
    }

    // Show Add Category Form
    @GetMapping("/admin/category/add")
    public String showAddCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/add_category";
    }

    // Handle Add Category Form
    @PostMapping("/admin/category/add")
    public String addCategory(@ModelAttribute("category") Category category, RedirectAttributes redirectAttributes) {
        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("success", "✅ Category added successfully.");
        return "redirect:/admin/manage-categories";
    }

    // Show Edit Category Form
    @GetMapping("/admin/category/edit/{id}")
    public String showEditCategoryForm(@PathVariable Integer id, Model model) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));
        model.addAttribute("category", category);
        return "admin/edit_category";
    }

    // Handle Edit Category Form
    @PostMapping("/admin/category/edit/{id}")
    public String updateCategory(@PathVariable Integer id, @ModelAttribute("category") Category updatedCategory, RedirectAttributes redirectAttributes) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));

        category.setName(updatedCategory.getName());
        categoryRepository.save(category);

        redirectAttributes.addFlashAttribute("success", "✅ Category updated successfully.");
        return "redirect:/admin/manage-categories";
    }

    // Handle Category Deletion
    @GetMapping("/admin/category/delete/{id}")
    public String deleteCategory(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            categoryRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "🗑️ Category deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Cannot delete category. It might be in use.");
        }
        return "redirect:/admin/manage-categories";
    }

}
