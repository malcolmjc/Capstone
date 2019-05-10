package com.example.varuns.capstone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.varuns.capstone.model.Artisan;
import com.example.varuns.capstone.model.ArtisanItem;
import com.example.varuns.capstone.services.ApiService;
import com.example.varuns.capstone.services.RestfulResponse;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProduct extends AppCompatActivity {

    Artisan artisan;
    TextInputEditText descInput;
    TextInputEditText nameInput;
    List<ArtisanItem> items;
    ArtisanItem item;
    Integer artisanId;
    Integer itemId;
    ImageButton imgButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);
        setTitle("Edit Product");

        artisanId = getIntent().getExtras().getInt("artisanId");
        itemId = getIntent().getExtras().getInt("itemId");

        nameInput = (TextInputEditText) findViewById(R.id.namebox);
        descInput = (TextInputEditText) findViewById(R.id.descbox);

        getArtisanById(artisanId);

        imgButton = (ImageButton) findViewById(R.id.imageButton2);

        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 1);
            }
        });

        setupBottomNavigationView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            if (intent != null) {
                final Uri uri = intent.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    imgButton.setImageBitmap(bitmap);
                } catch(IOException e) {
                    System.out.println("Error, cannot find image file");
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void setupBottomNavigationView() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        Intent intent = new Intent(EditProduct.this, menu_activity.class);
                        startActivity(intent);
                        break;
                    case R.id.navigation_dashboard:
                        Intent intent1 = new Intent(EditProduct.this, Send_message.class);
                        startActivity(intent1);
                        break;
                    case R.id.navigation_notifications:
                        Intent intent2 = new Intent(EditProduct.this, ReportsActivity.class);
                        startActivity(intent2);
                        break;

                }
                return true;
            }
        });
    }

    //this function takes the text inputted into the name and desc boxes and updates an ArtisanItem with it
    public void saveItem(View v){
        if(!verifyFields()){
            //if user tries to edit item so there is no name then an error is set
            return;
        }
        item.setItemName(nameInput.getText().toString());
        item.setItemDescription(descInput.getText().toString());

        //this call will update and save the contents of the artisan that is passed to it
        Call<RestfulResponse<Artisan>> call1 = ApiService.artisanService().saveArtisan(artisan);
        call1.enqueue(new Callback<RestfulResponse<Artisan>>() {
            @Override
            public void onResponse(Call<RestfulResponse<Artisan>> call, Response<RestfulResponse<Artisan>> response) {
                //report the result of the call
                Toast.makeText(EditProduct.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();

            }

            //if the call results in a failure
            @Override
            public void onFailure(Call<RestfulResponse<Artisan>> call, Throwable t) {
                Toast.makeText(EditProduct.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        finish();

    }

    //ends this activity and changes nothing if the cancel button is pressed
    public void cancelEdit(View v){
        finish();
    }

    //searches for an artisan by the artisanId number, artisan is saved to Artisan object
    private void getArtisanById(final Integer artisanId) {

        Call<RestfulResponse<Artisan>> call = ApiService.artisanService().getArtisanById(artisanId.toString());
        //handle the response
        call.enqueue(new Callback<RestfulResponse<Artisan>>() {
            @Override
            public void onResponse(Call<RestfulResponse<Artisan>> call, Response<RestfulResponse<Artisan>> response) {
                //gets artisan, finds appropriate item, sets text fields to item name and description
                artisan = response.body().getData();

                items = artisan.getArtisanItems();
                item = items.get(itemId);

                nameInput.setText(item.getItemName());
                descInput.setText(item.getItemDescription());
            }

            //response if call results in a failure
            @Override
            public void onFailure(Call<RestfulResponse<Artisan>> call, Throwable t) {
                Toast.makeText(EditProduct.this, "failure", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //verifies that user inputted correct information
    private boolean verifyFields() {
        nameInput.setError(null);
        if (nameInput.getText().length() == 0) {
            nameInput.setError("Item name is required!");
            return false;
        }

        return true;
    }
}
