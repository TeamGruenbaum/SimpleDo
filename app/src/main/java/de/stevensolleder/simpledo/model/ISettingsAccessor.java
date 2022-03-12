package de.stevensolleder.simpledo.model;

public interface ISettingsAccessor
{
    Direction getSortDirection();
    void setSortDirection(Direction newValue);
    Criterion getSortCriterion();
    void setSortCriterion(Criterion newValue);
    Time getAlldayTime();
    void setAlldayTime(Time newValue);
}
