package com.udragan.android.marathontracker.models;

/**
 * POJO representing a Checkpoint in marathon track.
 */
public class CheckpointModel {
    // fields ***********************************************************************************************************

    private long mLatitude;
    private long mLongitude;

    // constructors *****************************************************************************************************

    /**
     * Initializes a new instance of {@link com.udragan.android.marathontracker.models.CheckpointModel} class.
     *
     * @param latitude  initial latitude.
     * @param longitude initial longitude.
     */
    public CheckpointModel(long latitude, long longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
    }

    // properties *******************************************************************************************************

    /**
     * Returns the latitude of the checkpoint.
     *
     * @return latitude of the checkpoint.
     */
    public long getLatitude() {
        return mLatitude;
    }

    /**
     * Sets the new latitude value.
     *
     * @param latitude value to set latitude to.
     */
    public void setLatitude(long latitude) {
        mLatitude = latitude;
    }

    /**
     * Returns the longitude of the checkpoint.
     *
     * @return longitude of the checkpoint.
     */
    public long getLongitude() {
        return mLongitude;
    }

    /**
     * Sets the new longitude value.
     *
     * @param longitude value to set longitude to.
     */
    public void setLongitude(long longitude) {
        mLongitude = longitude;
    }
}
