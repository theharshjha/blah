==============================config=============================
package com.examly.springapp.config;

import java.sql.*;

import public class JdbcUtils {

private static final String url = "jdbc:mysql://localhost:3306/appdb";

    private static final String user = "root";

    private static final String password = "examly";

    

    public static Connection getConnection() throws SQLException{

        return DriverManager.getConnection(url, user, password);

    }

}

public class JdbcUtils {

    private static final String url = "jdbc:mysql://localhost:3306/appdb";

    private static final String user = "root";

    private static final String password = "examly";

    public static Connection getConnection() throws SQLException{


        return DriverManager.getConnection(url, user, password);

    }SQLException;

public class JdbcUtils {
    private static final String url = "jdbc:mysql://localhost:3306/appdb";
    private static final String user = "root";
    private static final String password = "examly";
    
    public static Connection getConnection() throws SQLException{

        return DriverManager.getConnection(url, user, password);

    }

   
    
}
===========================================dao ===================================
package com.examly.springapp.dao;

import java.util.List;

import com.examly.springapp.model.Product;

public interface ProductDAO {

    boolean createProduct(Product product);

    Product getProductById(int id);

    List<Product> updateProductByCategory(String category, double newPrice, int newStock);
    List<Product> deleteProductByPrice( double priceThreshold);
    List<Product> viewProductDetailsByCategory( String category);

}
=================================== in memorydao======================================
package com.examly.springapp.dao;

import java.util.List;

import com.examly.springapp.exception.LowStockException;
import com.examly.springapp.model.Product;

public interface ProductInMemoryDAO {

    Product createProduct(Product product) throws LowStockException;

    Product getProductById(int id) throws LowStockException;

    List<Product> updateProductByCategory(String category, double newPrice, int newStock) throws LowStockException;

    List<Product> deleteProductByPrice(double priceThreshold);
    
    List<Product> viewProductDetailsByCategory(String category);


    
}

==============================ProductDAOImpl=======================================================
package com.examly.springapp.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.examly.springapp.config.JdbcUtils;
import com.examly.springapp.exception.LowStockException;
import com.examly.springapp.model.Product;

public class ProductDAOImpl {
    public boolean createProduct(Product p)  throws SQLException{
        String sql = "insert into products values(?,?,?,?,?,?)";
        try (Connection con = JdbcUtils.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getProductId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getCategory());
            ps.setDouble(4, p.getPrice());
            ps.setInt(5, p.getStock());
            ps.setBoolean(6, p.isVerified());

           return ps.executeUpdate()==1 ;}

    

    }

    public Product getProductById(int productId) throws SQLException{
        String sql = "select * from products where productId=?";
        try (Connection con = JdbcUtils.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                    return mapRow(rs);

                

            }}
       
    }

    public List<Product> updateProductByCategory(String category, double newPrice, int newStock) throws SQLException{
        String sql = "update products set price=?, stock=? where category=?";
        try (Connection con = JdbcUtils.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, newPrice);
            ps.setInt(2, newStock);
            ps.setString(3, category);
            ps.executeUpdate();
                }
        return viewProductDetailsByCategory(category);
    }

    public List<Product> deleteProductByPrice(double priceThreshold) throws SQLException {
        List<Product> list = new ArrayList<>();
        String select = "select * from products where price<?";
        String sql = "delete from products where price<?";
        try (Connection con = JdbcUtils.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(select)) {
                ps.setDouble(1, priceThreshold);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRow(rs));

                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDouble(1, priceThreshold);
                ps.executeUpdate();

            }

        } 
        return list;

    }

    public List<Product> viewProductDetailsByCategory(String category)throws SQLException{
        String sql = "select * from products where category=? order by name asc";
        List<Product> list = new ArrayList<>();
        try (Connection con = JdbcUtils.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, category);
            try(ResultSet rs = ps.executeQuery()){
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        
        }
        return list;

    }

    private Product mapRow(ResultSet rs) throws SQLException {
        try {
            return new Product(
                rs.getInt("productId"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getDouble("price"),
                rs.getInt("stock"),
                rs.getBoolean("Verified")

            );
        } catch (LowStockException e) {
            // TODO Auto-generated catch block
            throw new SQLException();
        } 
    }
    
}

===========================================ProductInMemoryDAOImpl===============================================
package com.examly.springapp.dao.impl;

import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;

import com.examly.springapp.dao.ProductInMemoryDAO;
import com.examly.springapp.exception.LowStockException;
import com.examly.springapp.model.Product;

public class ProductInMemoryDAOImpl implements ProductInMemoryDAO {

    private List<Product> productDatabase = new ArrayList<>();

      @Override
      public Product createProduct(Product product) throws LowStockException{
          if (product.getStock() < 10 ) {
              throw new LowStockException("Stock is low for the product");   
          }
          productDatabase.add(product);
          return product;

      }

    @Override
    public Product getProductById(int id) throws LowStockException{

        return productDatabase.stream()
        .filter(p -> p.getProductId() == id)
        .findFirst()
        .orElseThrow(() -> new LowStockException("Stock is low for the product"));

    }

    @Override
    public List<Product> updateProductByCategory(String category, double newPrice, int newStock) throws LowStockException {
        if (newStock < 10) {
            throw new LowStockException("Stock is low for the product");
        }
            for(Product p : productDatabase){
                if (p.getCategory().equalsIgnoreCase(category)) {
                    p.setPrice(newPrice);
                    p.setStock(newStock);
                   
                   
                }
            
            
        }
        return viewProductDetailsByCategory(category);
    }

    @Override
    public List<Product> deleteProductByPrice(double priceThreshold){
        productDatabase.removeIf(p ->p.getPrice() < priceThreshold);
        return productDatabase;
    }

    @Override
    public List<Product> viewProductDetailsByCategory(String category){
        return productDatabase.stream()
            .filter(p->p.getCategory().equalsIgnoreCase(category))
            .sorted((a,b)->a.getName().compareToIgnoreCase(b.getName()))
            .collect(Collectors.toList());
    }
    
}

=======================================================LowstockException======================

package com.examly.springapp.exception;

public class LowStockException extends Exception {
    public LowStockException(String msg){
        super(msg);
    }
    
}

=================================Products===Entity===================
package com.examly.springapp.model;

import com.examly.springapp.exception.LowStockException;

public class Product {
    private int productId;
    private String name;
    private String category;
    private double price;
    private int stock;
    private boolean verified;

    
    public Product() {
       
    }

    public Product(int productId, String name, String category, double price, int stock, boolean verified) throws LowStockException {

         if (stock <10){
         
            throw new LowStockException("Stock is low ");

         }


        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.verified = verified;
    }

    

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public boolean isVerified() {
        return verified;
    }

    @Override
    public String toString() {
        return "Product ID: " + productId + ", Name: " + name + ", Category: " + category + ", Price: " + price
                + ", Stock: " + stock + ", Verified: " + verified;
    }

    

    

    
}


====================================ProductinMemoryService===========================

package com.examly.springapp.service;

import java.util.List;

import com.examly.springapp.exception.LowStockException;
import com.examly.springapp.model.Product;

public interface ProductInMemoryService {

    Product createProduct(Product product) throws LowStockException;
    Product getProductById(int id) throws LowStockException;

    List<Product> updateProductByCategory(String category, double newPrice, int newStock) throws LowStockException;

    List<Product> deleteProductByPrice(double priceThreshold);
    
    List<Product> viewProductDetailsByCategory(String category);


    
}
=======================================product service=========================

package com.examly.springapp.service;

public class ProductService {
    
}
=====================================ProductInMemoryServiceImpl======================

package com.examly.springapp.service.impl;

import java.util.List;

// import com.examly.springapp.dao.ProductInMemoryDAO;
import com.examly.springapp.dao.impl.ProductInMemoryDAOImpl;
import com.examly.springapp.exception.LowStockException;
import com.examly.springapp.model.Product;
import com.examly.springapp.service.ProductInMemoryService;

public class ProductInMemoryServiceImpl implements ProductInMemoryService{

    private final ProductInMemoryDAOImpl productDAO;

    public ProductInMemoryServiceImpl(){
        this.productDAO = new ProductInMemoryDAOImpl();
    }

    @Override
    public Product createProduct(Product product) throws LowStockException {
       return productDAO.createProduct(product);
        
    }

    @Override
    public List<Product> deleteProductByPrice(double priceThreshold) {
       return productDAO.deleteProductByPrice(priceThreshold);
        
       
        
    }

    @Override
    public Product getProductById(int id) throws LowStockException {
       
        return productDAO.getProductById(id);
    }

    @Override
    public List<Product> updateProductByCategory(String category, double newPrice, int newStock)
            throws LowStockException {
        
        return productDAO.updateProductByCategory(category, newPrice, newStock);
    }

    @Override
    public List<Product> viewProductDetailsByCategory(String category) {
        return productDAO.viewProductDetailsByCategory(category);
    }

    
}
=========================================ProductServiceImpl================

package com.examly.springapp.service.impl;

public class ProductServiceImpl {
    
}

===========================================SpringApplication========================

package com.examly.springapp;


import java.util.List;
import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.examly.springapp.exception.LowStockException;
import com.examly.springapp.model.Product;
import com.examly.springapp.service.ProductInMemoryService;
import com.examly.springapp.service.impl.ProductInMemoryServiceImpl;


@SpringBootApplication
public class SpringappApplication  {

    public static void displayDetails(Product product){
        System.out.println("Product ID: " + product.getProductId() + ", Name: " + product.getName() + ", Category: " + product.getCategory() + ", Price: " + product.getPrice()
        + ", Stock: " + product.getStock());
        
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringappApplication.class, args);


        // Product p1 = new Product(1,"Laptop", "Electronics", 50000,20,true);
        // Product p2 = new Product(1,"Mobile", "Electronics", 20000,5,false); 
        

        // try {
        //     if(p1.getStock() < 10)
        //         {
        //             throw new LowStockException("Stock is low for the product");
        //         }
        //         displayDetails(p1);
        //     if(p2.getStock() < 10)
        //         {
        //             throw new LowStockException("Stock is low for the product");
        //         }
        //         displayDetails(p2);
        // } catch (LowStockException e) {
        //     System.out.println(e.getMessage());
        // }

        





        Scanner sc = new Scanner(System.in);
        ProductInMemoryService service = new ProductInMemoryServiceImpl();

        while (true) {
            System.out.println("\n1.Add Product");
            System.out.println("2.Get Product by ID");
            System.out.println("3.Update by Category");
            System.out.println("4.Delete by Price");
            System.out.println("5.View by Category");
            System.out.println("6.Exit");
            System.out.println("Enter choice:");

            int ch = Integer.parseInt(sc.nextLine());

            try{

                switch (ch) {
                    case 1:
                         System.out.println("Enter details:");
                         Product p = new Product(
                            Integer.parseInt(sc.nextLine()),
                            sc.nextLine(),
                            sc.nextLine(),
                            Double.parseDouble(sc.nextLine()),
                            Integer.parseInt(sc.nextLine()),
                            Boolean.parseBoolean(sc.nextLine())
                         );
                         service.createProduct(p);
                         System.out.println("Product added");
                         break;
                    case 2:
                        System.out.println("Get product by Id, enter Id:");
                        Product product = service.getProductById(Integer.parseInt(sc.nextLine()));
                        System.out.println(product);
                        break;
                    case 3:
                        System.out.println("Update by category, enter category, newPrice, newStock");
                        List<Product> updated = service.updateProductByCategory(sc.nextLine(), Double.parseDouble(sc.nextLine()), Integer.parseInt(sc.nextLine()));
                        updated.forEach(System.out::println);
                        break;
                    case 4:
                         System.out.println("Enter Price Threshold");
                         List<Product> deleted = service.deleteProductByPrice(Double.parseDouble(sc.nextLine()));
                         deleted.forEach(System.out::println);
                         break;
                    case 5:
                        System.out.println("Enter category");
                        List<Product> products = service.viewProductDetailsByCategory(sc.nextLine());
                        products.forEach(System.out::println);

                        break;
                    case 6:
                         System.out.println("Exiting...");
                         break;
                    default:
                        System.out.println("Invalid Choice");

                        
                    
                }
        

            }catch(LowStockException e){
                System.out.println(e.getMessage());
            }

            
            
        }
        
        
        

    }
}
===================================================index.html======================================
<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="UTF-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Product Details Form</title>
  <link rel="stylesheet" href="style.css">
</head>

<body>
  <div class="container">

    <h1>PRODUCT DETAILS FORM</h1>

    <form id="productForm">

      <label for="">Product Name:</label>
      <input type="text" id="productName">

      <label for="">Category:</label>
      <input type="text" id="category">

      <label for="">Price (in $):</label>
      <input type="number" id="price">

      <label for="">Stock Quantity:</label>
      <input type="number" id="quantity">

      <label for=""> Select Discount (%):</label>
      <select id="discount">
        <option value="">Select Discount</option>
        <option value="10">10%</option>
        <option value="20">20%</option>
        <option value="30">30%</option>
      </select>

      <label for="">
        <input type="checkbox" id="verified"> Verified Product
      </label>
      <button id="submitButton" type="submit">Add Product</button>


    


    <table id="productTable">
      <thead>
        <tr id="productTableHead">
          <th>PRODUCT NAME</th>
          <th>CATEGORY</th>
          <th>PRICE </th>
          <th>QUANTITY</th>
          <th>DISCOUNT (%)</th>
          <th>VERIFIED</th>
        </tr>
      </thead>
      <tbody id="productList"></tbody>
    </table>

    <p id="ProductCount">Number of Product: 0</p>
  </form>

  </div>


  
  <script src="script.js"></script>
</body>

</html>
===============================================style.css===================================


*{
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

body {
    background-color: rgb(206, 217, 227);
    margin-bottom: 30px;
    font-family: 'DM Sans' sans-serif;
    padding: 30px 20px;
}

.container {
    max-width: 900px;
    padding: 30px;
    border-radius: 10px;
    background: fff;
    width: 450px;
    margin: 0 auto;
    box-shadow: 0 4px 20px rgb(0, 0, 1);
}

h1 {
    text-align: center;
    font-size: 28px;
    color: rgb(51, 51, 51);
    margin-bottom: 20px;
    text-transform: uppercase;
    letter-spacing: 1px;

}

label {
    font-weight: 700;
    margin-bottom: 5px;
    color: #333;
}

input, select {
    width: 100%;
    padding: 8px;
    margin-bottom: 15px;
}
#submitButton {
    width: 100%;
    padding: 10px;
    background-color: rgb(86, 160, 74);
    color: white;
    font-weight: bold;
    cursor: pointer;
}

#submitButton:hover {
    background-color: rgb(66, 140, 54);
}
table {
    width: 90%;
    margin-top: 15px;
    border-collapse: collapse;
}
 table, th, td {
    border: 1px solid black;
    padding: 8px;
    text-align: center;
    
 }

form {
      gap: 15px;
      display:flex;
      flex-direction: column;
     
 }


=================================================================script.js==========================================



const form = document.getElementById('productForm');
const productList = document.getElementById('productList');
const productCount = document.getElementById('ProductCount');

let count =0;
form.addEventListener('submit', function(e){
    e.preventDefault();

    const name = document.getElementById("productName").value.trim();
    const category = document.getElementById("category").value.trim();
    const price = parseFloat(document.getElementById("price").value);
    const quantity = parseInt(document.getElementById("quantity").value);
    const discount = document.getElementById("discount").value;
    const verified = document.getElementById("verified").checked ? 'True' : 'False';

    if (!name || !category || isNaN(quantity) ||!discount ||isNaN(price)) {
        alert('Please fill in all the fields.');
        return
        
    }
    const row =document.createElement('tr');
    row.innerHTML = `
    <td>${name}</td>
    <td>${category}</td>
    <td>${price.toFixed(2)}</td>
    <td>${quantity}</td>
    <td>${discount}</td>
    <td>${verified}</td>
    `;

    productList.appendChild(row);

    count++;
    productCount.textContent = `Number of Products: ${count}`

    

});






























