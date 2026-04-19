package com.example.androidpractice10_1;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.SurfaceHolder;

import java.util.ArrayList;

/**
 *  HandlerThread 是一个方便的框架辅助类，用来生成后台工作线程以处理收到的消息
 */
public class DrawingThread extends HandlerThread implements  Handler.Callback {
    //三个消息常量，分别用于添加新的绘图项、移动绘图项、清除所有绘图项
    private static final int MSG_ADD = 100;
    private static final int MSG_MOVE = 101;
    private static final int MSG_CLEAR = 102;

    private int mDrawingWidth, mDrawingHeight; //存储绘图区域的宽度和高度

    private SurfaceHolder mSurfaceHolder; //持有 SurfaceHolder 对象，用于访问 SurfaceView 进行绘图
    private Paint mPaint; //绘制图形的画笔
    private Handler mReceiver;  //用于接收和处理消息的 Handler 对象
    private Bitmap mIcon; //要绘制的 Bitmap 图标 （传 R.drawable.star ）
    private ArrayList<DrawingItem> mLocations;  //用于存储所有绘图项的位置和移动方向信息
    private boolean mRunning;   //用于指示线程是否正在运行--图像是否开始渲染

    private class DrawingItem {  //内部类-用于表示每个绘图项的位置 (x, y) 和移动方向 (horizontal, vertical)。
        // 当前位置的标识
        int x, y;
        // 移动方向的标识
        boolean horizontal, vertical;

        public DrawingItem(int x, int y, boolean horizontal,
                           boolean vertical) {
            this.x = x;
            this.y = y;
            this.horizontal = horizontal;
            this.vertical = vertical;
        }
    }

    public DrawingThread(SurfaceHolder holder, Bitmap icon) {  //接收一个 SurfaceHolder 和一个 Bitmap 作为参数
        super("DrawingThread");
        mSurfaceHolder = holder;
        mLocations = new ArrayList<>();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);   //Paint.ANTI_ALIAS_FLAG：Paint 类的一个标志位，用于设置抗锯齿功能
        mIcon = icon;  //绘制的图标（星星）
    }

    //HandlerThread 的 Looper 准备好时，该方法被调用。它创建了一个 Handler 并将其与当前线程的 Looper 关联，设置 mRunning 为 true，并发送一个 MSG_MOVE 消息以启动绘图循环
    @Override
    protected void onLooperPrepared() {
        mReceiver = new Handler(getLooper(), this); //Looper类用来为一个线程执行消息循环，使得线程能够按照顺序处理一系列的消息。
        // 开始渲染
        mRunning = true;
        mReceiver.sendEmptyMessage(MSG_MOVE);
    }

    @Override
    public boolean quit() {  //退出线程
        // 清除所有的消息
        mRunning = false;
        mReceiver.removeCallbacksAndMessages(null);

        return super.quit();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_ADD:
                // 在触摸的位置创建一个新的条目，该条目的开始方向是随机的
                DrawingItem newItem = new DrawingItem(msg.arg1,msg.arg2, Math.round(Math.random()) == 0,
                        Math.round(Math.random()) == 0);
                mLocations.add(newItem);
                break;
            case MSG_CLEAR:
                // 删除所有对象
                mLocations.clear();
                break;
            case MSG_MOVE:
                if (!mRunning)
                    return true;
                // 锁定 SurfaceView，并返回到要绘图的 Canvas
                Canvas canvas = mSurfaceHolder.lockCanvas();
                if (canvas == null) {
                    break;
                }
                // 首先清空 Canvas
                // 如果没有这句代码，当图标移动时会在图标之前的位置出现拖尾痕迹
                canvas.drawColor(Color.BLACK);
                // 绘制每个条目
                for (DrawingItem item : mLocations) {
                    // 更新位置
                    item.x += (item.horizontal ? 5 : -5);
                    if (item.x >= (mDrawingWidth - mIcon.getWidth())) {
                        item.horizontal = false;
                    }
                    if (item.x <= 0) {
                        item.horizontal = true;
                    }
                    item.y += (item.vertical ? 5 : -5);
                    if (item.y >= (mDrawingHeight - mIcon.getHeight()))
                    {
                        item.vertical = false;
                    }
                    if (item.y <= 0) {
                        item.vertical = true;
                    }
                    canvas.drawBitmap(mIcon, item.x, item.y, mPaint);
                }
                // 解锁 Canvas，并渲染当前的图像
                mSurfaceHolder.unlockCanvasAndPost(canvas);
                break;
        }
        // 如果线程仍在运行，再次发送 MSG_MOVE 消息以继续循环
        if (mRunning) {
            mReceiver.sendEmptyMessage(MSG_MOVE);
        }
        return true;
    }

    public void updateSize(int width, int height){
        mDrawingWidth = width;
        mDrawingHeight = height;
    }

    public void addItem(int x, int y) {
        // 通过 Message 参数将位置传给处理程序
        Message msg = Message.obtain(mReceiver, MSG_ADD, x, y);
        mReceiver.sendMessage(msg);
    }

    public void clearItems(){
        mReceiver.sendEmptyMessage(MSG_CLEAR);
    }
}
