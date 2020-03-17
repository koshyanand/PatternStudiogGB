package com.koshy.graphcut;

import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

import static org.opencv.imgproc.Imgproc.INTER_LINEAR;

public class Util {

    public static final String TAG = Util.class.getSimpleName();

    public static void addPatternUsingFGMask(Mat mask, Mat pattern, Mat img) {
        Size sz = mask.size();
        Mat resizedPattern = new Mat();
        Imgproc.resize(pattern, resizedPattern, sz);
        Mat locations = new Mat();
        Core.findNonZero(mask, locations);

        for (int i = 0; i < locations.rows(); i++) {
            double[] point = locations.get(i, 0);
            int row = (int) point[1];
            int col = (int) point[0];

            double[] pat = resizedPattern.get(row, col);
            img.put(row, col, pat);
        }
    }

    public static int[][][] generate3DArray(int row, int column, int value) {
        int[][][] array3D = new int[row][column][3];

        for (int r = 0; r < row; r++) {
            for (int c = 0; c < column; c++) {
                Arrays.fill(array3D[r][c], value);
            }
        }
        return array3D;
    }

    public static void printMat(Mat mat) {
        for (int row=0; row<mat.rows(); row++) {
            for (int col=0; col<mat.cols(); col++ ) {
                double[] pixel = mat.get(row, col);
                Log.d(TAG, "printMat Point : " + Arrays.toString(pixel));
            }
        }
    }

    public static float[] getPointerCoords(MotionEvent e, ImageView iv)
    {
        final int index = e.getActionIndex();
        final float[] coords = new float[] { e.getX(index), e.getY(index) };
        Matrix matrix = new Matrix();
        iv.getImageMatrix().invert(matrix);
        matrix.postTranslate(iv.getScrollX(), iv.getScrollY());
        matrix.mapPoints(coords);
        return coords;
    }

    public static Mat convertToGreyScale(Mat img) {
        Mat grey = new Mat();
        Imgproc.cvtColor(img, grey, Imgproc.COLOR_RGB2GRAY);
        return grey;
    }

    public static Mat convertToRGB(Mat img) {
        Mat grey_color = new Mat();
        Imgproc.cvtColor(img, grey_color, Imgproc.COLOR_GRAY2BGR);
        return grey_color;
    }

    public static Mat increaseContrast(Mat grey) {
        Mat grey_cont = new Mat();
        double alpha = 1.5;
        double beta = 0;
//        Imgproc.adaptiveThreshold(grey, grey_cont, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15,10);
        Core.convertScaleAbs(grey, grey_cont, alpha, beta);
        return grey_cont;
    }

    public static Mat addGaussianBlur(Mat grey_cont) {
        Mat grey_cont_blur =  new Mat();
        Imgproc.GaussianBlur(grey_cont, grey_cont_blur, new Size(5, 5), 0);
        return grey_cont_blur;
    }

    public static Mat displaceImage(Mat img, Mat map) {
        Mat result = new Mat();
        Imgproc.remap(img, result, map, map, INTER_LINEAR);
        return result;
    }

    public static Mat displaceImage1(Mat img, Mat map) {
        Mat result = new Mat(img.size(), img.type());
        for (int y=0; y<img.rows(); y++) {
            for (int x=0; x<img.cols(); x++ ) {
                double[] pixel = map.get(y, x);
//                Log.d(TAG, "displaceImage1: Length : " + pixel.length);
//                Log.d(TAG, "displaceImage1: " + pixel[0]);
                int dx = x + ((int)pixel[0] * 256 - 128) * 25 / 256;
                int dy = y + ((int)pixel[0] * 256 - 128) * 25 / 256;
                if (dx < 0)
                    dx = 0;
                if (dx >= img.cols())
                    dx = img.cols() - 1;
                if (dy < 0)
                    dy = 0;
                if (dy >= img.rows())
                    dy = img.rows() - 1;

                result.put(y, x, img.get(dy, dx));
            }
        }
        return result;
    }

    public static Mat blend(Mat img1, Mat img2) {
        Mat result = new Mat();
        double alpha = 0.5;
        double beta = 1.0 - alpha;
        Core.addWeighted(img1, alpha, img2, beta, 0.0, result);
        return result;
    }
}
