package com.koshy.graphcut;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

public class Util {

    public static Mat addPatternUsingFGMask(Mat mask, Mat pattern, Mat img) {
        Size sz = mask.size();
        Mat resizedPattern = new Mat();
        Imgproc.resize(pattern, resizedPattern, sz);
        Mat result = new Mat(sz, CvType.CV_8UC3, new Scalar(0, 0, 0));

        for (int row=0; row<mask.rows(); row++) {
            for (int col=0; col<mask.cols(); col++ ) {
                double[] pixel = mask.get(row, col);
//                System.out.println("Point : " + Arrays.toString(pixel));
                if(pixel[0] == 255) {
                    result.put(row, col, resizedPattern.get(row, col));
                } else {
                    result.put(row, col, img.get(row, col));
                }
            }
        }
        return result;
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
                System.out.println("Point : " + Arrays.toString(pixel));
            }
        }
    }
}
