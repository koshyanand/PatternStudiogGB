package com.koshy.graphcut;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_OPEN_IMAGE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int PERMISSION_REQUEST = 101;
    public static final int IMAGE_REQUEST_CODE = 111;
    public static final int PATTERN_REQUEST_CODE = 112;

    String mCurrentPhotoPath;
    Bitmap mBitmap;
    String mPatternPath;
    ImageView mImageView;
    int touchCount = 0;
    Point tl;
    Point br;
    boolean targetChose = false;
    ProgressDialog dlg;

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
            Log.d(MainActivity.class.getSimpleName(), "onCreate: OPENCV Error");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void setPic() {
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

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
        mImageView.setImageBitmap(mBitmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
            mCurrentPhotoPath = images.get(0).getPath();
            Log.d(TAG, "onActivityResult: " + mCurrentPhotoPath);
            setPic();
        }

        if (requestCode == PATTERN_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
            Log.d(TAG, "onActivityResult: Got pattern");
//            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//            mImageView.setImageBitmap(mPatternBitmap);
            mPatternPath = images.get(0).getPath();
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
                    mImageView.setOnTouchListener(new View.OnTouchListener() {

                        @SuppressLint("ClickableViewAccessibility")
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                Log.d(TAG, "onTouch: Down");
                                if (touchCount == 0) {
                                    tl.x = event.getX();
                                    tl.y = event.getY();
                                    touchCount++;
                                }
                                else if (touchCount == 1) {
                                    br.x = event.getX();
                                    br.y = event.getY();

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
                                    mImageView.setImageDrawable(new BitmapDrawable(getResources(), tmpBm));

                                    targetChose = true;
                                    touchCount = 0;
                                    mImageView.setOnTouchListener(null);
                                }
                            }

                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                Log.d(TAG, "onTouch: UP");

                            }
                                return true;
                        }
                    });

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
            Mat img = Imgcodecs.imread(mCurrentPhotoPath);

            Mat background = new Mat(img.size(), CvType.CV_8UC3,
                    new Scalar(255, 255, 255));
            Mat firstMask = new Mat();
            Mat bgModel = new Mat();
            Mat fgModel = new Mat();
            Mat mask;
            Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(Imgproc.GC_PR_FGD));
            Mat dst = new Mat();
            Rect rect = new Rect(tl, br);

            Imgproc.grabCut(img, firstMask, rect, bgModel, fgModel,
                    5, Imgproc.GC_INIT_WITH_RECT);
            Core.compare(firstMask, source, firstMask, Core.CMP_EQ);

//            Mat foreground = new Mat(img.size(), CvType.CV_8UC3,
//                    new Scalar(0, 0, 0));

//            int[][][] fg3D = Util.generate3DArray((int) img.size().height, (int) img.size().width, -1);
//            Util.printMat(firstMask);

//            img.copyTo(foreground, firstMask);

            Mat pattern = Imgcodecs.imread(mPatternPath);

            Mat result = Util.addPatternUsingFGMask(firstMask, pattern, img);
            firstMask.release();
            source.release();
            bgModel.release();
            fgModel.release();
            Imgcodecs.imwrite(mCurrentPhotoPath + ".png", result);
//
//            Scalar color = new Scalar(255, 0, 0, 255);
//            Imgproc.rectangle(img, tl, br, color);
//
//            Mat tmp = new Mat();
//            System.out.println("Temp : " + String.valueOf(tmp.));
//
//            Imgproc.resize(background, tmp, img.size());
//            background = tmp;
//            mask = new Mat(foreground.size(), CvType.CV_8UC1,
//                    new Scalar(255, 255, 255));
//
//            Imgproc.cvtColor(foreground, mask, Imgproc.COLOR_BGR2GRAY);
//            Imgproc.threshold(mask, mask, 254, 255, Imgproc.THRESH_BINARY_INV);
//            System.out.println();
//            Mat vals = new Mat(1, 1, CvType.CV_8UC3, new Scalar(0.0));
//            background.copyTo(dst);
//
//            background.setTo(vals, mask);
//
//            Core.add(background, foreground, dst, mask);
//
//            firstMask.release();
//            source.release();
//            bgModel.release();
//            fgModel.release();
//            vals.release();
//
//            Imgcodecs.imwrite(mCurrentPhotoPath + ".png", dst);

            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            Bitmap jpg = BitmapFactory
                    .decodeFile(mCurrentPhotoPath + ".png");

            mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mImageView.setAdjustViewBounds(true);
            mImageView.setPadding(2, 2, 2, 2);
            mImageView.setImageBitmap(jpg);
            mImageView.invalidate();

            dlg.dismiss();
        }
    }
}
