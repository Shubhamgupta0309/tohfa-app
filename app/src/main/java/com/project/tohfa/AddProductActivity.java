package com.project.tohfa;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddProductActivity extends AppCompatActivity {

    private EditText productNameEditText, productPriceEditText, productDescriptionEditText;
    private LinearLayout imageContainer;
    private Button chooseImageButton, uploadButton;

    private List<Uri> imageUris = new ArrayList<>();
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    private static final int PICK_IMAGES_REQUEST = 1;

    // Checkboxes for categories
    private CheckBox categorySameDayDelivery, categoryNewArrivals, categoryPersonalized,
            categoryFlowersPlants, categoryCakes, categoryCombos, categoryChocolates,
            categoryFestivalOffer, categoryBulkCorporateGifts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Initialize Views
        productNameEditText = findViewById(R.id.productNameEditText);
        productPriceEditText = findViewById(R.id.productPriceEditText);
        productDescriptionEditText = findViewById(R.id.productDescriptionEditText);
        imageContainer = findViewById(R.id.imageContainer);
        chooseImageButton = findViewById(R.id.chooseImageButton);
        uploadButton = findViewById(R.id.uploadButton);

        // Initialize Checkboxes
        categorySameDayDelivery = findViewById(R.id.categorySameDayDelivery);
        categoryNewArrivals = findViewById(R.id.categoryNewArrivals);
        categoryPersonalized = findViewById(R.id.categoryPersonalized);
        categoryFlowersPlants = findViewById(R.id.categoryFlowersPlants);
        categoryCakes = findViewById(R.id.categoryCakes);
        categoryCombos = findViewById(R.id.categoryCombos);
        categoryChocolates = findViewById(R.id.categoryChocolates);
        categoryFestivalOffer = findViewById(R.id.categoryFestivalOffer);
        categoryBulkCorporateGifts = findViewById(R.id.categoryBulkCorporateGifts);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");

        chooseImageButton.setOnClickListener(v -> chooseImages());
        uploadButton.setOnClickListener(v -> uploadProduct());
    }

    private void chooseImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK) {
            imageContainer.removeAllViews();
            imageUris.clear();

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imageUris.add(imageUri);
                    addImageToContainer(imageUri);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                imageUris.add(imageUri);
                addImageToContainer(imageUri);
            }
        }
    }

    private void addImageToContainer(Uri imageUri) {
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 300);
        params.setMargins(0, 0, 10, 0);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Picasso.get().load(imageUri).into(imageView);
        imageContainer.addView(imageView);
    }

    private void uploadProduct() {
        String name = productNameEditText.getText().toString().trim();
        String price = productPriceEditText.getText().toString().trim();
        String description = productDescriptionEditText.getText().toString().trim();
        List<String> selectedCategories = getSelectedCategories();

        if (name.isEmpty() || price.isEmpty() || description.isEmpty() || imageUris.isEmpty() || selectedCategories.isEmpty()) {
            Toast.makeText(this, "All fields, images, and at least one category are required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        uploadImages(name, price, description, selectedCategories);
    }

    private void uploadImages(String name, String price, String description, List<String> categories) {
        List<String> imageUrls = new ArrayList<>();
        final int[] uploadedCount = {0};

        for (Uri imageUri : imageUris) {
            String imageName = UUID.randomUUID().toString();
            StorageReference imageRef = storageReference.child("product_images/" + imageName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        imageUrls.add(uri.toString());
                        uploadedCount[0]++;

                        if (uploadedCount[0] == imageUris.size()) {
                            saveProductToFirestore(name, price, description, categories, imageUrls);
                        }
                    }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(AddProductActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveProductToFirestore(String name, String price, String description, List<String> categories, List<String> imageUrls) {
        String id = firestore.collection("products").document().getId();

        Product product = new Product(id, name, price, description, imageUrls, categories);

        firestore.collection("products")
                .document(id)
                .set(product)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddProductActivity.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddProductActivity.this, "Error adding product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private List<String> getSelectedCategories() {
        List<String> selectedCategories = new ArrayList<>();
        if (categorySameDayDelivery.isChecked()) selectedCategories.add("Same Day Delivery");
        if (categoryNewArrivals.isChecked()) selectedCategories.add("New Arrivals");
        if (categoryPersonalized.isChecked()) selectedCategories.add("Personalized");
        if (categoryFlowersPlants.isChecked()) selectedCategories.add("Flowers/Plants");
        if (categoryCakes.isChecked()) selectedCategories.add("Cakes");
        if (categoryCombos.isChecked()) selectedCategories.add("Combos");
        if (categoryChocolates.isChecked()) selectedCategories.add("Chocolates");
        if (categoryFestivalOffer.isChecked()) selectedCategories.add("Festival Offer");
        if (categoryBulkCorporateGifts.isChecked()) selectedCategories.add("Bulk/Corporate Gifts");

        return selectedCategories;
    }
}