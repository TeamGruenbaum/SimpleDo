package de.gruenbaum.simpledo.model;

public interface ISortSettingsAccessor
{
    Direction getSortDirection();
    void setSortDirection(Direction newValue);
    Criterion getSortCriterion();
    void setSortCriterion(Criterion newValue);
}
