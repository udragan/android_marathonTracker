package com.udragan.android.marathontracker.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.udragan.android.marathontracker.R;
import com.udragan.android.marathontracker.infrastructure.common.Constants;
import com.udragan.android.marathontracker.infrastructure.interfaces.ICursorLoaderCallback;
import com.udragan.android.marathontracker.providers.MarathonContract;

import static android.content.Context.MODE_PRIVATE;

/**
 * Adapter for {@link com.udragan.android.marathontracker.providers.MarathonContract.TrackEntry}.
 */
public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    // members **********************************************************************************************************

    private static final String TAG = TrackAdapter.class.getSimpleName();

    private Context mContext;
    private Cursor mCursor;
    private int mSelectedIndex;

    // constructors *****************************************************************************************************

    /**
     * Initializes a new instance of {@link TrackAdapter} class.
     *
     * @param context context
     * @param cursor  cursor to providing data
     */
    public TrackAdapter(Context context,
                        Cursor cursor) {
        if (!(context instanceof ICursorLoaderCallback)) {
            throw new ClassCastException(String.format("Provided context does not implement '%s'",
                    ICursorLoaderCallback.class.getSimpleName()));
        }

        mContext = context;
        mCursor = cursor;
        mSelectedIndex = RecyclerView.NO_POSITION;
    }

    // overrides ********************************************************************************************************

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent,
                                              int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.track_view, parent, false);

        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrackViewHolder holder,
                                 int position) {
        mCursor.moveToPosition(position);

        int idColumnIndex = mCursor.getColumnIndex(MarathonContract.TrackEntry._ID);
        int nameColumnIndex = mCursor.getColumnIndex(MarathonContract.TrackEntry.COLUMN_NAME);
        int isCompleteColumnIndex = mCursor.getColumnIndex(MarathonContract.TrackEntry.COLUMN_IS_COMPLETE);
        int durationColumnIndex = mCursor.getColumnIndex(MarathonContract.TrackEntry.COLUMN_DURATION);

        holder.mIdView.setText(String.valueOf(mCursor.getLong(idColumnIndex)));
        holder.mNameView.setText(mCursor.getString(nameColumnIndex));
        holder.mIsComplete.setChecked(mCursor.getInt(isCompleteColumnIndex) == 1);
        holder.mDuration.setText(String.valueOf(mCursor.getLong(durationColumnIndex)));

        int backgroundColor = Color.TRANSPARENT;

        if (position == mSelectedIndex) {
            TypedValue typedValue = new TypedValue();

            if (mContext.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true)) {
                backgroundColor = typedValue.data;
            }
        }

        holder.itemView.setBackgroundColor(backgroundColor);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }

        return mCursor.getCount();
    }

    // public methods ***************************************************************************************************

    /**
     * Swap adapter cursor when new data is available.
     *
     * @param cursor data provider
     */
    public void swapCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = cursor;

        if (mCursor != null) {
            notifyDataSetChanged();
        }
    }

    /**
     * Select track by id.
     *
     * @param id id of the track to select
     */
    public void selectTrack(int id) {
        if (mCursor != null) {
            Log.d(TAG, String.format("Selecting track with id: %d",
                    id));

            if (mCursor.moveToFirst()) {
                int idColumnIndex = mCursor.getColumnIndex(MarathonContract.TrackEntry._ID);
                int index = 0;

                do {
                    if (mCursor.getInt(idColumnIndex) == id) {
                        mSelectedIndex = index;
                        notifyItemChanged(mSelectedIndex);
                        ((ICursorLoaderCallback) mContext).LoadCursor(id);

                        return;
                    }

                    index++;
                } while (mCursor.moveToNext());
            }

            Log.i(TAG, String.format("Track with id: %d not found.",
                    id));
        } else {
            Log.w(TAG, "Attempted to select track while track cursor is null!");
        }
    }

    // ViewHolder class *************************************************************************************************
    // ******************************************************************************************************************

    class TrackViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        // members ******************************************************************************************************

        private TextView mIdView;
        private TextView mNameView;
        private CheckBox mIsComplete;
        private TextView mDuration;

        // constructors *************************************************************************************************

        TrackViewHolder(View itemView) {
            super(itemView);

            mIdView = itemView.findViewById(R.id.track_id_text);
            mNameView = itemView.findViewById(R.id.track_name_text);
            mIsComplete = itemView.findViewById(R.id.track_is_complete_checkbox);
            mDuration = itemView.findViewById(R.id.track_duration_text);

            itemView.setOnClickListener(this);
        }

        // overrides ****************************************************************************************************

        @Override
        public void onClick(View view) {
            int selectedIndex = getAdapterPosition();

            if (selectedIndex == RecyclerView.NO_POSITION) {
                return;
            }

            notifyItemChanged(mSelectedIndex);
            mSelectedIndex = selectedIndex;
            notifyItemChanged(mSelectedIndex);

            TextView idView = view.findViewById(R.id.track_id_text);
            int id = Integer.parseInt(idView.getText().toString());
            ((ICursorLoaderCallback) mContext).LoadCursor(id);
            saveLastActiveTrackIdPreference(id);
        }

        // private methods **********************************************************************************************

        private void saveLastActiveTrackIdPreference(int id) {
            SharedPreferences preferences = mContext.getSharedPreferences(Constants.GLOBAL_PREFERENCES_KEY, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(Constants.PREFERENCE_KEY_LAST_ACTIVE_TRACK_ID, id);
            editor.apply();
            Log.v(TAG, String.format("Saved preference %s: %s",
                    Constants.PREFERENCE_KEY_LAST_ACTIVE_TRACK_ID, id));
        }
    }
}
