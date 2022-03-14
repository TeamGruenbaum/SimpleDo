package de.stevensolleder.simpledo.presenter.notifications;

import androidx.annotation.NonNull;

import de.stevensolleder.simpledo.model.Date;
import de.stevensolleder.simpledo.model.Time;

public interface INotificationHelper
{
    void createNotificationChannel(@NonNull String id, @NonNull String name, @NonNull String description, @NonNull int importance);
    void planAndSendNotification(@NonNull Date date, @NonNull Time time, @NonNull String content, @NonNull int id);
    void cancelNotification(int id);
}
