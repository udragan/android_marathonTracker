package com.udragan.android.marathontracker.models;

import java.util.List;

/**
 * POJO representing a marathon track.
 */
public class TrackModel {

    // members **********************************************************************************************************

    private int mId;
    private String mName;
    private boolean mIsComplete;
    private long mDuration;
    private List<CheckpointModel> mCheckpoints;

    // constructors *****************************************************************************************************

    /**
     * Initializes a new instance of {@link com.udragan.android.marathontracker.models.TrackModel} class.
     *
     * @param id          track id.
     * @param name        track name.
     * @param isComplete  is track complete.
     * @param duration    track duration.
     * @param checkpoints track checkpoints.
     */
    public TrackModel(int id,
                      String name,
                      boolean isComplete,
                      long duration,
                      List<CheckpointModel> checkpoints) {
        mId = id;
        mName = name;
        mIsComplete = isComplete;
        mDuration = duration;
        mCheckpoints = checkpoints;
    }

    // properties *******************************************************************************************************

    /**
     * Returns the id of the track.
     *
     * @return track id.
     */
    public int getId() {
        return mId;
    }

    /**
     * Sets the track id.
     *
     * @param id new value of the track id.
     */
    public void setId(int id) {
        mId = id;
    }

    /**
     * Returns the name of the track.
     *
     * @return track name.
     */
    public String getName() {
        return mName;
    }

    /**
     * Sets the track name.
     *
     * @param name new value of the track name.
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Returns if the track is complete.
     *
     * @return true if track is complete, false otherwise.
     */
    public boolean getIsComplete() {
        return mIsComplete;
    }

    /**
     * Sets is track complete.
     *
     * @param isComplete new value of track completeness.
     */
    public void setIsComplete(boolean isComplete) {
        mIsComplete = isComplete;
    }

    /**
     * Returns the track duration if the track is complete, 0 otherwise.
     *
     * @return track duration.
     */
    public long getDuration() {
        return mDuration;
    }

    /**
     * Sets the track duration.
     *
     * @param duration new value of track duration.
     */
    public void setDuration(long duration) {
        mDuration = duration;
    }

    /**
     * Returns track checkpoints.
     *
     * @return track checkpoints.
     */
    public List<CheckpointModel> getCheckpoints() {
        return mCheckpoints;
    }

    /**
     * Sets track checkpoints.
     *
     * @param checkpoints new value of track checkpoints.
     */
    public void setCheckpoints(List<CheckpointModel> checkpoints) {
        mCheckpoints = checkpoints;
    }
}
