package com.humanhelper.chesshelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WindowManager windowManager;
    private ArrowView arrowView;
    // Координаты начала и конца стрелки (внутри окна 1080x1080)
    private int startX = 540;
    private int startY = 800; 
    private int endX = 540;
    private int endY = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            finish();
            return;
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);

        Button btnUp = new Button(this);
        btnUp.setText("ВВЕРХ");
        btnUp.setOnClickListener(v -> { endY -= 50; updateArrow(); });

        Button btnDown = new Button(this);
        btnDown.setText("ВНИЗ");
        btnDown.setOnClickListener(v -> { endY += 50; updateArrow(); });

        Button btnLeft = new Button(this);
        btnLeft.setText("ВЛЕВО");
        btnLeft.setOnClickListener(v -> { endX -= 50; updateArrow(); });

        Button btnRight = new Button(this);
        btnRight.setText("ВПРАВО");
        btnRight.setOnClickListener(v -> { endX += 50; updateArrow(); });

        Button btnOn = new Button(this);
        btnOn.setText("ВКЛЮЧИТЬ СТРЕЛКУ");
        btnOn.setOnClickListener(v -> showOverlay());

        layout.addView(btnOn);
        layout.addView(btnUp);
        layout.addView(btnDown);
        layout.addView(btnLeft);
        layout.addView(btnRight);

        setContentView(layout);
    }

    private void showOverlay() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        arrowView = new ArrowView(this);
        
        // Окно размером с доску (1080x1080)
        int size = 1080;
        
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                size,
                size,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | 
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 400; // Сдвиг вниз, чтобы окно было над доской (подгони под свой экран)
        
        try {
            windowManager.addView(arrowView, params);
            Toast.makeText(this, "Стрелка включена", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateArrow() {
        if (arrowView != null) {
            arrowView.invalidate();
        }
    }

    public class ArrowView extends View {
        private Paint paint;
        public ArrowView(Context context) {
            super(context);
            paint = new Paint();
            paint.setColor(Color.parseColor("#5C9CE6"));
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setStrokeWidth(25f);
            paint.setAntiAlias(true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            // Рисуем стрелку только внутри окна
            canvas.drawLine(startX, startY, endX, endY, paint);
            paint.setStyle(Paint.Style.FILL);
            Path path = new Path();
            path.moveTo(endX, endY);
            path.lineTo(endX - 40, endY + 60);
            path.lineTo(endX + 40, endY + 60);
            path.close();
            canvas.drawPath(path, paint);
        }
    }
}
