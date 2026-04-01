package com.example.EShopProject.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.EShopProject.Repository.OrderItemRepository;
import com.example.EShopProject.Repository.ProductRepository;
import com.example.EShopProject.entity.OrderItem;
import com.example.EShopProject.entity.Product;



@Service
public class ProductService {
	
	@Autowired
	private ProductRepository repo;
	
	@Autowired
	private OrderItemRepository orderItemRepository;
	
	public List<Product> getAllProduct()
	{
		return repo.findAll();
	}

	public void addProduct(Product newproduct)
	{
		repo.save(newproduct);
	}
	
	public void deleteProduct(Integer productId) {
		Product product = repo.findById(productId).orElse(null);

	    if (product != null) {
	        // 1. Find all order items referencing this product
	        List<OrderItem> itemsWithProduct = orderItemRepository.findByProduct(product);

	        // 2. Set their product reference to null
	        for (OrderItem item : itemsWithProduct) {
	            item.setProduct(null);
	        }

	        orderItemRepository.saveAll(itemsWithProduct);

	        // 3. Now delete the product safely
	        repo.delete(product);

	    }
	}
	
	public Product getSingleProduct(int id)
	{
		return repo.findById(id).get();
	}
	
	public void updateProduct(int id, Product uproduct)
	{
		uproduct.setId(id);
		repo.save(uproduct);
	}
	

	 public List<Product> searchProduct(String keyword) {
	        return repo.searchByKeyword(keyword);
	    }
	 
	 public List<Product> getAllProductsSorted(String sortBy) {
		 List<Product> products = repo.findAll();

		    
		    switch (sortBy) {
		        case "priceHigh":
		            products.sort(Comparator.comparingDouble(Product::getPrice).reversed());
		            break;
		        case "priceLow":
		            products.sort(Comparator.comparingDouble(Product::getPrice));
		            break;
		        case "nameAsc":
		            products.sort(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER));
		            break;
		    }

		    return products;
		}

}
