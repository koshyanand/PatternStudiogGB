package com.koshy.graphcut;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.OutputStream;

public class DrawOnBitmapActivity extends AppCompatActivity implements View.OnClickListener
{

    DrawableImageView choosenImageView;
    Button choosePicture;
    Button savePicture;

    Bitmap bmp;
    Bitmap alteredBitmap;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_on_bitmap);

        choosenImageView = findViewById(R.id.ChoosenImageView);
        choosePicture = findViewById(R.id.ChoosePictureButton);
        savePicture = findViewById(R.id.SavePictureButton);

        savePicture.setOnClickListener(this);
        choosePicture.setOnClickListener(this);
    }

    public void onClick(View v)
    {
        if (v == choosePicture)
        {
            Intent choosePictureIntent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(choosePictureIntent, 0);
        }
        else if (v == savePicture)
        {
            if (alteredBitmap != null)
            {
                ContentValues contentValues = new ContentValues(3);
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "Draw On Me");

                Uri imageFileUri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                try {
                    OutputStream imageFileOS = getContentResolver()
                            .openOutputStream(imageFileUri);
                    alteredBitmap
                            .compress(Bitmap.CompressFormat.JPEG, 90, imageFileOS);
                    Toast t = Toast
                            .makeText(this, "Saved!", Toast.LENGTH_SHORT);
                    t.show();

                } catch (Exception e) {
                    Log.v("EXCEPTION", e.getMessage());
                }
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            Uri imageFileUri = intent.getData();
            try {
                BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                bmpFactoryOptions.inJustDecodeBounds = true;
                bmp = BitmapFactory
                        .decodeStream(
                                getContentResolver().openInputStream(
                                        imageFileUri), null, bmpFactoryOptions);

                bmpFactoryOptions.inJustDecodeBounds = false;
                bmp = BitmapFactory
                        .decodeStream(
                                getContentResolver().openInputStream(
                                        imageFileUri), null, bmpFactoryOptions);

                alteredBitmap = Bitmap.createBitmap(bmp.getWidth(),
                        bmp.getHeight(), bmp.getConfig());

                choosenImageView.setNewImage(alteredBitmap, bmp);
            }
            catch (Exception e) {
                Log.v("ERROR", e.toString());
            }
        }
    }
}
