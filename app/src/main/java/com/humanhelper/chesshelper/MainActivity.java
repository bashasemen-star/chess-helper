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
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends Activity {
    private static final String TAG = "ChessHelper";
    private WindowManager windowManager;
    private ArrowView arrowView;
    private Process stockfishProcess;
    private OutputStreamWriter stockfishWriter;

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

        Button btnTest = new Button(this);
        btnTest.setText("ТЕСТ STOCKFISH");
        btnTest.setOnClickListener(v -> testStockfish());

        Button btnOn = new Button(this);
        btnOn.setText("ВКЛЮЧИТЬ СТРЕЛКУ");
        btnOn.setOnClickListener(v -> showOverlay());

        layout.addView(btnTest);
        layout.addView(btnOn);
        setContentView(layout);
        
        // Запускаем движок в фоне
        new Thread(this::startStockfish).start();
    }

    private void startStockfish() {
        try {
            // Копируем движок из assets во внутреннюю память (чтобы можно было запустить)
            File engineFile = new File(getFilesDir(), "stockfish");
            if (!engineFile.exists()) {
                InputStream is = getAssets().open("stockfish");
                FileOutputStream fos = new FileOutputStream(engineFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fos.close();
                is.close();
                engineFile.setExecutable(true);
            }

            stockfishProcess = Runtime.getRuntime().exec(engineFile.getAbsolutePath());
            stockfishWriter = new OutputStreamWriter(stockfishProcess.getOutputStream());
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(stockfishProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                Log.d(TAG, "Stockfish: " + line);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting stockfish", e);
        }
    }

    private void testStockfish() {
        if (stockfishWriter == null) {
            Toast.makeText(this, "Движок еще не готов", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            stockfishWriter.write("uci\n");
            stockfishWriter.write("position startpos\n");
            stockfishWriter.write("go depth 10\n");
            stockfishWriter.flush();
            Toast.makeText(this, "Запрос отправлен. Смотри логи!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showOverlay() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        arrowView = new ArrowView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        try {
            windowManager.addView(arrowView, params);
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
            canvas.drawLine(540, 1600, 540, 1300, paint);
            paint.setStyle(Paint.Style.FILL);
            Path path = new Path();
            path.moveTo(540, 1300);
            path.lineTo(500, 1360);
            path.lineTo(580, 1360);
            path.close();
            canvas.drawPath(path, paint);
        }
    }
}
