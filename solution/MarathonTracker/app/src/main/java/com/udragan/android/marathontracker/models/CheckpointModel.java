package com.udragan.android.marathontracker.models;

/**
 * POJO representing a Checkpoint in marathon track.
 */
public class CheckpointModel {

    // members **********************************************************************************************************

    private int mId;
    private String mName;
    private int mIndex;
    private double mLatitude;
    private double mLongitude;
    private boolean mIsChecked;
    private long mTime;

    // constructors *****************************************************************************************************

    /**
     * Initializes a new instance of {@link com.udragan.android.marathontracker.models.CheckpointModel} class.
     *
     * @param id        initial id.
     * @param name      initial name.
     * @param index     initial index.
     * @param latitude  initial latitude.
     * @param longitude initial longitude.
     * @param isChecked weather checkpoint is checked.
     * @param time      initial time the checkpoint is checked.
     */
    public CheckpointModel(int id,
                           String name,
                           int index,
                           double latitude,
                           double longitude,
                           boolean isChecked,
                           long time) {
        mId = id;
        mName = name;
        mIndex = index;
        mLatitude = latitude;
        mLongitude = longitude;
        mIsChecked = isChecked;
        mTime = time;
    }

    // properties *******************************************************************************************************

    /**
     * Returns the id of the checkpoint.
     *
     * @return checkpoint id.
     */
    public int getId() {
        return mId;
    }

    /**
     * Sets the checkpoint id.
     *
     * @param id new value of the checkpoint id.
     */
    public void setId(int id) {
        mId = id;
    }

    /**
     * Returns the name of the checkpoint.
     *
     * @return checkpoint name.
     */
    public String getName() {
        return mName;
    }

    /**
     * Sets the checkpoint name.
     *
     * @param name new value of the checkpoint name.
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Returns the index of checkpoint in the track.
     *
     * @return checkpoint index in track.
     */
    public int getIndex() {
        return mIndex;
    }

    /**
     * Sets the new index value.
     *
     * @param index new value of checkpoint index.
     */
    public void setIndex(int index) {
        mIndex = index;
    }

    /**
     * Returns the latitude of the checkpoint.
     *
     * @return latitude of the checkpoint.
     */
    public double getLatitude() {
        return mLatitude;
    }

    /**
     * Sets the new latitude value.
     *
     * @param latitude new value of checkpoint latitude.
     */
    public void setLatitude(long latitude) {
        mLatitude = latitude;
    }

    /**
     * Returns the longitude of the checkpoint.
     *
     * @return longitude of the checkpoint.
     */
    public double getLongitude() {
        return mLongitude;
    }

    /**
     * Sets the new longitude value.
     *
     * @param longitude new value of checkpoint longitude.
     */
    public void setLongitude(long longitude) {
        mLongitude = longitude;
    }

    /**
     * Returns weather the checkpoint is checked.
     *
     * @return true if checked, false otherwise.
     */
    public boolean getIsChecked() {
        return mIsChecked;
    }

    /**
     * Sets weather the checkpoint is checked.
     *
     * @param isChecked new value of checkpoint state.
     */
    public void setIsChecked(boolean isChecked) {
        mIsChecked = isChecked;
    }

    /**
     * Returns the time in milliseconds when checkpoint is checked.
     *
     * @return time of checking of the checkpoint in milliseconds.
     */
    public long getTime() {
        return mTime;
    }

    /**
     * Sets the new time value.
     *
     * @param time new value of checkpoint time.
     */
    public void setTime(long time) {
        mTime = time;
    }
}
