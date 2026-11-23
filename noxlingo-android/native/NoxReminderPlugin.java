package app.noxlingo.mobile.v2;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@CapacitorPlugin(name = "NoxReminder", permissions = {
    @Permission(alias = "notifications", strings = {Manifest.permission.POST_NOTIFICATIONS})
})
public class NoxReminderPlugin extends Plugin {
    private static final String PREFS = "noxlingo.reminder.native.v1";
    private static final String CHANNEL_ID = "noxlingo_daily_practice_v1";
    private static final int REQUEST_CODE = 8710;
    private static final int NOTIFICATION_ID = 8711;

    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!"app.noxlingo.mobile.v2.REMINDER_CHECK".equals(intent.getAction())) return;
            SharedPreferences prefs = settings(context);
            if (!prefs.getBoolean("enabled", false)) return;
            if (!today().equals(prefs.getString("lastPractice", ""))) {
                notifyUser(context, false);
            }
            scheduleNext(context);
        }
    }

    public static class BootReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
                scheduleNext(context);
            }
        }
    }

    private static SharedPreferences settings(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= 33
            && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        return manager != null && (Build.VERSION.SDK_INT < 24 || manager.areNotificationsEnabled());
    }

    private static PendingIntent alarmIntent(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("app.noxlingo.mobile.v2.REMINDER_CHECK");
        return PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static void scheduleNext(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        PendingIntent pending = alarmIntent(context);
        manager.cancel(pending);
        SharedPreferences prefs = settings(context);
        if (!prefs.getBoolean("enabled", false)) return;
        int hour = prefs.getInt("hour", 20);
        int minute = prefs.getInt("minute", 0);
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, hour);
        next.set(Calendar.MINUTE, minute);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis() + 1000) {
            next.add(Calendar.DATE, 1);
        }
        // Sem SCHEDULE_EXACT_ALARM: horário aproximado, respeitando restrições do Android.
        if (Build.VERSION.SDK_INT >= 23) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pending);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pending);
        }
    }

    private static void notifyUser(Context context, boolean test) {
        if (!hasNotificationPermission(context)) return;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Lembretes de prática — NoxLingo", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Lembrete diário apenas quando você ainda não praticou espanhol.");
            manager.createNotificationChannel(channel);
        }
        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openApp = PendingIntent.getActivity(
            context, 8712, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
            ? new Notification.Builder(context, CHANNEL_ID) : new Notification.Builder(context);
        builder.setSmallIcon(context.getApplicationInfo().icon)
            .setContentTitle(test ? "NoxLingo — teste de notificação" : "¿Un poquito de español hoy?")
            .setContentText(test ? "Está funcionando! O corvo vai lembrar você de praticar. :P"
                : "Você ainda não praticou hoje. Que tal um exercício rapidinho?")
            .setAutoCancel(true)
            .setContentIntent(openApp);
        manager.notify(test ? NOTIFICATION_ID + 1 : NOTIFICATION_ID, builder.build());
    }

    @PluginMethod
    public void status(PluginCall call) {
        SharedPreferences prefs = settings(getContext());
        JSObject result = new JSObject();
        result.put("available", true);
        result.put("enabled", prefs.getBoolean("enabled", false));
        result.put("permissionGranted", hasNotificationPermission(getContext()));
        result.put("hour", prefs.getInt("hour", 20));
        result.put("minute", prefs.getInt("minute", 0));
        result.put("lastPractice", prefs.getString("lastPractice", ""));
        result.put("practicedToday", today().equals(prefs.getString("lastPractice", "")));
        call.resolve(result);
    }

    @PluginMethod
    public void configure(PluginCall call) {
        if (Boolean.TRUE.equals(call.getBoolean("enabled", false)) && Build.VERSION.SDK_INT >= 33
            && getPermissionState("notifications") != PermissionState.GRANTED) {
            requestPermissionForAlias("notifications", call, "afterNotificationPermission");
            return;
        }
        applyConfiguration(call);
    }

    @PermissionCallback
    private void afterNotificationPermission(PluginCall call) {
        if (getPermissionState("notifications") != PermissionState.GRANTED) {
            call.reject("Permissão de notificação negada. Ative nas configurações do Android.", "NOTIFICATION_PERMISSION_DENIED");
            return;
        }
        applyConfiguration(call);
    }

    private void applyConfiguration(PluginCall call) {
        int hour = call.getInt("hour", 20);
        int minute = call.getInt("minute", 0);
        boolean enabled = Boolean.TRUE.equals(call.getBoolean("enabled", false));
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            call.reject("Horário inválido.", "BAD_TIME");
            return;
        }
        if (enabled && !hasNotificationPermission(getContext())) {
            call.reject("Notificações bloqueadas pelo Android. Autorize nas configurações do aplicativo.", "NOTIFICATIONS_DISABLED");
            return;
        }
        settings(getContext()).edit()
            .putBoolean("enabled", enabled)
            .putInt("hour", hour)
            .putInt("minute", minute)
            .apply();
        scheduleNext(getContext());
        JSObject response = new JSObject();
        response.put("enabled", enabled);
        response.put("hour", hour);
        response.put("minute", minute);
        call.resolve(response);
    }

    @PluginMethod
    public void markPractice(PluginCall call) {
        settings(getContext()).edit().putString("lastPractice", today()).apply();
        NotificationManager manager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.cancel(NOTIFICATION_ID);
        JSObject response = new JSObject();
        response.put("date", today());
        call.resolve(response);
    }

    @PluginMethod
    public void test(PluginCall call) {
        if (!hasNotificationPermission(getContext())) {
            call.reject("Autorize as notificações para testar.", "NOTIFICATIONS_DISABLED");
            return;
        }
        notifyUser(getContext(), true);
        call.resolve();
    }
}
