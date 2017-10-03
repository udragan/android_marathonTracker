package com.udragan.android.marathontracker.infrastructure.parsers;

import android.util.Xml;

import com.udragan.android.marathontracker.infrastructure.common.Constants;
import com.udragan.android.marathontracker.infrastructure.common.XmlParserBase;
import com.udragan.android.marathontracker.models.CheckpointModel;
import com.udragan.android.marathontracker.models.TrackModel;
import com.udragan.android.marathontracker.providers.MarathonContract;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Xml parser for tracks.
 */
public class TrackXmlParser extends XmlParserBase {

    // public methods ***************************************************************************************************

    /**
     * Parse xml provided in input stream.
     *
     * @param is input stream.
     * @return a list of parsed tracks.
     * @throws XmlPullParserException if pull parser related fault.
     * @throws IOException            if I/O operation failed or is interrupted.
     */
    public List<TrackModel> parse(InputStream is)
            throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
            parser.nextTag();

            return readTracks(parser);
        } finally {
            is.close();
        }
    }

    // private methods **************************************************************************************************

    private List<TrackModel> readTracks(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, getNS(), "tracks");

        List<TrackModel> tracks = new ArrayList<>();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();

            if (name.equals("track")) {
                tracks.add(readTrack(parser));
            } else {
                skip(parser);
            }
        }

        return tracks;
    }

    private TrackModel readTrack(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, getNS(), "track");

        int id = MarathonContract.INVALID_TRACK_ID;
        String trackName = "";
        List<CheckpointModel> checkpoints = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();

            switch (name) {
                case "name":
                    trackName = readString(parser, "name");
                    break;

                case "checkpoints":
                    checkpoints = readCheckpoints(parser);
                    break;

                default:
                    skip(parser);
                    break;
            }
        }

        return new TrackModel(id,
                trackName,
                false,
                Constants.INVALID_TIME_MILLIS,
                checkpoints);
    }

    private List<CheckpointModel> readCheckpoints(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, getNS(), "checkpoints");

        List<CheckpointModel> checkpoints = new ArrayList<>();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();

            if (name.equals("checkpoint")) {
                checkpoints.add(readCheckpoint(parser));
            } else {
                skip(parser);
            }
        }

        return checkpoints;
    }

    private CheckpointModel readCheckpoint(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, getNS(), "checkpoint");

        int id = MarathonContract.INVALID_CHECKPOINT_ID;
        String checkpointName = "";
        int index = MarathonContract.INVALID_CHECKPOINT_INDEX;
        double lat = Double.NEGATIVE_INFINITY;
        double lon = Double.NEGATIVE_INFINITY;
        boolean isChecked = false;
        long time = Constants.INVALID_TIME_MILLIS;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();

            switch (name) {
                case "id":
                    id = readInt(parser, "id");
                    break;

                case "name":
                    checkpointName = readString(parser, "name");
                    break;

                case "index":
                    index = readInt(parser, "index");
                    break;

                case "lat":
                    lat = readDouble(parser, "lat");
                    break;

                case "lon":
                    lon = readDouble(parser, "lon");
                    break;

                case "isChecked":
                    isChecked = readBoolean(parser, "isChecked");
                    break;

                case "time":
                    time = readLong(parser, "time");
                    break;

                default:
                    skip(parser);
                    break;
            }
        }

        return new CheckpointModel(id,
                checkpointName,
                index,
                lat,
                lon,
                isChecked,
                time);
    }
}
