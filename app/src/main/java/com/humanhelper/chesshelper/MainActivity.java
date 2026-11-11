package com.humanhelper.chesshelper;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity {
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
        startService(new Intent(this, OverlayService.class));
        finish();
    }

    public static class OverlayService extends Service {
        private WindowManager windowManager;
        private ArrowView arrowView;

        @Override
        public void onCreate() {
            super.onCreate();
            
            String channelId = "chess_helper_channel";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, "Chess Helper", NotificationManager.IMPORTANCE_LOW);
                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }
            
            Notification notification = new Notification.Builder(this, channelId)
                    .setContentTitle("Chess Helper")
                    .setContentText("Стрелка активна")
                    .setSmallIcon(android.R.drawable.ic_menu_compass)
                    .build();
            
            // ВАЖНО: указываем тип specialUse для Android 14+
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            } else {
                startForeground(1, notification);
            }

            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            arrowView = new ArrowView(this);
            
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.START;
            windowManager.addView(arrowView, params);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) { return null; }
    }

    public static class ArrowView extends View {
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
            float startX = 540f;
            float startY = 1600f;
            float endX = 540f;
            float endY = 1300f;
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
