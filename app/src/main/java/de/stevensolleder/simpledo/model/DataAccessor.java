package de.stevensolleder.simpledo.model;

import java.util.List;

public interface DataAccessor
{
    List<Entry> getEntries();
    void setEntries(List<Entry> entries);
    void addEntry(Entry newValue);
    void addEntry(int index, Entry newValue);
    Entry getEntry(int index);
    void changeEntry(Entry newValue, int index);
    int getEntriesSize();
    void removeEntry(int index);
    void swapEntries(int fromIndex, int toIndex);
    void sortEntries();

    Direction getSortDirection();
    void setSortDirection(Direction newValue);
    Criterion getSortCriterion();
    void setSortCriterion(Criterion newValue);
    Time getAlldayTime();
    void setAlldayTime(Time newValue);
}
