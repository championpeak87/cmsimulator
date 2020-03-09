package fiitstu.gulis.cmsimulator.elements;

import android.provider.ContactsContract;
import android.util.Log;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.dialogs.NewRegexTestDialog;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RegexTest {
    private static RegexTest instance = null;

    // empty symbol
    private static final String EMPTY_SYMBOL = "ε";

    // quantifiers
    private static final String NONE_OR_MORE = "*";
    private static final String ONE_OR_MORE = "+";
    private static final String NONE_OR_ONE = "?";

    // multiplicators
    private static final String MULTIPLICATOR_START = "{";
    private static final String MULTIPLICATOR_END = "}";

    private static final String LOGICAL_OR = "|";

    private static int QUANTIFIERS_DEPTH;

    // groups
    private static final String GROUP_START = "(";
    private static final String GROUP_END = ")";

    // escape char
    private static final String ESCAPE_CHARACTER = "\\";

    public enum TestVerification {
        GRAMMAR,
        AUTOMATA
    }

    private RegexTest() {
        DataSource dataSource = DataSource.getInstance();
        dataSource.open();
        QUANTIFIERS_DEPTH = dataSource.getRegexDepth();
    }

    public static RegexTest getInstance() {
        if (instance == null)
            instance = new RegexTest();
        return instance;
    }

    private boolean isAlphabetSymbol(String symbol) {
        List<Symbol> alphabet = DataSource.getInstance().getInputAlphabetFullExtract();
        for (Symbol sym : alphabet) {
            final String value = sym.getValue();
            if (symbol.equals(value))
                return true;
        }

        return false;
    }

    public boolean containsWrongSymbols(String input) {
        char[] inputArray = input.toCharArray();
        for (char selectedChar : inputArray) {
            final String selectedCharString = String.valueOf(selectedChar);
            if (!isSpecialSymbol(selectedCharString) && !isAlphabetSymbol(selectedCharString)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSpecialSymbol(String symbol) {
        switch (symbol) {
            case EMPTY_SYMBOL:
            case ESCAPE_CHARACTER:
            case GROUP_END:
            case GROUP_START:
            case NONE_OR_MORE:
            case ONE_OR_MORE:
            case NONE_OR_ONE:
            case MULTIPLICATOR_START:
            case MULTIPLICATOR_END:
            case LOGICAL_OR:
                return true;
        }
        return false;
    }

    public List<String> getListOfParsedStrings(String input) {
        DataSource dataSource = DataSource.getInstance();
        dataSource.open();
        QUANTIFIERS_DEPTH = dataSource.getRegexDepth();
        List<String> outputTests = new ArrayList<>();
        outputTests.add(input);
        while (containsNoneOrMore(outputTests))
            outputTests = handleNoneOrMore(outputTests);
        while (containsNoneOrOne(outputTests))
            outputTests = handleNoneOrOne(outputTests);
        while (containsOneOrMore(outputTests))
            outputTests = handleOneOrMore(outputTests);
        while (containsMultiplicator(outputTests))
            outputTests = handleMultiplicator(outputTests);

        return outputTests;
    }

    private boolean containsMultiplicator(List<String> input) {
        for (String string : input) {
            if (string.contains(MULTIPLICATOR_START) && string.contains(MULTIPLICATOR_END))
                return true;
        }

        return false;
    }

    private boolean containsNoneOrOne(List<String> input) {

        for (String string : input) {
            if (string.contains(NONE_OR_ONE))
                return true;
        }

        return false;
    }

    private boolean containsOneOrMore(List<String> input) {

        for (String string : input) {
            if (string.contains(ONE_OR_MORE))
                return true;
        }

        return false;
    }

    private boolean containsNoneOrMore(List<String> input) {

        for (String string : input) {
            if (string.contains(NONE_OR_MORE))
                return true;
        }

        return false;
    }

    private enum MULTIPLICATOR_TYPE {
        EXACT_COUNT,
        MORE_THAN,
        LESS_THAN,
        IN_BETWEEN
    }

    private MULTIPLICATOR_TYPE getMultiplicatorType(String regex, int multiplicator_position) {
        MULTIPLICATOR_TYPE result = null;
        boolean hasStartNumber = false;
        boolean hasEndNumber = false;
        boolean hasSeparator = false;

        if (regex.charAt(multiplicator_position) == MULTIPLICATOR_START.charAt(0)) {
            int counter = multiplicator_position + 1;
            while (regex.charAt(counter) >= '0' && regex.charAt(counter) <= '9') {
                counter++;
                hasStartNumber = true;
            }
            if (regex.charAt(counter) == ',') {
                hasSeparator = true;
                counter++;
            }
            while (regex.charAt(counter) >= '0' && regex.charAt(counter) <= '9') {
                hasEndNumber = true;
                counter++;
            }
            if (regex.charAt(counter) == MULTIPLICATOR_END.charAt(0)) {
                if (hasStartNumber && !hasSeparator && !hasEndNumber) {
                    result = MULTIPLICATOR_TYPE.EXACT_COUNT;
                } else if (hasStartNumber && hasSeparator && !hasEndNumber) {
                    result = MULTIPLICATOR_TYPE.MORE_THAN;
                } else if (hasStartNumber && hasSeparator && hasEndNumber) {
                    result = MULTIPLICATOR_TYPE.IN_BETWEEN;
                } else if (!hasStartNumber && hasSeparator && hasEndNumber) {
                    result = MULTIPLICATOR_TYPE.LESS_THAN;
                }
            }
        }

        return result;
    }

    private int getExactMultiplicatorCount(String regex, int multiplicator_position) {
        int exact_count = 0;
        int counter = multiplicator_position + 1;
        while (regex.charAt(counter) >= '0' && regex.charAt(counter) <= '9') {
            exact_count = exact_count * 10 + Integer.parseInt(String.valueOf(regex.charAt(counter)));
            counter++;
        }
        if (regex.charAt(counter) == MULTIPLICATOR_END.charAt(0))
            return exact_count;
        else return -1;
    }

    private int[] getInBetweenMultiplicatorCount(String regex, int multiplicator_position) {
        int max_count = 0;
        int min_count = 0;
        int counter = multiplicator_position + 1;

        while (regex.charAt(counter) >= '0' && regex.charAt(counter) <= '9') {
            min_count = min_count * 10 + Integer.parseInt(String.valueOf(regex.charAt(counter)));
            counter++;
        }

        if (regex.charAt(counter) == ',')
            counter++;
        else return null;

        while (regex.charAt(counter) >= '0' && regex.charAt(counter) <= '9') {
            max_count = max_count * 10 + Integer.parseInt(String.valueOf(regex.charAt(counter)));
            counter++;
        }
        if (regex.charAt(counter) == MULTIPLICATOR_END.charAt(0)) {
            int result[] = new int[2];
            result[0] = min_count;
            result[1] = max_count;
            return result;
        } else return null;
    }

    private int getMaxMultiplicatorCount(String regex, int multiplicator_position) {
        int max_count = 0;
        int counter = multiplicator_position + 1;
        if (regex.charAt(counter) == ',')
            counter++;
        else return -1;
        while (regex.charAt(counter) >= '0' && regex.charAt(counter) <= '9') {
            max_count = max_count * 10 + Integer.parseInt(String.valueOf(regex.charAt(counter)));
            counter++;
        }
        if (regex.charAt(counter) == MULTIPLICATOR_END.charAt(0))
            return max_count;
        else return -1;
    }

    private int getMinMultiplicatorCount(String regex, int multiplicator_position) {
        int min_count = 0;
        int counter = multiplicator_position + 1;
        while (regex.charAt(counter) >= '0' && regex.charAt(counter) <= '9') {
            min_count = min_count * 10 + Integer.parseInt(String.valueOf(regex.charAt(counter)));
            counter++;
        }
        if (regex.charAt(counter) == ',')
            counter++;
        if (regex.charAt(counter) == MULTIPLICATOR_END.charAt(0))
            return min_count;
        else return -1;
    }

    private List<String> handleMultiplicator(List<String> input) {
        final char CHAR_MULTIPLICATOR_START = MULTIPLICATOR_START.charAt(0);
        final char CHAR_MULTIPLICATOR_END = MULTIPLICATOR_END.charAt(0);
        List<String> regex_strings = new ArrayList<String>();

        for (String _input : input) {
            char[] input_char = _input.toCharArray();
            int input_char_size = _input.length();

            for (int i = 0; i < input_char_size; i++) {
                if (input_char[i] == CHAR_MULTIPLICATOR_START) {

                    String replacement_string = null;
                    String replication_pattern = null;

                    MULTIPLICATOR_TYPE type = getMultiplicatorType(_input, i);
                    int min_repeat_count = -1;
                    int max_repeat_count = -1;
                    int exact_repeat_count = -1;
                    switch (type) {
                        case EXACT_COUNT:
                            exact_repeat_count = getExactMultiplicatorCount(_input, i);
                            break;
                        case MORE_THAN:
                            min_repeat_count = getMinMultiplicatorCount(_input, i);
                            break;
                        case LESS_THAN:
                            max_repeat_count = getMaxMultiplicatorCount(_input, i);
                            break;
                        case IN_BETWEEN:
                            final int[] in_between_array = getInBetweenMultiplicatorCount(_input, i);
                            min_repeat_count = in_between_array[0];
                            max_repeat_count = in_between_array[1];
                            break;
                    }
                    boolean contains_group = false;
                    if (i == 0) {
                        replacement_string = NONE_OR_MORE;
                        replication_pattern = EMPTY_SYMBOL;

                        regex_strings.add(_input.replace(replacement_string, replication_pattern));
                    } else {
                        StringBuilder stringBuilder = new StringBuilder();
                        if (input_char[i - 1] == GROUP_END.charAt(0)) {
                            contains_group = true;
                            for (int j = i - 2; j >= 0; j--) {
                                if (input_char[j] == GROUP_START.charAt(0)) {
                                    replication_pattern = _input.subSequence(j + 1, i - 1).toString();
                                    stringBuilder.append(GROUP_START);
                                    stringBuilder.append(replication_pattern);
                                    stringBuilder.append(GROUP_END);
                                    break;
                                }
                            }
                        }
                        if (!contains_group)
                            stringBuilder.append(input_char[i - 1]);
                        switch (type) {
                            case EXACT_COUNT:
                                stringBuilder.append(String.format("{%d}", exact_repeat_count));
                                break;
                            case MORE_THAN:
                                stringBuilder.append(String.format("{%d,}", min_repeat_count));
                                break;
                            case LESS_THAN:
                                stringBuilder.append(String.format("{,%d}", max_repeat_count));
                                break;
                            case IN_BETWEEN:
                                stringBuilder.append(String.format("{%d,%d}", min_repeat_count, max_repeat_count));
                                break;
                        }

                        if (!contains_group) {
                            replication_pattern = String.valueOf(input_char[i - 1]);
                        }
                        replacement_string = stringBuilder.toString();

                        String replacementString;
                        switch (type) {
                            case EXACT_COUNT:
                                replacementString = repeatString(replication_pattern, exact_repeat_count);
                                regex_strings.add(_input.replaceFirst(Pattern.quote(replacement_string), replacementString));
                                break;
                            case MORE_THAN:
                                for (int j = min_repeat_count; j < QUANTIFIERS_DEPTH; j++) {
                                    replacementString = repeatString(replication_pattern, j);
                                    regex_strings.add(_input.replaceFirst(Pattern.quote(replacement_string), replacementString));
                                }
                                break;
                            case LESS_THAN:
                                for (int j = 1; j < max_repeat_count; j++) {
                                    replacementString = repeatString(replication_pattern, j);
                                    regex_strings.add(_input.replaceFirst(Pattern.quote(replacement_string), replacementString));
                                }
                                break;
                            case IN_BETWEEN:
                                for (int j = min_repeat_count; j < max_repeat_count; j++) {
                                    replacementString = repeatString(replication_pattern, j);
                                    regex_strings.add(_input.replaceFirst(Pattern.quote(replacement_string), replacementString));
                                }
                                break;
                        }
                    }

                    break;
                }
            }
        }

        return regex_strings;
    }

    private List<String> handleNoneOrMore(List<String> input) {
        final char CHAR_NONE_OR_MORE = NONE_OR_MORE.charAt(0);
        List<String> regex_strings = new ArrayList<String>();

        for (String _input : input) {
            char[] input_char = _input.toCharArray();
            int input_char_size = _input.length();

            for (int i = 0; i < input_char_size; i++) {
                if (input_char[i] == CHAR_NONE_OR_MORE) {
                    String replacement_string = null;
                    String replication_pattern = null;
                    boolean contains_group = false;
                    if (i == 0) {
                        replacement_string = NONE_OR_MORE;
                        replication_pattern = EMPTY_SYMBOL;

                        regex_strings.add(_input.replace(replacement_string, replication_pattern));
                    } else {
                        StringBuilder stringBuilder = new StringBuilder();
                        if (input_char[i - 1] == GROUP_END.charAt(0)) {
                            contains_group = true;
                            for (int j = i - 2; j >= 0; j--) {
                                if (input_char[j] == GROUP_START.charAt(0)) {
                                    replication_pattern = _input.subSequence(j + 1, i - 1).toString();
                                    stringBuilder.append(GROUP_START);
                                    stringBuilder.append(replication_pattern);
                                    stringBuilder.append(GROUP_END);
                                    break;
                                }
                            }
                        }
                        if (!contains_group)
                            stringBuilder.append(input_char[i - 1]);
                        stringBuilder.append(NONE_OR_MORE);

                        if (!contains_group) {
                            replication_pattern = String.valueOf(input_char[i - 1]);
                        }
                        replacement_string = stringBuilder.toString();

                        for (int j = 0; j < QUANTIFIERS_DEPTH; j++) {
                            String replacementString = repeatString(replication_pattern, j);
                            regex_strings.add(_input.replaceFirst(Pattern.quote(replacement_string), replacementString));
                        }
                    }

                    break;
                }
            }
        }

        return regex_strings;
    }

    private List<String> handleOneOrMore(List<String> input) {
        final char CHAR_ONE_OR_MORE = ONE_OR_MORE.charAt(0);
        List<String> regex_strings = new ArrayList<String>();

        for (String _input : input) {
            char[] input_char = _input.toCharArray();
            int input_char_size = _input.length();

            for (int i = 0; i < input_char_size; i++) {
                if (input_char[i] == CHAR_ONE_OR_MORE) {
                    String replacement_string;
                    String replication_pattern = null;
                    boolean contains_group = false;
                    if (i == 0) {
                        replacement_string = ONE_OR_MORE;
                        replication_pattern = EMPTY_SYMBOL;

                        regex_strings.add(_input.replace(replacement_string, replication_pattern));
                    } else {
                        StringBuilder stringBuilder = new StringBuilder();
                        if (input_char[i - 1] == GROUP_END.charAt(0)) {
                            contains_group = true;
                            for (int j = i - 2; j >= 0; j--) {
                                if (input_char[j] == GROUP_START.charAt(0)) {
                                    replication_pattern = _input.subSequence(j + 1, i - 1).toString();
                                    stringBuilder.append(GROUP_START);
                                    stringBuilder.append(replication_pattern);
                                    stringBuilder.append(GROUP_END);
                                    break;
                                }
                            }
                        }
                        if (!contains_group)
                            stringBuilder.append(input_char[i - 1]);
                        stringBuilder.append(ONE_OR_MORE);

                        if (!contains_group)
                            replication_pattern = String.valueOf(input_char[i - 1]);
                        replacement_string = stringBuilder.toString();

                        for (int j = 1; j < QUANTIFIERS_DEPTH; j++) {
                            String replacementString = repeatString(replication_pattern, j);
                            regex_strings.add(_input.replaceFirst(Pattern.quote(replacement_string), replacementString));
                        }
                    }

                    break;
                }
            }
        }

        return regex_strings;
    }

    private List<String> handleNoneOrOne(List<String> input) {
        final char CHAR_NONE_OR_ONE = NONE_OR_ONE.charAt(0);
        List<String> regex_strings = new ArrayList<String>();

        for (String _input : input) {
            char[] input_char = _input.toCharArray();
            int input_char_size = _input.length();

            for (int i = 0; i < input_char_size; i++) {
                if (input_char[i] == CHAR_NONE_OR_ONE) {
                    String replacement_string;
                    String replication_pattern = null;
                    boolean contains_group = false;
                    if (i == 0) {
                        replacement_string = NONE_OR_ONE;
                        replication_pattern = EMPTY_SYMBOL;

                        regex_strings.add(_input.replace(replacement_string, replication_pattern));
                    } else {
                        StringBuilder stringBuilder = new StringBuilder();
                        if (input_char[i - 1] == GROUP_END.charAt(0)) {
                            contains_group = true;
                            for (int j = i - 2; j >= 0; j--) {
                                if (input_char[j] == GROUP_START.charAt(0)) {
                                    replication_pattern = _input.subSequence(j + 1, i - 1).toString();
                                    stringBuilder.append(GROUP_START);
                                    stringBuilder.append(replication_pattern);
                                    stringBuilder.append(GROUP_END);
                                    break;
                                }
                            }
                        }

                        if (!contains_group)
                            stringBuilder.append(input_char[i - 1]);
                        stringBuilder.append(NONE_OR_ONE);

                        if (!contains_group)
                            replication_pattern = String.valueOf(input_char[i - 1]);
                        replacement_string = stringBuilder.toString();

                        for (int j = 0; j <= 1; j++) {
                            String replacementString = repeatString(replication_pattern, j);
                            regex_strings.add(_input.replaceFirst(Pattern.quote(replacement_string), replacementString));
                        }
                    }

                    break;
                }
            }
        }

        return regex_strings;
    }

    private String repeatString(String input, int count) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            stringBuilder.append(input);
        }

        return stringBuilder.toString();
    }


}
