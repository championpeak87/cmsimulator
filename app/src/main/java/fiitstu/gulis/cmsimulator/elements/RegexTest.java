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

    private static final int QUANTIFIERS_DEPTH = 10;

    // groups
    private static final String GROUP_START = "(";
    private static final String GROUP_END = ")";

    // escape char
    private static final String ESCAPE_CHARACTER = "\\";

    private RegexTest() {
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
        List<String> outputTests = new ArrayList<>();
        outputTests.add(input);
        while (containsHandleNoneOrMore(outputTests))
            outputTests = handleNoneOrMore(outputTests);
        return null;
    }

    private boolean containsHandleNoneOrMore(List<String> input) {

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
                    String replacement_string;
                    String replication_pattern = null;
                    if (i == 0) {
                        replacement_string = NONE_OR_MORE;
                        // TODO: return list of string without *
                    } else {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(input_char[i - 1]);
                        stringBuilder.append(NONE_OR_MORE);

                        replication_pattern = String.valueOf(input_char[i - 1]);
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

    private String repeatString(String input, int count) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            stringBuilder.append(input);
        }

        return stringBuilder.toString();
    }


}
