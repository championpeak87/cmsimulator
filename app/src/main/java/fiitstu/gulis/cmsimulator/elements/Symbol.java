package fiitstu.gulis.cmsimulator.elements;

import android.support.v4.util.LongSparseArray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An alphabet symbol
 *
 * Created by Martin on 7. 3. 2017.
 */
public class Symbol implements Serializable {

    public static final int EMPTY = 0b1;
    public static final int LEFT_BOUND = 0b10;
    public static final int RIGHT_BOUND = 0b100;
    public static final int STACK_BOTTOM = LEFT_BOUND; //stack has no left and right, so we can reuse

    // GAME SYMBOLS ID
    public static final int MOVEMENT_UP_ID = 0b1;
    public static final int MOVEMENT_DOWN_ID = 0b10;
    public static final int MOVEMENT_LEFT_ID = 0b100;
    public static final int MOVEMENT_RIGHT_ID = 0b1000;

    // GAME SYMBOLS VALUES
    public static final String MOVEMENT_UP = "↑";
    public static final String MOVEMENT_DOWN = "↓";
    public static final String MOVEMENT_LEFT = "←";
    public static final String MOVEMENT_RIGHT = "→";

    private long id;
    private String value;
    private int properties;

    /**
     * Parses a list of symbols into a string word
     * @param list the list of symbols to be parsed
     * @return a word represented by the list of symbols
     */
    public static String listToWord(List<Symbol> list) {
        if (list == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (Symbol symbol: list) {
            stringBuilder.append(symbol.getValue());
        }

        return stringBuilder.toString();
    }

    /**
     * Serializes a list of symbols into a String
     * @param list the list of symbols to be parsed
     * @return a serialized String representation of the list
     */
    public static String serializeList(List<Symbol> list) {
        if (list == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (Symbol symbol: list) {
            stringBuilder.append(symbol.getId());
            stringBuilder.append(' ');
        }

        return stringBuilder.toString();
    }

    /**
     * Deserializes String returned by serializeList back into list of symbols
     * @param string the String to deserialize
     * @param symbolMap map that maps IDs to symbols
     * @return the deserialized list of symbol
     */
    public static List<Symbol> deserializeList(String string, LongSparseArray<Symbol> symbolMap) {
        if (string == null) {
            return null;
        }

        if (string.equals("")) {
            return new ArrayList<>();
        }

        List<Symbol> result = new ArrayList<>();
        String[] tokens = string.split("\\s+");
        for (int i = 0; i < tokens.length; i++) {
            result.add(symbolMap.get(Long.parseLong(tokens[i])));
        }

        return result;
    }


    public static List<Symbol> stringIntoSymbolList(String string, LongSparseArray<Symbol> symbolMap) {
        if (string == null) {
            return null;
        }

        if (string.equals("")) {
            return new ArrayList<>();
        }

        List<Symbol> result = new ArrayList<>();
        char[] tokens = string.toCharArray();

        for (int i = 0; i < tokens.length; i++) {
            result.add(getSymbolFromMap(symbolMap, tokens[i]));
        }

        return result;
    }

    private static Symbol getSymbolFromMap(LongSparseArray<Symbol> symbolMap, int input)
    {
        for (int i = 0; i < symbolMap.size(); i++)
        {
            Symbol symbol = symbolMap.valueAt(i);
            if (symbol.getId() == Integer.parseInt(String.valueOf(input)))
                return symbol;
        }
        return null;
    }


    private static Symbol getSymbolFromMap(LongSparseArray<Symbol> symbolMap, char input)
    {
        for (int i = 0; i < symbolMap.size(); i++)
        {
            Symbol symbol = symbolMap.valueAt(i);
            if (symbol.getValue().equals(String.valueOf(input)))
                return symbol;
        }
        return null;
    }

    public static List<Symbol> extractFromTape(List<TapeElement> list) {
        List<Symbol> result = new ArrayList<>();
        for (TapeElement tapeElement: list) {
            result.add(tapeElement.getSymbol());
        }

        return result;
    }

    /**
     * Removes all symbols that have at least one of the given properties from the list
     * @param list the list of symbols to be filtered
     * @param properties the properties that the removed symbols have
     */
    public static void removeSpecialSymbols(List<Symbol> list, int properties) {
        Iterator<Symbol> iterator = list.iterator();
        while (iterator.hasNext()) {
            if ((iterator.next().getProperties() & properties) != 0) {
                iterator.remove();
            }
        }
    }

    public Symbol(long id, String value) {
        this(id, value, 0);
    }

    public Symbol(long id, String value, int properties) {
        this.id = id;
        this.value = value;
        this.properties = properties;
    }

    public long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isEmpty() {
        return getProperty(EMPTY);
    }

    public void setEmpty(boolean empty) {
        setProperty(EMPTY, empty);
    }

    public boolean isLeftBound() {
        return getProperty(LEFT_BOUND);
    }

    public void setLeftBound(boolean leftBound) {
        setProperty(LEFT_BOUND, leftBound);
    }

    public boolean isRightBound() {
        return getProperty(RIGHT_BOUND);
    }

    public void setRightBound(boolean rightBound) {
        setProperty(RIGHT_BOUND, rightBound);
    }

    public boolean isStackBotom() {
        return getProperty(STACK_BOTTOM);
    }

    public void setStackBottom(boolean stackBottom) {
        setProperty(STACK_BOTTOM, stackBottom);
    }

    public int getProperties() {
        return properties;
    }

    public void setProperties(int properties) {
        this.properties = properties;
    }

    private boolean getProperty(int property) {
        return (properties & property) != 0;
    }

    private void setProperty(int property, boolean value) {
        if (value) {
            properties |= property;
        }
        else  {
            properties &= ~property;
        }
    }
}
