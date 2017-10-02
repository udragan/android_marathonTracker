package com.udragan.android.marathontracker.infrastructure.interfaces;

import com.udragan.android.marathontracker.models.TrackModel;

import java.util.List;

/**
 * Interface for download track async task.
 */
public interface IDownloadTrackCallback {

    /**
     * Initiate the callback and provide downloaded tracks.
     *
     * @param tracks downloaded and parsed tracks.
     */
    void tracksDownloaded(List<TrackModel> tracks);
}
