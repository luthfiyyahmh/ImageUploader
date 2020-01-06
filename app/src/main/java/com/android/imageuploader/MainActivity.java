package com.android.imageuploader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.theartofdev.edmodo.cropper.CropImage;

public class MainActivity extends AppCompatActivity {

    ImageView imgMain;
    Button bClick, bUpload;
    Bitmap photo;
    private File file;

    OutputStream outputStream;

    //Declare an Image URI, which will be in use later
    Uri mImageUri;
    Bitmap mImageBitmap;

    //private static final String URL = "http://10.0.2.2:5000/upload";
    private static final String URL = "http://192.168.0.195:5000/upload";
    private static final int CAMERA_REQUEST = 1888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgMain = findViewById(R.id.imgMain);
        bClick = findViewById(R.id.bClick);
        bUpload = findViewById(R.id.bUpload);

        bClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                CropImage.activity().start(MainActivity.this);
            }
        });

        bUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String s = response.trim();
                        if (!s.equalsIgnoreCase("Loi")) {
                            Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        String image = getStringImage(photo);
                        Map<String, String> params = new HashMap<String, String>();
                        Random random = new Random();
                        int randomNumber = random.nextInt(999999 - 100000) + 100000;
                        String fileName = "Image" + String.valueOf(randomNumber)+".png";
                        params.put("FileName",fileName);
                        params.put("IMG", image);
                        return params;
                    }
                };
                {
                    int socketTimeout = 30000;
                    RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    stringRequest.setRetryPolicy(policy);
                    RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                    requestQueue.add(stringRequest);
                }
            }
        });
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CAMERA_REQUEST) {
//            photo = (Bitmap) data.getExtras().get("data");
//            imgMain.setImageBitmap(photo);
//        }
//
//    }
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri();

                // Convert URI ke Bitmap, karna harus bentuk Bitmap kalau mau disimpan ke Storage
                try {
                    //Getting the bitmap from gallery
                    mImageBitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), mImageUri);
                    imgMain.setImageURI(mImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception e = result.getError();
                Toast.makeText(this, "Possible error is : " + e, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getStringImage(Bitmap bm) {
        bm = ((BitmapDrawable) imgMain.getDrawable()).getBitmap();
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, ba);
        byte[] imagebyte = ba.toByteArray();
        String encode = Base64.encodeToString(imagebyte, Base64.DEFAULT);
        return encode;
    }
}
