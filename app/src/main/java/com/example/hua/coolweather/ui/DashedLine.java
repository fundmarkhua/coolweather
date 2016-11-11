package com.example.hua.coolweather.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Administrator on 2016/11/9.
 * 画虚线
 */
class DashedLine extends View {
    Paint paint;
    Path path;
    PathEffect effects;
    public DashedLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        path = new Path();
        effects = new DashPathEffect(new float[]{4, 4, 4, 4}, 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getResources().getColor(android.R.color.black));
        path.moveTo(0, 0);
        path.lineTo(0, 900);
        paint.setPathEffect(effects);
        canvas.drawPath(path, paint);
    }
}