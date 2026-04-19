package com.example.androidpractice10_1;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnTouchListener {

    private SurfaceView mSurfaceView;
    private DrawingThread mDrawingThread;
    // 1. 声明按钮变量
    private Button mEraseButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 初始化 SurfaceView
        mSurfaceView = findViewById(R.id.surface_view);

        // 2. 初始化按钮
        mEraseButton = findViewById(R.id.button_erase);

        // 2. 获取 Holder 并注册回调
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(this);

        // 3. 设置触摸监听
        mSurfaceView.setOnTouchListener(this);

        // 4. 保持屏幕常亮 (可选，提升体验)
        mSurfaceView.setKeepScreenOn(true);

        mEraseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 调用线程的清除方法
                if (mDrawingThread != null) {
                    mDrawingThread.clearItems();
                }
            }
        });
    }

    /**
     * Surface 创建时调用
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 加载星星图片
        // 注意：这里假设你的图片名为 star.png 且在 drawable 文件夹下
        // 如果图片加载为 null，请检查资源名称
        android.graphics.Bitmap starBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.star);

        // 如果找不到图片，可以用代码画一个圆圈代替测试
        if (starBitmap == null) {
            // 这是一个兜底方案，实际开发中请确保 R.drawable.star 存在
            starBitmap = android.graphics.Bitmap.createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(starBitmap);
            android.graphics.Paint paint = new android.graphics.Paint();
            paint.setColor(android.graphics.Color.YELLOW);
            canvas.drawCircle(50, 50, 50, paint);
        }

        // 启动绘图线程
        mDrawingThread = new DrawingThread(holder, starBitmap);
        mDrawingThread.start(); // 启动 HandlerThread
    }

    /**
     * Surface 尺寸变化时调用
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 通知线程更新绘图区域的大小，用于边界检测
        if (mDrawingThread != null) {
            mDrawingThread.updateSize(width, height);
        }
    }

    /**
     * Surface 销毁时调用
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 必须停止线程，防止内存泄漏
        if (mDrawingThread != null) {
            mDrawingThread.quit();
            try {
                mDrawingThread.join(); // 等待线程彻底结束
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mDrawingThread = null;
        }
    }

    /**
     * 触摸事件处理
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // 获取触摸坐标，并通知线程添加星星
            // event.getX() 和 getY() 返回相对于 View 的坐标
            if (mDrawingThread != null) {
                // 减去图片宽高的一半，让星星中心对准手指（可选优化）
                mDrawingThread.addItem((int) event.getX(), (int) event.getY());
            }
            return true; // 表示事件已处理
        }
        return false;
    }
}