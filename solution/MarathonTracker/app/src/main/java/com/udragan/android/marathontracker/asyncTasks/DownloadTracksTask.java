package com.udragan.android.marathontracker.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.udragan.android.marathontracker.infrastructure.interfaces.IDownloadTrackCallback;
import com.udragan.android.marathontracker.infrastructure.parsers.TrackXmlParser;
import com.udragan.android.marathontracker.models.TrackModel;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Async task for downloading tracks.
 */
public class DownloadTracksTask extends AsyncTask<InputStream, Void, List<TrackModel>> {

    // members **********************************************************************************************************

    private Context mContext;

    // constructors *****************************************************************************************************

    /**
     * Initializes a new instance of {@link com.udragan.android.marathontracker.asyncTasks.DownloadTracksTask} class.
     *
     * @param context the calling context/activity.
     */
    public DownloadTracksTask(Context context) {
        if (!(context instanceof IDownloadTrackCallback)) {
            throw new ClassCastException(String.format("Provided context does not implement '%s'",
                    IDownloadTrackCallback.class.getSimpleName()));
        }

        mContext = context;
    }

    // overrides ********************************************************************************************************

    @Override
    protected List<TrackModel> doInBackground(InputStream... streams) {
        InputStream is = streams[0];
        List<TrackModel> tracks = null;

        TrackXmlParser parser = new TrackXmlParser();

        try {
            tracks = parser.parse(is);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return tracks;
    }

    @Override
    protected void onPostExecute(List<TrackModel> result) {
        ((IDownloadTrackCallback) mContext).tracksDownloaded(result);
    }
}
