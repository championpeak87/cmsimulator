package fiitstu.gulis.cmsimulator.network.automata_tasks;

import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.*;
import fiitstu.gulis.cmsimulator.models.tasks.automata_type;
import fiitstu.gulis.cmsimulator.models.tasks.deterministic;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class AutomataTaskParser {
    private File inputFile;

    // XML TAGS
    private static final String AUTOMATA_KEY = "automaton";
    private static final String AUTOMATA_TYPE_KEY = "type";
    private static final String AUTOMATA_TYPE_FA_KEY = "fsa";
    private static final String AUTOMATA_TYPE_TM_KEY = "tm";
    private static final String AUTOMATA_TYPE_PDA_KEY = "pda";
    private static final String AUTOMATA_TYPE_LBA_KEY = "lba";
    private static final String TASK_TITLE_KEY = "title";
    private static final String TASK_DESCRIPTION_KEY = "text";
    private static final String TASK_PUBLIC_INPUTS_KEY = "public_inputs";
    private static final String TASK_MAX_STEPS_KEY = "max_steps";

    private automata_type getAutomataType() throws IOException, SAXException, ParserConfigurationException {
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(this.inputFile);

        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName(AUTOMATA_KEY);
        Node nNode = nList.item(0);
        Element e = (Element) nNode;

        String automata_type = e.getAttribute(AUTOMATA_TYPE_FA_KEY);
        switch (automata_type) {
            case AUTOMATA_TYPE_FA_KEY:
                return fiitstu.gulis.cmsimulator.models.tasks.automata_type.FINITE_AUTOMATA;
            case AUTOMATA_TYPE_TM_KEY:
                return fiitstu.gulis.cmsimulator.models.tasks.automata_type.TURING_MACHINE;
            case AUTOMATA_TYPE_PDA_KEY:
                return fiitstu.gulis.cmsimulator.models.tasks.automata_type.PUSHDOWN_AUTOMATA;
            case AUTOMATA_TYPE_LBA_KEY:
                return fiitstu.gulis.cmsimulator.models.tasks.automata_type.LINEAR_BOUNDED_AUTOMATA;
            default:
                return fiitstu.gulis.cmsimulator.models.tasks.automata_type.UNKNOWN;
        }
    }

    private String getTaskTitle() throws IOException, SAXException, ParserConfigurationException {
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(this.inputFile);

        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName(AUTOMATA_KEY);
        Node nNode = nList.item(0);
        Element e = (Element) nNode;

        String taskTitle = e.getAttribute(TASK_TITLE_KEY);
        return taskTitle;
    }

    private String getTeskDescription() throws IOException, SAXException, ParserConfigurationException {
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(this.inputFile);

        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName(AUTOMATA_KEY);
        Node nNode = nList.item(0);
        Element e = (Element) nNode;

        String taskDescription = e.getAttribute(TASK_DESCRIPTION_KEY);
        return taskDescription;
    }

    private boolean getPublicInputs() throws  IOException, SAXException, ParserConfigurationException {
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(this.inputFile);

        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName(AUTOMATA_KEY);
        Node nNode = nList.item(0);
        Element e = (Element) nNode;

        boolean publicInputs = e.getAttribute(TASK_PUBLIC_INPUTS_KEY) == "true" ? true : false;

        return publicInputs;
    }

    private int getMaxSteps() throws IOException, SAXException, ParserConfigurationException
    {
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(this.inputFile);

        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName(AUTOMATA_KEY);
        Node nNode = nList.item(0);
        Element e = (Element) nNode;

        String maxSteps = e.getAttribute(TASK_MAX_STEPS_KEY);
        int maxSteps_int = Integer.parseInt(maxSteps);
        return maxSteps_int;
    }

    public AutomataTask getAutomataTask()
    {
        String taskName = "", taskDescription = "", fileName = inputFile.getName();
        automata_type automata_type = null;
        boolean publicInputs = true;
        int maxSteps = -1;
        try{
            taskName = this.getTaskTitle();
            taskDescription = this.getTeskDescription();
            automata_type = this.getAutomataType();
            publicInputs = this.getPublicInputs();
            maxSteps = this.getMaxSteps();
        } catch(IOException e)
        {
            System.err.println("FILE COULD NOT BE OPENED");
        } catch(SAXException e)
        {
            System.err.println("PARSING ERROR");
        } catch (ParserConfigurationException e)
        {
            System.err.println("PARSING ERROR");
        }

        AutomataTask newAutomataTask;
        switch (automata_type)
        {
            case TURING_MACHINE:
//                newAutomataTask = new TuringMachineTask(
//                        taskName,
//                        taskDescription,
//                        maxSteps,
//                        "",
//                        deterministic.NONDETERMINISTIC,
//                );
//                return newAutomataTask;
            case UNKNOWN:

                break;
            case FINITE_AUTOMATA:
//                newAutomataTask = new FiniteAutomataTask();
//                return newAutomataTask;
            case LINEAR_BOUNDED_AUTOMATA:
//                newAutomataTask = new LinearBoundedAutomataTask();
//                return newAutomataTask;
            case PUSHDOWN_AUTOMATA:
//                newAutomataTask = new PushdownAutomataTask();
//                return newAutomataTask;
            default:
                return null;
        }

        return null;
    }
}
