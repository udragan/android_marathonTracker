package com.udragan.android.marathontracker.infrastructure.interfaces;

/**
 * Interface for cursor loader callback.
 */
public interface ICursorLoaderCallback {

    /**
     * Initiate callback to refresh cursor loader.
     * @param id identifier to refresh the loader with
     */
    void LoadCursor(int id);
}
