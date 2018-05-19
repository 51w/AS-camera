package com.rustfisher.camera.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 提示框
 * Created by Rust on 2017/12/19.
 */
public class ColorHintView extends View {
    public static final String TAG = "ESAppColorHint";
    private List<Rect> faceRectList;
    private boolean showRect = false;
    private Paint paint;
    private int colorSelected = Color.RED;
    private int colorNormal = Color.GREEN;

    public ColorHintView(Context context) {
        this(context, null);
    }

    public ColorHintView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorHintView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        faceRectList = new ArrayList<>(16);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
    }

    public void setColorNormal(int colorNormal) {
        this.colorNormal = colorNormal;
    }

    public void setColorSelected(int colorSelected) {
        this.colorSelected = colorSelected;
    }

    /**
     * 在这里进行缩放计算
     *
     * @param rectList        要绘制的矩形列表
     * @param rectWholeWid    矩形所在区域的宽
     * @param rectWholeHeight 矩形所在区域的高
     */
    public void setFaceRect(List<Rect> rectList, int rectWholeWid, int rectWholeHeight) {
        double widRatio = getWidth() / (rectWholeWid * 1.0);
        double heightRatio = getHeight() / (rectWholeHeight * 1.0);
        this.faceRectList.clear();
        for (Rect rect : rectList) {
            this.faceRectList.add(new Rect((int) (rect.left * widRatio), (int) (rect.top * heightRatio),
                    (int) (rect.right * widRatio), (int) (rect.bottom * heightRatio)));
        }
        setShowRect(true);
    }

    public void showRect(Rect rect, int rectWholeWid, int rectWholeHeight) {
        double widRatio = getWidth() / (rectWholeWid * 1.0);
        double heightRatio = getHeight() / (rectWholeHeight * 1.0);
        this.faceRectList.clear();
        this.faceRectList.add(new Rect((int) (rect.left * widRatio), (int) (rect.top * heightRatio),
                (int) (rect.right * widRatio), (int) (rect.bottom * heightRatio)));
        setShowRect(true);
    }

    public void setShowRect(boolean showRect) {
        this.showRect = showRect;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showRect) {
            for (int i = 0; i < faceRectList.size(); i++) {
                Rect rect = faceRectList.get(i);
                paint.setColor(i == 0 ? colorSelected : colorNormal);
                canvas.drawRect(rect, paint);
            }
        }
    }
}
