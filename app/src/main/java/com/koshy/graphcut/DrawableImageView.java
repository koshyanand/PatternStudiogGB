package com.koshy.graphcut;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawableImageView extends androidx.appcompat.widget.AppCompatImageView implements View.OnTouchListener {
    float downx = 0;
    float downy = 0;
    float upx = 0;
    float upy = 0;

    Canvas canvas;
    Paint paint;
    Matrix matrix;

    public DrawableImageView(Context context)
    {
        super(context);
        setOnTouchListener(this);
    }

    public DrawableImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setOnTouchListener(this);
    }

    public DrawableImageView(Context context, AttributeSet attrs,
                             int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        setOnTouchListener(this);
    }

    public void setNewImage(Bitmap alteredBitmap, Bitmap bmp)
    {
        canvas = new Canvas(alteredBitmap );
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(5);
        matrix = new Matrix();
        canvas.drawBitmap(bmp, matrix, paint);

        setImageBitmap(alteredBitmap);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        int action = event.getAction();

        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                downx = Util.getPointerCoords(event, this)[0];//event.getX();
                downy = Util.getPointerCoords(event, this)[1];//event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                upx = Util.getPointerCoords(event, this)[0];//event.getX();
                upy = Util.getPointerCoords(event, this)[1];//event.getY();
                canvas.drawLine(downx, downy, upx, upy, paint);
                invalidate();
                downx = upx;
                downy = upy;
                break;
            case MotionEvent.ACTION_UP:
                upx = Util.getPointerCoords(event, this)[0];//event.getX();
                upy = Util.getPointerCoords(event, this)[1];//event.getY();
                canvas.drawLine(downx, downy, upx, upy, paint);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return true;
    }

}