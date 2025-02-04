package com.project.tohfa;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Log the product data for debugging
        Log.d("ProductAdapter", "Product Data: " + product.toString());

        holder.productName.setText(product.getName());
        holder.productPrice.setText(product.getPrice());
        holder.productDescription.setText(product.getDescription());

        // Display multiple categories
        List<String> categories = product.getCategories();
        if (categories != null && !categories.isEmpty()) {
            holder.productCategory.setText(String.join(", ", categories));  // Display all selected categories
        } else {
            holder.productCategory.setText("No category selected");
        }

        // Load image with a check for null or empty URL
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Log.d("ProductAdapter", "Loading image from URL: " + product.getImageUrl());
            Picasso.get().load(product.getImageUrl()).into(holder.productImage);
        } else {
            Log.d("ProductAdapter", "Loading placeholder image");
            Picasso.get().load(R.drawable.placeholder_image).into(holder.productImage);
        }

        // Remove product functionality
        holder.removeButton.setOnClickListener(v -> removeProduct(position, product.getId()));
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    private void removeProduct(int position, String productId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("products")
                .whereEqualTo("id", productId)  // Assuming "id" is the unique identifier field
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        documentSnapshot.getReference().delete();  // Delete from Firestore
                    }
                    productList.remove(position);  // Remove from local list
                    notifyItemRemoved(position);  // Notify RecyclerView
                })
                .addOnFailureListener(e -> {
                    Log.e("ProductAdapter", "Failed to remove product: " + e.getMessage());
                    Toast.makeText(context, "Failed to remove product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        TextView productCategory;
        TextView productDescription;
        Button removeButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productCategory = itemView.findViewById(R.id.productCategory);
            productDescription = itemView.findViewById(R.id.productDescription);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}