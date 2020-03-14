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
}
