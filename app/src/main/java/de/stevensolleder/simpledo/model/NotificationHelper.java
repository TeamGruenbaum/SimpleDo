package de.stevensolleder.simpledo.model;

public interface NotificationHelper<T>
{
    void createNotificationChannel();
    void planAndSendNotification(T information);
    void cancelNotification(T information);
    void updateAlldayNotifications();
}
