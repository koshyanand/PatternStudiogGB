package com.koshy.graphcut;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static org.opencv.core.CvType.CV_32FC1;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_OPEN_IMAGE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int PERMISSION_REQUEST = 101;
    public static final int IMAGE_REQUEST_CODE = 111;
    public static final int PATTERN_REQUEST_CODE = 112;

    String mCurrentPhotoPath;
//    String mCurrentPhotoPath = "/storage/emulated/0/WhatsApp/Media/WallPaper/080fb730a49ee62dec5ee09a4a33ff3a.jpg";

    Bitmap mBitmap;
    String mPatternPath;
//    String mPatternPath = "/storage/emulated/0/Download/istockphoto-841479514-612x612.jpg";
    ImageView mImageView;
    int touchCount = 0;
    Point tl;
//    Point tl = new Point(225.8816680908203 , 1074.50830078125);
    Point br;
//    Point br = new Point(841.9195556640625 , 2060.575439453125);
    boolean targetChose = true;
    ProgressDialog dlg;
    Matrix mRotateMatrix;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.imgDisplay);
        dlg = new ProgressDialog(this);
        tl = new Point();
        br = new Point();
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
            mCurrentPhotoPath = images.get(0).getPath();
            Log.d(TAG, "onActivityResult: Photo : " + mCurrentPhotoPath);
            setPic();
        }

        if (requestCode == PATTERN_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
            Log.d(TAG, "onActivityResult: Got pattern");
            mPatternPath = images.get(0).getPath();
            Log.d(TAG, "onActivityResult: Pattern : " + mPatternPath);
        }

        if (requestCode == PERMISSION_REQUEST) {
            ImagePicker.with(this).start();
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_add_pattern:
                if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) & (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) & (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)){
                    // Permission is not granted
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, PERMISSION_REQUEST);
                } else {
                    ImagePicker.with(this).setRequestCode(PATTERN_REQUEST_CODE).start();
                }
                return true;

            case R.id.action_open_img:
                if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) & (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) & (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)){
                    // Permission is not granted
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, PERMISSION_REQUEST);
                } else {
                    ImagePicker.with(this).setRequestCode(IMAGE_REQUEST_CODE).start();
                }
                return true;
            case R.id.action_choose_target:
                if (mCurrentPhotoPath != null)
                    targetChose = false;
                mImageView.setOnTouchListener(touchListener);

                return true;
            case R.id.action_cut_image:
                if (mCurrentPhotoPath != null && targetChose) {
                    new ProcessImageTask().execute();
                    targetChose = false;
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.d(TAG, "onTouch: Down");
                if (touchCount == 0) {
                    float[] pointerCoords = Util.getPointerCoords(event, mImageView);
                    tl.x = pointerCoords[0];
                    tl.y = pointerCoords[1];
                    Log.d(TAG, "onTouch TL: " + tl.x + " , " + tl.y);
                    touchCount++;
                } else if (touchCount == 1) {
                    float[] pointerCoords = Util.getPointerCoords(event, mImageView);

                    br.x = pointerCoords[0];
                    br.y = pointerCoords[1];
                    Log.d(TAG, "onTouch BR: " + br.x + " , " + br.y);

                    Paint rectPaint = new Paint();
                    rectPaint.setARGB(255, 255, 0, 0);
                    rectPaint.setStyle(Paint.Style.STROKE);
                    rectPaint.setStrokeWidth(3);
                    Bitmap tmpBm = Bitmap.createBitmap(mBitmap.getWidth(),
                            mBitmap.getHeight(), Bitmap.Config.RGB_565);

                    Canvas tmpCanvas = new Canvas(tmpBm);

                    tmpCanvas.drawBitmap(mBitmap, 0, 0, null);
                    tmpCanvas.drawRect(new RectF((float) tl.x, (float) tl.y, (float) br.x, (float) br.y),
                            rectPaint);
                    BitmapDrawable drawable = new BitmapDrawable(getResources(), tmpBm);
                    mImageView.setImageDrawable(drawable);

                    targetChose = true;
                    touchCount = 0;
                    mImageView.setOnTouchListener(null);
                }
            }

            return true;
        }
    };

    private void setPic() {
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();
        Util.setImageSize(mCurrentPhotoPath, 500, 500);
//        Bitmap bitmap = Util.scaleImage(mCurrentPhotoPath, 70);

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        mBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        Bitmap n_bitmap = null;
        try {
            ExifInterface exif = new ExifInterface(mCurrentPhotoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }
            mRotateMatrix = matrix;
            n_bitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true); // rotating bitmap
        }
        catch (Exception e) {

        }
        mBitmap = n_bitmap;
        mImageView.setImageBitmap(n_bitmap);
    }

    private class ProcessImageTask extends AsyncTask<Integer, Integer, Integer> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dlg.setMessage("Processing Image...");
            dlg.setCancelable(false);
            dlg.setIndeterminate(true);
            dlg.show();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            Mat img = Imgcodecs.imread(mCurrentPhotoPath + ".png");

            Mat background = new Mat(img.size(), CvType.CV_8UC3,
                    new Scalar(255, 255, 255));
            Mat firstMask = new Mat();
            Mat bgModel = new Mat();
            Mat fgModel = new Mat();
            Mat mask;
            Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(Imgproc.GC_PR_FGD));
            Mat dst = new Mat();
            Rect rect = new Rect(tl, br);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
            String currentDateandTime = sdf.format(new Date());
            Log.d(TAG, "doInBackground: Starting GrabCut : " + currentDateandTime);
            Imgproc.grabCut(img, firstMask, rect, bgModel, fgModel,
                    5, Imgproc.GC_INIT_WITH_RECT);
            currentDateandTime = sdf.format(new Date());
            Log.d(TAG, "doInBackground: Finished GrabCut : " + currentDateandTime);
            Core.compare(firstMask, source, firstMask, Core.CMP_EQ);

            Mat foreground = new Mat(img.size(), CvType.CV_8UC3,
                    new Scalar(0, 0, 0));

//            int[][][] fg3D = Util.generate3DArray((int) img.size().height, (int) img.size().width, -1);
//            Util.printMat(firstMask);

            img.copyTo(foreground, firstMask);

            Mat pat = Imgcodecs.imread(mPatternPath);
            Mat pattern = new Mat();
            Imgproc.resize( pat, pattern, img.size());

            Mat img_grey = Util.convertToGreyScale(foreground);
            Mat img_grey_cont = Util.increaseContrast(img_grey);
            Log.d(TAG, "doInBackground1: " + img_grey_cont.type());

            Mat img_grey_cont_blur = Util.addGaussianBlur(img_grey_cont);
            Mat map = new Mat(img_grey_cont_blur.size(), CV_32FC1);

            img_grey_cont_blur.convertTo(map, CV_32FC1, 1.0f / 255.0f);
//            map = img_grey_cont_blur.clone();
            Log.d(TAG, "doInBackground2: " + map.type());

            currentDateandTime = sdf.format(new Date());
            Log.d(TAG, "doInBackground: Starting Displacement : " + currentDateandTime);

            Mat displacedPattern = Util.displaceImage1(pattern, map);
            currentDateandTime = sdf.format(new Date());
            Log.d(TAG, "doInBackground: Completed Displacement : " + currentDateandTime);

            Mat grey_color = Util.convertToRGB(img_grey);
            Mat result = Util.blend(displacedPattern, grey_color);
//
            Util.addPatternUsingFGMask(firstMask, result, img);
            firstMask.release();
            source.release();
            bgModel.release();
            fgModel.release();
            grey_color.release();
            img_grey_cont_blur.release();
            result.release();
            displacedPattern.release();
            map.release();
            img_grey.release();
            pat.release();
            pattern.release();
            foreground.release();
            Imgcodecs.imwrite(mCurrentPhotoPath, img);

            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            Bitmap jpg = BitmapFactory
                    .decodeFile(mCurrentPhotoPath);
            mBitmap = Bitmap.createBitmap(jpg, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), mRotateMatrix, true); // rotating bitmap

            mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mImageView.setAdjustViewBounds(true);
            mImageView.setPadding(2, 2, 2, 2);
            mImageView.setImageBitmap(mBitmap);
            mImageView.invalidate();

            dlg.dismiss();
        }
    }
}
