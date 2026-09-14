package com.example.smartsaccoapp.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "market_items")
public class MarketItem {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String sellerEmail;
    public String title;
    public String description;
    public double price;
    public String status; // AVAILABLE, ESCROW, SOLD
    public String category; // e.g., Livestock, Produce, Tools

    public MarketItem(String sellerEmail, String title, String description, double price, String category) {
        this.sellerEmail = sellerEmail;
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.status = "AVAILABLE";
    }
    
    public MarketItem() {}
}