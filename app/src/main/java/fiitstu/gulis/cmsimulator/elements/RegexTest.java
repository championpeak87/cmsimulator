package fiitstu.gulis.cmsimulator.elements;

import android.provider.ContactsContract;
import android.util.Log;
import fiitstu.gulis.cmsimulator.database.DataSource;

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

    private static int QUANTIFIERS_DEPTH;

    // groups
    private static final String GROUP_START = "(";
    private static final String GROUP_END = ")";

    // escape char
    private static final String ESCAPE_CHARACTER = "\\";

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

    public boolean containsWrongSymbols(String input) {
        List<Symbol> alphabet = DataSource.getInstance().getInputAlphabetFullExtract();
        for (Symbol symbol : alphabet) {
            final String value = symbol.getValue();
            if (!input.contains(value) && !isSpecialSymbol(value))
                return true;
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
                return true;
        }
        return false;
    }

    public List<String> getListOfParsedStrings(String input) {
        QUANTIFIERS_DEPTH = DataSource.getInstance().getRegexDepth();
        List<String> outputTests = new ArrayList<>();
        outputTests.add(input);
        while (containsNoneOrMore(outputTests))
            outputTests = handleNoneOrMore(outputTests);
        while (containsNoneOrOne(outputTests))
            outputTests = handleNoneOrOne(outputTests);
        while (containsOneOrMore(outputTests))
            outputTests = handleOneOrMore(outputTests);

        return outputTests;
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
