package com.example.rssitrilaterationapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class MyCanvas extends View {

    Paint paint;
    private double x;
    private double y;
    Bitmap bmp;
    Bitmap scaledbmp;

    public MyCanvas(Context context, AttributeSet attributes){
        super(context, attributes);
        this.x = 0.0;
        this.y = 0.0;

        //TODO: change to floorplan image
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.small);

        //scale image
        //TODO: change the width and height to fit image on screen
        scaledbmp = Bitmap.createScaledBitmap(bmp, 1080, 659, true);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        paint = new Paint();

        //draw floorplan
        canvas.drawBitmap(scaledbmp, 0, 0, paint);

        //draw red dot to indicate the position
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle((float)this.x, (float)this.y, 25, paint);
    }

    public void setPosition(double x, double y){
        this.x = Math.max(0, x);
        this.y = Math.max(0, y);
        this.invalidate();
    }
}
