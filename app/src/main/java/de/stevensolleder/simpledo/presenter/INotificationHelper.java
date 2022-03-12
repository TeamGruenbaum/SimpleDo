package de.stevensolleder.simpledo.presenter;

import de.stevensolleder.simpledo.model.Date;
import de.stevensolleder.simpledo.model.Time;

public interface INotificationHelper
{
    void createNotificationChannel();
    void planAndSendNotification(Time time, Date date, String content, int id);
    void cancelNotification(int id);
    void updateAlldayNotifications();
}
