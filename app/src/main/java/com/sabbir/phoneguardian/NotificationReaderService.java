
package com.sabbir.phoneguardian;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.app.Notification;
import android.os.Bundle;

public class NotificationReaderService
        extends NotificationListenerService {

    public static volatile String latestApp = "";
    public static volatile String latestName = "";
    public static volatile String latestMessage = "";
    public static volatile int notificationCount = 0;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        if (sbn == null) return;

        if (sbn.getPackageName().equals(getPackageName())) {
            return;
        }

        Notification notification = sbn.getNotification();

        if (notification == null) return;

        Bundle extras = notification.extras;

        if (extras == null) return;

        String appName = sbn.getPackageName();

        String title = extras.getCharSequence(
            Notification.EXTRA_TITLE, ""
        ).toString();

        String message = extras.getCharSequence(
            Notification.EXTRA_BIG_TEXT,
            extras.getCharSequence(
                Notification.EXTRA_TEXT, ""
            )
        ).toString();

        if (title.isEmpty() && message.isEmpty()) {
            return;
        }

        if ((notification.flags &
            Notification.FLAG_GROUP_SUMMARY) != 0) {
            return;
        }

        latestApp = appName;
        latestName = title;
        latestMessage = message;

        notificationCount++;
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // নোটিফিকেশন সরানো হলে আপাতত কিছু করার নেই।
    }
        }
