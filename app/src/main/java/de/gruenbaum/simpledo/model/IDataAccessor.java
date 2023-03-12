package de.gruenbaum.simpledo.model;

import java.util.List;

public interface IDataAccessor
{
    List<Entry> getEntries();
    void setEntries(List<Entry> entries);
    void addEntry(Entry newValue);
    void addEntry(int index, Entry newValue);
    Entry getEntry(int index);
    void changeEntry(int index, Entry newValue);
    int getEntriesSize();
    void removeEntry(int index);
    void swapEntries(int fromIndex, int toIndex);
    void sortEntries(Criterion sortCriterion, Direction sortDirection);
}
