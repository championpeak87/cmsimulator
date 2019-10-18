package fiitstu.gulis.cmsimulator.elements;

import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import fiitstu.gulis.cmsimulator.database.FileFormatException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * The result of solving a task
 *
 * Created by Jakub Sedlář on 15.01.2018.
 */
public class TaskResult {

    private static final String ROOT_TAG = "task_result";
    private static final String NAME_TAG = "name";
    private static final String VERSION_TAG = "version";
    private static final String POSITIVE_TAG = "correct";
    private static final String POSITIVE_MAX_TAG = "correct_max";
    private static final String NEGATIVE_TAG = "incorrect";
    private static final String NEGATIVE_MAX_TAG = "incorrect_max";
    //legacy tags
    private static final String SCORE_TAG = "score";
    private static final String MAX_TAG = "max";

    //version 3.0-3.2 used 0 (number is applied retro-actively - versioning did not exist in those versions)
    public static final int CURRENT_VERSION = 1;

    private String name;
    private int positive;
    private int maxPositive;
    private int negative;
    private int maxNegative;
    private int version = CURRENT_VERSION;

    public TaskResult(String name, int positive, int maxPositive, int negative, int maxNegative) {
        this.name = name;
        this.positive = positive;
        this.maxPositive = maxPositive;
        this.negative = negative;
        this.maxNegative = maxNegative;
    }

    private TaskResult() {
    }

    private static String readAttribute(XmlPullParser parser, String tag) throws IOException,
            XmlPullParserException,
            FileFormatException {

        if (parser.nextTag() != XmlPullParser.START_TAG || !parser.getName().equals(tag)) {
            throw new FileFormatException("did not find expected tag: " + tag);
        }

        return parser.nextText();
    }

    /**
     * Deserializes result from an XML.
     * Version 0 did no differentiate between scores from positive and negative test inputs,
     * if an XML of that version is loaded, getPositive and getNegative will both return the total number of tests
     * @param xml the serialized task result
     * @return the deserialized TaskResult
     * @throws XmlPullParserException if something goes wrong during parsing
     * @throws IOException if something goes wrong during parsing
     * @throws FileFormatException if the XML does not contain a properly serialized task result
     */
    public static TaskResult fromXML(String xml) throws XmlPullParserException, IOException, FileFormatException {
        XmlPullParser xmlPullParser = Xml.newPullParser();
        StringReader reader = new StringReader(xml);

        TaskResult result = new TaskResult();

        xmlPullParser.setInput(reader);

        int eventType = xmlPullParser.nextTag();
        if (eventType != XmlPullParser.START_TAG || !xmlPullParser.getName().equals(ROOT_TAG)) {
            throw new FileFormatException("did not find expected tag: " + ROOT_TAG);
        }

        eventType = xmlPullParser.nextTag();
        if (eventType != XmlPullParser.START_TAG) {
            throw new FileFormatException("unexpected end of structure");
        }
        if (xmlPullParser.getName().equals(VERSION_TAG)) {
            result.setVersion(Integer.parseInt(xmlPullParser.nextText()));

            if (result.getVersion() == 1) {
                result.setName(readAttribute(xmlPullParser, NAME_TAG));
                result.setPositive(Integer.parseInt(readAttribute(xmlPullParser, POSITIVE_TAG)));
                result.setMaxPositive(Integer.parseInt(readAttribute(xmlPullParser, POSITIVE_MAX_TAG)));
                result.setNegative(Integer.parseInt(readAttribute(xmlPullParser, NEGATIVE_TAG)));
                result.setMaxNegative(Integer.parseInt(readAttribute(xmlPullParser, NEGATIVE_MAX_TAG)));
            }
            else {
                throw  new FileFormatException("unknown version: " + result.getVersion());
            }
        }
        else { //version 0 did not have version element
            result.setVersion(0);
            if (xmlPullParser.getName().equals(NAME_TAG)) {
                result.setName(xmlPullParser.nextText());
            }
            else {
                throw new FileFormatException("did not find expected tag: " + NAME_TAG);
            }

            result.setPositive(Integer.parseInt(readAttribute(xmlPullParser, SCORE_TAG)));
            result.setNegative(result.getPositive());
            result.setMaxPositive(Integer.parseInt(readAttribute(xmlPullParser, MAX_TAG)));
            result.setMaxNegative(result.getMaxPositive());
        }

        eventType = xmlPullParser.nextTag();
        if (eventType != XmlPullParser.END_TAG || !xmlPullParser.getName().equals(ROOT_TAG)) {
            throw new FileFormatException("did not find expected closing tag: /" + ROOT_TAG);
        }

        return result;
    }

    public String toXML() throws IOException {
        XmlSerializer xmlSerializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        xmlSerializer.setOutput(writer);

        xmlSerializer.startDocument("UTF-8", true);

        xmlSerializer.startTag("", ROOT_TAG);

        if (getVersion() == 0) {
            xmlSerializer.startTag("", NAME_TAG);
            xmlSerializer.text(getName());
            xmlSerializer.endTag("", NAME_TAG);

            xmlSerializer.startTag("", SCORE_TAG);
            xmlSerializer.text(String.valueOf(getPositive() + getNegative()));
            xmlSerializer.endTag("", SCORE_TAG);

            xmlSerializer.startTag("", MAX_TAG);
            xmlSerializer.text(String.valueOf(getMaxPositive() + getMaxNegative()));
            xmlSerializer.endTag("", MAX_TAG);
        }
        else {
            xmlSerializer.startTag("", VERSION_TAG);
            xmlSerializer.text(String.valueOf(getVersion()));
            xmlSerializer.endTag("", VERSION_TAG);

            //the following may be specific for version 1, but since newer versions don't exist
            //yet, behavior for them is undefined anyway, so no need for another "if"
            xmlSerializer.startTag("", NAME_TAG);
            xmlSerializer.text(getName());
            xmlSerializer.endTag("", NAME_TAG);

            xmlSerializer.startTag("", POSITIVE_TAG);
            xmlSerializer.text(String.valueOf(getPositive()));
            xmlSerializer.endTag("", POSITIVE_TAG);

            xmlSerializer.startTag("", POSITIVE_MAX_TAG);
            xmlSerializer.text(String.valueOf(getMaxPositive()));
            xmlSerializer.endTag("", POSITIVE_MAX_TAG);

            xmlSerializer.startTag("", NEGATIVE_TAG);
            xmlSerializer.text(String.valueOf(getNegative()));
            xmlSerializer.endTag("", NEGATIVE_TAG);

            xmlSerializer.startTag("", NEGATIVE_MAX_TAG);
            xmlSerializer.text(String.valueOf(getMaxNegative()));
            xmlSerializer.endTag("", NEGATIVE_MAX_TAG);
        }

        xmlSerializer.endTag("", ROOT_TAG);
        xmlSerializer.endDocument();

        return writer.toString();
    }

    /**
     * Returns the name of the solver
     * @return the name of the solver
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the solver
     * @param name the name of the solver
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the obtained score on positive tests
     * @return the obtained score on positive tests
     */
    public int getPositive() {
        return positive;
    }

    /**
     * Sets the obtained score on positive tests
     * @param positive the obtained score on positive tests
     */
    public void setPositive(int positive) {
        this.positive = positive;
    }

    /**
     * Returns the maximum obtainable score on positive tests
     * @return the maximum obtainable score on positive tests
     */
    public int getMaxPositive() {
        return maxPositive;
    }

    /**
     * Sets the maximum obtainable score on positive tests
     * @param maxPositive the maximum obtainable score on positive tests
     */
    public void setMaxPositive(int maxPositive) {
        this.maxPositive = maxPositive;
    }

    /**
     * Returns the obtained score on negative tests
     * @return the obtained score on negative tests
     */
    public int getNegative() {
        return negative;
    }

    /**
     * Sets the obtained score on negative tests
     * @param negative the obtained score on negative tests
     */
    public void setNegative(int negative) {
        this.negative = negative;
    }

    /**
     * Returns the maximum obtainable score on negative tests
     * @return the maximum obtainable score on negative tests
     */
    public int getMaxNegative() {
        return maxNegative;
    }

    /**
     * Sets the maximum obtainable score on negative tests
     * @param maxNegative the maximum obtainable score on negative tests
     */
    public void setMaxNegative(int maxNegative) {
        this.maxNegative = maxNegative;
    }

    /**
     * If the TaskResult was created by the {@link #fromXML(String)}, returns the version of the XML,
     * otherwise returns {@link #CURRENT_VERSION}
     * @return the version of the XML file the result was extracted from, or {@link #CURRENT_VERSION}
     * if it was not extracted form XML file
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the version the XML version of the TaskResult. This affects how it will be serialized
     * by {@link #toXML()}
     * @param version the version the XML version of the TaskResult
     */
    public void setVersion(int version) {
        this.version = version;
    }
}
