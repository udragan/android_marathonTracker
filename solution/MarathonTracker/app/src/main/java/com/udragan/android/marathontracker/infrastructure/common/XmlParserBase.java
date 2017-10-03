package com.udragan.android.marathontracker.infrastructure.common;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Base class for xml pull parser.
 */
public class XmlParserBase {

    // members **********************************************************************************************************

    private String mNamespace = null;

    // properties *******************************************************************************************************

    /**
     * Returns the namespace of xml document.
     *
     * @return xml namespace.
     */
    protected String getNS() {
        return mNamespace;
    }

    /**
     * Sets the namespace of the parsed xml.
     *
     * @param namespace new value of the namespace.
     */
    protected void setNS(String namespace) {
        mNamespace = namespace;
    }

    // protected methods ************************************************************************************************

    /**
     * Reads the value of provided tag and parses it to boolean.
     *
     * @param parser  xml pull parser.
     * @param tagName tag to read value from.
     * @return parsed value of provided tag.
     * @throws XmlPullParserException if pull parser related fault.
     * @throws IOException            if I/O operation failed or is interrupted.
     */
    protected boolean readBoolean(XmlPullParser parser,
                                  String tagName)
            throws XmlPullParserException, IOException {
        String value = readString(parser, tagName);

        return Boolean.parseBoolean(value);
    }

    /**
     * Reads the value of provided tag and parses it to int.
     *
     * @param parser  xml pull parser.
     * @param tagName tag to read value from.
     * @return parsed value of provided tag.
     * @throws XmlPullParserException if pull parser related fault.
     * @throws IOException            if I/O operation failed or is interrupted.
     */
    protected int readInt(XmlPullParser parser,
                          String tagName)
            throws XmlPullParserException, IOException {
        String value = readString(parser, tagName);

        return Integer.parseInt(value);
    }

    /**
     * Reads the value of provided tag and parses it to long.
     *
     * @param parser  xml pull parser.
     * @param tagName tag to read value from.
     * @return parsed value of provided tag.
     * @throws XmlPullParserException if pull parser related fault.
     * @throws IOException            if I/O operation failed or is interrupted.
     */
    protected long readLong(XmlPullParser parser,
                            String tagName)
            throws XmlPullParserException, IOException {
        String value = readString(parser, tagName);

        return Long.parseLong(value);
    }

    /**
     * Reads the value of provided tag and parses it to double.
     *
     * @param parser  xml pull parser.
     * @param tagName tag to read value from.
     * @return parsed value of provided tag.
     * @throws XmlPullParserException if pull parser related fault.
     * @throws IOException            if I/O operation failed or is interrupted.
     */
    protected double readDouble(XmlPullParser parser,
                                String tagName)
            throws XmlPullParserException, IOException {
        String value = readString(parser, tagName);

        return Double.parseDouble(value);
    }

    /**
     * Reads the value of provided tag as string.
     *
     * @param parser  xml pull parser.
     * @param tagName tag to read value from.
     * @return value of provided tag.
     * @throws XmlPullParserException if pull parser related fault.
     * @throws IOException            if I/O operation failed or is interrupted.
     */
    protected String readString(XmlPullParser parser,
                                String tagName)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, mNamespace, tagName);
        String value = readText(parser);
        parser.require(XmlPullParser.END_TAG, mNamespace, tagName);

        return value;
    }

    /**
     * Skips the entire tag of provided parser.
     *
     * @param parser xml pull parser.
     * @throws XmlPullParserException if pull parser related fault.
     * @throws IOException            if I/O operation failed or is interrupted.
     */
    protected void skip(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }

        int depth = 1;

        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.START_TAG:
                    depth++;
                    break;

                case XmlPullParser.END_TAG:
                    depth--;
                    break;
            }
        }
    }

    // private methods **************************************************************************************************

    private String readText(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        String result = "";

        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }

        return result;
    }
}
