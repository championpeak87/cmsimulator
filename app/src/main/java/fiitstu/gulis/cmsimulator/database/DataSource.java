package fiitstu.gulis.cmsimulator.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import java.nio.DoubleBuffer;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import fiitstu.gulis.cmsimulator.app.CMSimulator;
import fiitstu.gulis.cmsimulator.elements.*;

/**
 * Class that handles interactions with the database, allows storing and retrieving of all entities.
 * <p>
 * Created by Martin on 7. 3. 2017.
 */
public class DataSource {

    //log tag
    private static final String TAG = DataSource.class.getName();

    private static DbOpenHelper dbHelper;
    private SQLiteDatabase database;

    //singleton initialization
    private DataSource() {
    }

    private static DataSource instance = null;

    public static synchronized DataSource getInstance() {
        if (instance == null) {
            instance = new DataSource();
            Log.i(TAG, "new instance created");
        }
        dbHelper = DbOpenHelper.getInstance(CMSimulator.getContext());

        Log.i(TAG, "instance returned");
        return instance;
    }

    /**
     * Takes a formatted string representation of a list of symbol IDs and a LongSparseArray that
     * maps the IDs to symbols, returns an ArrayList of the symbols
     *
     * @param string           a list of IDs of stack symbols represented by a formatted String
     * @param stackAlphabetMap a LongSparseArray that maps the IDs to Symbols
     * @return an ArrayList of the stack symbols
     */
    private static ArrayList<Symbol> parsePDASymbolList(String string, LongSparseArray<Symbol> stackAlphabetMap) {
        ArrayList<Symbol> result = new ArrayList<>();

        StringBuilder numberString = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '<') {
                numberString = new StringBuilder();
            } else if (string.charAt(i) == '>') {
                long number = Long.parseLong(numberString.toString());
                result.add(stackAlphabetMap.get(number));
            } else {
                numberString.append(string.charAt(i));
            }
        }

        return result;
    }

    /**
     * Opens connection with the database. If a connection is already open, does nothing
     */
    public synchronized void open() {
        if (database == null) {
            Log.v(TAG, "trying to open database connection");
            try {
                database = dbHelper.getWritableDatabase();
                Log.i(TAG, "database connection opened");
            } catch (Exception e) {
                Log.e(TAG, "error while opening database", e);
            }
        } else {
            Log.w(TAG, "tried to re-open the database");
        }
    }

    /**
     * Closes connection with the database.
     */
    public synchronized void close() {
        Log.v(TAG, "trying to close database connection");
        try {
            dbHelper.close();
            database = null;
            Log.i(TAG, "database connection closed");
        } catch (Exception e) {
            Log.e(TAG, "error while closing database");
        }
    }

    public Symbol getInputSymbolWithProperties(int properties) {
        Log.v(TAG, "getEmptySymbol method started");
        Symbol symbol = null;
        Cursor cursor = null;
        try {
            cursor = database.query(DbOpenHelper.TABLE_INPUT_ALPHABET,
                    new String[]{DbOpenHelper.COLUMN_INPUT_SYMBOL_ID, DbOpenHelper.COLUMN_INPUT_SYMBOL_VALUE,
                            DbOpenHelper.COLUMN_INPUT_SYMBOL_PROPERTIES},
                    "( " + DbOpenHelper.COLUMN_INPUT_SYMBOL_PROPERTIES + " & " + properties + " ) = " + properties,
                    null, null, null, null);
            Log.v(TAG, "cursor initialized -> " + cursor.getCount() + " rows returned");

            if (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_INPUT_SYMBOL_ID));
                String value = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_INPUT_SYMBOL_VALUE));
                int allProperties = cursor.getInt(cursor.getColumnIndex(DbOpenHelper.COLUMN_INPUT_SYMBOL_PROPERTIES));

                symbol = new Symbol(id, value, allProperties);
            }
            Log.i(TAG, "empty symbol returned");
        } catch (Exception e) {
            Log.e(TAG, "error while getting empty symol", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return symbol;
    }

    public Symbol getStackSymbolWithProperties(int properties) {
        Log.v(TAG, "getStartStackSymbol method started");
        Symbol symbol = null;
        Cursor cursor = null;
        try {
            cursor = database.query(DbOpenHelper.TABLE_STACK_ALPHABET,
                    new String[]{DbOpenHelper.COLUMN_STACK_SYMBOL_ID, DbOpenHelper.COLUMN_STACK_SYMBOL_VALUE,
                            DbOpenHelper.COLUMN_STACK_SYMBOL_PROPERTIES},
                    "( " + DbOpenHelper.COLUMN_STACK_SYMBOL_PROPERTIES + " & " + properties + " ) = " + properties,
                    null, null, null, null);
            Log.v(TAG, "cursor initialized -> " + cursor.getCount() + " rows returned");

            if (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_STACK_SYMBOL_ID));
                String value = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_STACK_SYMBOL_VALUE));
                int allProperties = cursor.getInt(cursor.getColumnIndex(DbOpenHelper.COLUMN_STACK_SYMBOL_PROPERTIES));

                symbol = new Symbol(id, value, allProperties);
            }
            Log.i(TAG, "empty symbol returned");
        } catch (Exception e) {
            Log.e(TAG, "error while getting empty symol", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return symbol;
    }

    //method to add input symbol into database, throws exception if not successful
    public Symbol addInputSymbol(String value, int properties) {
        Log.v(TAG, "addInputSymbol method started");
        Symbol symbol;
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_INPUT_SYMBOL_VALUE, value);
        contentValues.put(DbOpenHelper.COLUMN_INPUT_SYMBOL_PROPERTIES, properties);
        Log.v(TAG, "contentValues prepared");


        long id = 0;
        try {
            id = database.insertOrThrow(DbOpenHelper.TABLE_INPUT_ALPHABET, null, contentValues);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        symbol = new Symbol(id, value, properties);
        Log.i(TAG, "input symbol '" + symbol.getValue() + "' added into database");
        return symbol;
    }

    //method to update input symbol in database, throws exception if not successful
    public void updateInputSymbol(Symbol symbol, String value) {
        Log.v(TAG, "updateInputSymbol method started");
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_INPUT_SYMBOL_VALUE, value);

        Log.v(TAG, "contentValues prepared");

        database.update(DbOpenHelper.TABLE_INPUT_ALPHABET, contentValues, DbOpenHelper.COLUMN_INPUT_SYMBOL_ID + " = ?", new String[]{String.valueOf(symbol.getId())});
        symbol.setValue(value);

        Log.i(TAG, "input symbol '" + symbol.getValue() + "' updated in database");
    }

    //method to delete input symbol from database, throws exception if not successful
    public void deleteInputSymbol(Symbol symbol, Symbol emptySymbol, List<TapeElement> tapeElementList) {
        Log.v(TAG, "deleteInputSymbol method started");
        //if symbols are in tape, update them
        for (TapeElement tapeElement : tapeElementList) {
            if (tapeElement.getSymbol().getId() == symbol.getId()) {
                updateTapeElement(tapeElement, emptySymbol, tapeElement.getOrder());
            }
        }
        database.delete(DbOpenHelper.TABLE_INPUT_ALPHABET, DbOpenHelper.COLUMN_INPUT_SYMBOL_ID + " = ?", new String[]{String.valueOf(symbol.getId())});
        Log.i(TAG, "input symbol '" + symbol.getValue() + "' deleted from database");
    }

    //method to add stack symbol into database, throws exception if not successful
    public Symbol addStackSymbol(String value, int properties) {
        Log.v(TAG, "addStackSymbol method started");

        Symbol symbol;
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_STACK_SYMBOL_VALUE, value);
        contentValues.put(DbOpenHelper.COLUMN_STACK_SYMBOL_PROPERTIES, properties);
        Log.v(TAG, "contentValues prepared");

        long id = database.insertOrThrow(DbOpenHelper.TABLE_STACK_ALPHABET, null, contentValues);
        symbol = new Symbol(id, value, properties);
        Log.i(TAG, "stack symbol '" + symbol.getValue() + "' added into database");
        return symbol;
    }

    //method to update stack symbol in database, throws exception if not successful
    public void updateStackSymbol(Symbol symbol, String value) {
        Log.v(TAG, "updateStackSymbol method started");

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_STACK_SYMBOL_VALUE, value);
        Log.v(TAG, "contentValues prepared");

        database.update(DbOpenHelper.TABLE_STACK_ALPHABET, contentValues, DbOpenHelper.COLUMN_STACK_SYMBOL_ID + " = ?", new String[]{String.valueOf(symbol.getId())});
        symbol.setValue(value);
        Log.i(TAG, "stack symbol '" + symbol.getValue() + "' updated in database");
    }

    //method to delete stack symbol from database, throws exception if not successful
    public void deleteStackSymbol(Symbol symbol) {
        Log.v(TAG, "deleteStackSymbol method started");
        Cursor cursor = null;
        try {
            //check if used in any transition
            cursor = database.query(DbOpenHelper.TABLE_TRANSITION,
                    new String[]{DbOpenHelper.COLUMN_TRANSITION_POP, DbOpenHelper.COLUMN_TRANSITION_PUSH},
                    null, null, null, null, null);
            Log.v(TAG, "cursor initialized -> " + cursor.getCount() + " rows returned");
            if (cursor.getCount() > 0) {
                //prepare search string
                String symbolString = "<" + symbol.getId() + ">";
                //search for symbol
                while (cursor.moveToNext()) {
                    String popString = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_TRANSITION_POP));
                    if (popString.contains(symbolString)) {
                        throw new SQLiteConstraintException();
                    }
                    String pushString = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_TRANSITION_PUSH));
                    if (pushString.contains(symbolString)) {
                        throw new SQLiteConstraintException();
                    }
                }
            }

            database.delete(DbOpenHelper.TABLE_STACK_ALPHABET, DbOpenHelper.COLUMN_STACK_SYMBOL_ID + " = ?", new String[]{String.valueOf(symbol.getId())});
            Log.i(TAG, "stack symbol '" + symbol.getValue() + "' deleted from database");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //method to add state into database, throws exception if not successful
    public State addState(String value, int positionX, int positionY, boolean initialState, boolean finalState) {
        Log.v(TAG, "addState method started");
        State state;
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_STATE_VALUE, value);
        contentValues.put(DbOpenHelper.COLUMN_STATE_POS_X, positionX);
        contentValues.put(DbOpenHelper.COLUMN_STATE_POS_Y, positionY);
        contentValues.put(DbOpenHelper.COLUMN_STATE_INITIAL, initialState ? 1 : 0);
        contentValues.put(DbOpenHelper.COLUMN_STATE_FINAL, finalState ? 1 : 0);
        Log.v(TAG, "contentValues prepared");

        long id = database.insertOrThrow(DbOpenHelper.TABLE_STATE, null, contentValues);
        state = new State(id, value, positionX, positionY, initialState, finalState);
        Log.i(TAG, "state '" + state.getValue() + ", " + state.getPositionX() + ", " + state.getPositionY() + ", "
                + state.isInitialState() + ", " + state.isFinalState() + "' added into database");
        return state;
    }

    //method to update state in database, throws exception if not successful
    public void updateState(State state, String value, int positionX, int positionY, boolean initialState, boolean finalState) {
        Log.v(TAG, "updateState method started");
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_STATE_VALUE, value);
        contentValues.put(DbOpenHelper.COLUMN_STATE_POS_X, positionX);
        contentValues.put(DbOpenHelper.COLUMN_STATE_POS_Y, positionY);
        contentValues.put(DbOpenHelper.COLUMN_STATE_INITIAL, initialState ? 1 : 0);
        contentValues.put(DbOpenHelper.COLUMN_STATE_FINAL, finalState ? 1 : 0);
        Log.v(TAG, "contentValues prepared");

        database.update(DbOpenHelper.TABLE_STATE, contentValues, DbOpenHelper.COLUMN_STATE_ID + " = ?", new String[]{String.valueOf(state.getId())});
        state.setValue(value);
        state.setPositionX(positionX);
        state.setPositionY(positionY);
        state.setInitialState(initialState);
        state.setFinalState(finalState);
        Log.i(TAG, "state '" + state.getValue() + ", " + state.getPositionX() + ", " + state.getPositionY() + ", "
                + state.isInitialState() + ", " + state.isFinalState() + "' updated in database");
    }

    //method to delete state from database, throws exception if not successful
    public void deleteState(State state) {
        Log.v(TAG, "deleteState method started");
        database.delete(DbOpenHelper.TABLE_STATE, DbOpenHelper.COLUMN_STATE_ID + " = ?", new String[]{String.valueOf(state.getId())});
        Log.i(TAG, "state '" + state.getValue() + ", " + state.getPositionX() + ", " + state.getPositionY() + ", "
                + state.isInitialState() + ", " + state.isFinalState() + "' deleted from database");
    }

    //method to delete all states from database
    public void dropStates() {
        Log.v(TAG, "dropStates method started");
        database.delete(DbOpenHelper.TABLE_STATE, null, null);
        Log.i(TAG, "all states deleted from database");
    }

    //method to delete all transitions from database
    public void dropTransitions() {
        Log.v(TAG, "dropStates method started");
        database.delete(DbOpenHelper.TABLE_TRANSITION, null, null);
        Log.i(TAG, "all transitions deleted from database");
    }

    //method to add fsa transition into database, throws exception if not successful
    public Transition addFsaTransition(State fromState, Symbol readSymbol, State toState, long emptySymbolId) {
        Log.v(TAG, "addFsaTransition method started");
        Transition transition;
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_FROM_STATE, fromState.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_TO_STATE, toState.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_READ_SYMBOL, readSymbol.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_WRITE_SYMBOL, emptySymbolId); //because of unique handling
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_DIRECTION, -1); //because of unique handling
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_POP, -1); //because of unique handling
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_PUSH, -1); //because of unique handling
        Log.v(TAG, "contentValues prepared");

        long id = database.insertOrThrow(DbOpenHelper.TABLE_TRANSITION, null, contentValues);
        transition = new FsaTransition(id, fromState, readSymbol, toState);
        Log.i(TAG, "fsaTransition '" + transition.getDesc() + "' added into database");
        return transition;
    }

    //method to update fsa transaction in database, throws exception if not successful
    public void updateFsaTransition(FsaTransition transition, State fromState, Symbol readSymbol, State toState, long emptySymbolId) {
        Log.v(TAG, "updateFsaTransition method started");
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_FROM_STATE, fromState.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_TO_STATE, toState.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_READ_SYMBOL, readSymbol.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_WRITE_SYMBOL, emptySymbolId);
        Log.v(TAG, "contentValues prepared");

        database.update(DbOpenHelper.TABLE_TRANSITION, contentValues, DbOpenHelper.COLUMN_TRANSITION_ID + " = ?", new String[]{String.valueOf(transition.getId())});
        transition.setFromState(fromState);
        transition.setReadSymbol(readSymbol);
        transition.setToState(toState);
        Log.i(TAG, "fsaTransition '" + transition.getDesc() + "' updated in database");
    }

    /**
     * Adds PDA transition into database, throws exception if not successful
     *
     * @param fromState      the state the machine is in when the transition happens
     * @param readSymbol     the symbol that is read when the transition happens
     * @param toState        the state the machine will be in after the transition
     * @param emptySymbolId  the ID of the symbol that is being used as empty symbol
     * @param popSymbolList  list of symbols that are popped from the stack when the transition happens
     * @param pushSymbolList list of symbols that are pushed onto the stack after teh transition
     * @return the inserted Transition
     * @throws android.database.SQLException
     */
    public Transition addPdaTransition(State fromState, Symbol readSymbol, State toState, long emptySymbolId,
                                       List<Symbol> popSymbolList, List<Symbol> pushSymbolList)
            throws android.database.SQLException {
        Log.v(TAG, "addPdaTransition method started");
        Transition transition;
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_FROM_STATE, fromState.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_TO_STATE, toState.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_READ_SYMBOL, readSymbol.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_WRITE_SYMBOL, emptySymbolId); //because of unique handling
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_DIRECTION, -1); //because of unique handling
        //create pop string
        StringBuilder popString = new StringBuilder();
        for (Symbol symbol : popSymbolList) {
            popString.append("<").append(symbol.getId()).append(">");
        }
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_POP, popString.toString());
        //create push string
        StringBuilder pushString = new StringBuilder();
        for (Symbol symbol : pushSymbolList) {
            pushString.append("<").append(symbol.getId()).append(">");
        }
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_PUSH, pushString.toString());
        Log.v(TAG, "transition contentValues prepared");

        long id = database.insertOrThrow(DbOpenHelper.TABLE_TRANSITION, null, contentValues);
        transition = new PdaTransition(id, fromState, readSymbol, toState, popSymbolList, pushSymbolList);
        Log.i(TAG, "pdaTransition '" + transition.getDesc() + "' added into database");
        return transition;
    }

    //method to update pda transition in database, throws exception if not successful
    public void updatePdaTransition(PdaTransition transition, State fromState, Symbol readSymbol,
                                    State toState, long emptySymbolId,
                                    List<Symbol> popSymbolList, List<Symbol> pushSymbolList) {
        Log.v(TAG, "updatePdaTransition method started");
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_FROM_STATE, fromState.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_TO_STATE, toState.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_READ_SYMBOL, readSymbol.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_WRITE_SYMBOL, emptySymbolId);
        //create pop string
        StringBuilder popString = new StringBuilder();
        for (Symbol symbol : popSymbolList) {
            popString.append("<").append(symbol.getId()).append(">");
        }
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_POP, popString.toString());
        //create push string
        StringBuilder pushString = new StringBuilder();
        for (Symbol symbol : pushSymbolList) {
            pushString.append("<").append(symbol.getId()).append(">");
        }
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_PUSH, pushString.toString());
        Log.v(TAG, "transition contentValues prepared");

        database.update(DbOpenHelper.TABLE_TRANSITION, contentValues, DbOpenHelper.COLUMN_TRANSITION_ID + " = ?", new String[]{String.valueOf(transition.getId())});
        transition.setFromState(fromState);
        transition.setReadSymbol(readSymbol);
        transition.setToState(toState);
        transition.setPopSymbolList(popSymbolList);
        transition.setPushSymbolList(pushSymbolList);
        Log.i(TAG, "pdaTransition '" + transition.getDesc() + "' updated in database");
    }

    //method to add tm transition into database, throws exception if not successful
    public Transition addTmTransition(State fromState, Symbol readSymbol, State toState, Symbol writeSymbol, TmTransition.Direction direction) {
        Log.v(TAG, "addTmTransition method started");
        Transition transition;
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_FROM_STATE, fromState.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_TO_STATE, toState.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_READ_SYMBOL, readSymbol.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_WRITE_SYMBOL, writeSymbol.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_DIRECTION, direction == TmTransition.Direction.LEFT ? 1 : 0);
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_POP, -1); //because of unique handling
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_PUSH, -1); //because of unique handling
        Log.v(TAG, "contentValues prepared");

        long id = database.insertOrThrow(DbOpenHelper.TABLE_TRANSITION, null, contentValues);
        transition = new TmTransition(id, fromState, readSymbol, toState, writeSymbol, direction);
        Log.i(TAG, "tmTransition '" + transition.getDesc() + "' added into database");
        return transition;
    }

    //method to update tm transition in database, throws exception if not successful
    public void updateTmTransition(TmTransition transition, State fromState, Symbol readSymbol, State toState, Symbol writeSymbol, TmTransition.Direction direction) {
        Log.v(TAG, "updateTmTransition method started");
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_FROM_STATE, fromState.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_TO_STATE, toState.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_READ_SYMBOL, readSymbol.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_WRITE_SYMBOL, writeSymbol.getId());
        contentValues.put(DbOpenHelper.COLUMN_TRANSITION_DIRECTION, direction == TmTransition.Direction.LEFT ? 1 : 0);
        Log.v(TAG, "contentValues prepared");

        database.update(DbOpenHelper.TABLE_TRANSITION, contentValues, DbOpenHelper.COLUMN_TRANSITION_ID + " = ?", new String[]{String.valueOf(transition.getId())});
        transition.setFromState(fromState);
        transition.setReadSymbol(readSymbol);
        transition.setToState(toState);
        transition.setWriteSymbol(writeSymbol);
        transition.setDirection(direction);
        Log.i(TAG, "tmTransition '" + transition.getDesc() + "' updated in database");
    }

    //method to delete transition from database, throws exception if not successful
    public void deleteTransition(Transition transition) {
        Log.v(TAG, "deleteTransition method started");
        database.delete(DbOpenHelper.TABLE_TRANSITION, DbOpenHelper.COLUMN_TRANSITION_ID + " = ?", new String[]{String.valueOf(transition.getId())});
        Log.i(TAG, "transition '" + transition.getDesc() + "' deleted from database");
    }

    //method to add grammar rule into database, throws exception if not succesful
    public GrammarRule addGrammarRule(String leftRule, String rightRule) {
        Log.v(TAG, "addGrammarRule method started");
        GrammarRule grammarRule;
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_LEFT_RULE, leftRule);
        contentValues.put(DbOpenHelper.COLUMN_RIGHT_RULE, rightRule);

        Log.v(TAG, "contentValues prepared");

        database.insertOrThrow(DbOpenHelper.TABLE_GRAMMAR_RULE, null, contentValues);
        grammarRule = new GrammarRule(leftRule, rightRule);
        Log.i(TAG, "grammar rule '" + grammarRule.getGrammarRight() + ", " + grammarRule.getGrammarLeft() + ", " + "' added into database");
        return grammarRule;
    }

    public void clearGrammarRuleTable() {
        Cursor cursor = null;
        try {
            cursor = database.query(DbOpenHelper.TABLE_GRAMMAR_RULE,
                    new String[]{DbOpenHelper.COLUMN_GRAMMAR_RULE_ID, DbOpenHelper.COLUMN_LEFT_RULE,
                            DbOpenHelper.COLUMN_RIGHT_RULE},
                    null, null, null, null, null);
            if (cursor.getCount() > 0) {
                database.execSQL("DELETE FROM " + DbOpenHelper.TABLE_GRAMMAR_RULE);
            }
        } catch (Exception e) {
            Log.e(TAG, "error while getting content of GRAMMAR RULE table", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //method to get whole content of GRAMMAR RULE table
    public List<GrammarRule> getGrammarRuleFullExtract() {
        Log.v(TAG, "getGrammarRuleFullExtract method started");
        List<GrammarRule> grammarRuleList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(DbOpenHelper.TABLE_GRAMMAR_RULE,
                    new String[]{DbOpenHelper.COLUMN_GRAMMAR_RULE_ID, DbOpenHelper.COLUMN_LEFT_RULE,
                            DbOpenHelper.COLUMN_RIGHT_RULE},
                    null, null, null, null, null);
            Log.v(TAG, "cursor initialized -> " + cursor.getCount() + " rows returned");

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_GRAMMAR_RULE_ID));
                    String leftRule = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_LEFT_RULE));
                    String rightRule = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_RIGHT_RULE));

                    grammarRuleList.add(new GrammarRule(id, leftRule, rightRule));
                }
            }
            Log.i(TAG, "GRAMMAR RULE table content list returned");
        } catch (Exception e) {
            Log.e(TAG, "error while getting content of GRAMMAR RULE table", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return grammarRuleList;
    }

    /**
     * Adds a test scenario into the database or updates it if already stored and returns its primary key.
     * Throws if not successful.
     *
     * @param test the test to be added or updated
     * @return the primary key of the added/updated test
     * @throws android.database.SQLException
     */
    public long addOrUpdateTest(TestScenario test, boolean negative) throws SQLException {
        Log.v(TAG, "addOrUpdateTest method started");
        String table = negative ? DbOpenHelper.TABLE_NEGATIVE_TEST : DbOpenHelper.TABLE_TEST;
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_TEST_INPUT, Symbol.serializeList(test.getInputWord()));
        contentValues.put(DbOpenHelper.COLUMN_TEST_OUTPUT, Symbol.serializeList(test.getOutputWord()));
        Log.v(TAG, "contentValues prepared");

        if (test.getId() == 0) {
            long id = database.insertOrThrow(table, null, contentValues);
            Log.i(TAG, "test '" + test.getInputWord() + ", " + test.getOutputWord()
                    + "' added into database");
            return id;
        } else {
            database.update(table, contentValues, DbOpenHelper.COLUMN_TEST_ID + " = ?", new String[]{String.valueOf(test.getId())});
            Log.i(TAG, "test '" + test.getInputWord() + ", " + test.getOutputWord()
                    + "' updated in database");
            return test.getId();
        }
    }

    /**
     * Deletes a test from the database. Throws if not successful.
     *
     * @param test the test to be deleted
     * @throws android.database.SQLException
     */
    public void deleteTest(TestScenario test, boolean negative) {
        Log.v(TAG, "deleteTest method started");
        String table = negative ? DbOpenHelper.TABLE_NEGATIVE_TEST : DbOpenHelper.TABLE_TEST;
        database.delete(table, DbOpenHelper.COLUMN_TEST_ID + " = ?", new String[]{String.valueOf(test.getId())});
        Log.i(TAG, "test '" + test.getInputWord() + ", " + test.getOutputWord()
                + "' deleted from database");
    }

    /**
     * Returns the entire contents of the TEST table or NEGATIVE_TEST table
     *
     * @param negative if true, contents of NEGATIVE_TEST are returned, if false, TEST
     * @param alphabet list of iput alphabet symbols
     * @return the entire contents of the TEST table or NEGATIVE_TEST table
     */
    public List<TestScenario> getTestFullExtract(boolean negative, List<Symbol> alphabet) {
        Log.v(TAG, "getTestFullExtract method started");
        String table = negative ? DbOpenHelper.TABLE_NEGATIVE_TEST : DbOpenHelper.TABLE_TEST;
        List<TestScenario> testList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(table,
                    new String[]{DbOpenHelper.COLUMN_TEST_ID, DbOpenHelper.COLUMN_TEST_INPUT,
                            DbOpenHelper.COLUMN_TEST_OUTPUT},
                    null, null, null, null, null);
            Log.v(TAG, "cursor initialized -> " + cursor.getCount() + " rows returned");

            //create maps from list
            LongSparseArray<Symbol> inputAlphabetMap = new LongSparseArray<>();
            for (Symbol symbol : alphabet) {
                inputAlphabetMap.put(symbol.getId(), symbol);
            }

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_TEST_ID));
                    String input = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_TEST_INPUT));
                    String output = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_TEST_OUTPUT));

                    testList.add(new TestScenario(id, Symbol.deserializeList(input, inputAlphabetMap), Symbol.deserializeList(output, inputAlphabetMap)));
                }
            }
            Log.i(TAG, "TEST table content list returned");
        } catch (Exception e) {
            Log.e(TAG, "error while getting content of TEST table", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return testList;
    }

    //method to get whole content of INPUT ALPHABET table
    public List<Symbol> getInputAlphabetFullExtract() {
        Log.v(TAG, "getInputAlphabetFullExtract method started");
        List<Symbol> alphabetList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(DbOpenHelper.TABLE_INPUT_ALPHABET,
                    new String[]{DbOpenHelper.COLUMN_INPUT_SYMBOL_ID, DbOpenHelper.COLUMN_INPUT_SYMBOL_VALUE,
                            DbOpenHelper.COLUMN_INPUT_SYMBOL_PROPERTIES},
                    null, null, null, null, null);
            Log.v(TAG, "cursor initialized -> " + cursor.getCount() + " rows returned");

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    Symbol symbol = new Symbol(cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_INPUT_SYMBOL_ID)),
                            cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_INPUT_SYMBOL_VALUE)),
                            cursor.getInt(cursor.getColumnIndex(DbOpenHelper.COLUMN_INPUT_SYMBOL_PROPERTIES)));
                    alphabetList.add(symbol);
                }
            }
            Log.i(TAG, "INPUT ALPHABET table content list returned");
        } catch (Exception e) {
            Log.e(TAG, "error while getting content of INPUT ALPHABET table", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return alphabetList;
    }

    //method to get whole content of STACK ALPHABET table
    public List<Symbol> getStackAlphabetFullExtract() {
        Log.v(TAG, "getStackAlphabetFullExtract method started");
        List<Symbol> alphabetList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(DbOpenHelper.TABLE_STACK_ALPHABET,
                    new String[]{DbOpenHelper.COLUMN_STACK_SYMBOL_ID, DbOpenHelper.COLUMN_STACK_SYMBOL_VALUE,
                            DbOpenHelper.COLUMN_STACK_SYMBOL_PROPERTIES},
                    null, null, null, null, null);
            Log.v(TAG, "cursor initialized -> " + cursor.getCount() + " rows returned");

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    Symbol symbol = new Symbol(cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_STACK_SYMBOL_ID)),
                            cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_STACK_SYMBOL_VALUE)),
                            cursor.getInt(cursor.getColumnIndex(DbOpenHelper.COLUMN_STACK_SYMBOL_PROPERTIES)));
                    alphabetList.add(symbol);
                }
            }
            Log.i(TAG, "STACK ALPHABET table content list returned");
        } catch (Exception e) {
            Log.e(TAG, "error while getting content of STACK ALPHABET table", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return alphabetList;
    }

    //method to get whole content of STATE table
    public List<State> getStateFullExtract() {
        Log.v(TAG, "getStateFullExtract method started");
        List<State> stateList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(DbOpenHelper.TABLE_STATE,
                    new String[]{DbOpenHelper.COLUMN_STATE_ID, DbOpenHelper.COLUMN_STATE_VALUE,
                            DbOpenHelper.COLUMN_STATE_POS_X, DbOpenHelper.COLUMN_STATE_POS_Y,
                            DbOpenHelper.COLUMN_STATE_INITIAL, DbOpenHelper.COLUMN_STATE_FINAL},
                    null, null, null, null, null);
            Log.v(TAG, "cursor initialized -> " + cursor.getCount() + " rows returned");

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_STATE_ID));
                    String value = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_STATE_VALUE));
                    int positionX = cursor.getInt(cursor.getColumnIndex(DbOpenHelper.COLUMN_STATE_POS_X));
                    int positionY = cursor.getInt(cursor.getColumnIndex(DbOpenHelper.COLUMN_STATE_POS_Y));
                    boolean initialState = cursor.getInt(cursor.getColumnIndex(DbOpenHelper.COLUMN_STATE_INITIAL)) == 1;
                    boolean finalState = cursor.getInt(cursor.getColumnIndex(DbOpenHelper.COLUMN_STATE_FINAL)) == 1;

                    stateList.add(new State(id, value, positionX, positionY, initialState, finalState));
                }
            }
            Log.i(TAG, "STATE table content list returned");
        } catch (Exception e) {
            Log.e(TAG, "error while getting content of STATE table", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return stateList;
    }

    //method to get whole content of TRANSITION table for FSA
    public List<Transition> getFsaTransitionFullExtract(List<Symbol> inputAlphabetList, List<State> stateList) {
        Log.v(TAG, "getFsaTransitionFullExtract method started");
        List<Transition> transitionList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(DbOpenHelper.TABLE_TRANSITION,
                    new String[]{DbOpenHelper.COLUMN_TRANSITION_ID, DbOpenHelper.COLUMN_TRANSITION_FROM_STATE,
                            DbOpenHelper.COLUMN_TRANSITION_TO_STATE, DbOpenHelper.COLUMN_TRANSITION_READ_SYMBOL},
                    null, null, null, null, null);
            Log.v(TAG, "cursor initialized -> " + cursor.getCount() + " rows returned");

            if (cursor.getCount() > 0) {
                //create maps from lists
                LongSparseArray<Symbol> inputAlphabetMap = new LongSparseArray<>();
                for (Symbol symbol : inputAlphabetList) {
                    inputAlphabetMap.put(symbol.getId(), symbol);
                }
                LongSparseArray<State> stateMap = new LongSparseArray<>();
                for (State state : stateList) {
                    stateMap.put(state.getId(), state);
                }
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_TRANSITION_ID));
                    State fromState = stateMap.get(cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_TRANSITION_FROM_STATE)));
                    State toState = stateMap.get(cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_TRANSITION_TO_STATE)));

                    Symbol readSymbol = inputAlphabetMap.get(cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_TRANSITION_READ_SYMBOL)));

                    transitionList.add(new FsaTransition(id, fromState, readSymbol, toState));
                }
            }
            Log.i(TAG, "TRANSITION table content list returned");
        } catch (Exception e) {
            Log.e(TAG, "error while getting content of TRANSITION table", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return transitionList;
    }

    //method to get whole content of TRANSITION table for PDA
    public List<Transition> getPdaTransitionFullExtract(List<Symbol> inputAlphabetList, List<Symbol> stackAlphabetList, List<State> stateList) {
        Log.v(TAG, "getPdaTransitionFullExtract method started");
        List<Transition> transitionList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(DbOpenHelper.TABLE_TRANSITION,
                    new String[]{DbOpenHelper.COLUMN_TRANSITION_ID, DbOpenHelper.COLUMN_TRANSITION_FROM_STATE,
                            DbOpenHelper.COLUMN_TRANSITION_TO_STATE, DbOpenHelper.COLUMN_TRANSITION_READ_SYMBOL,
                            DbOpenHelper.COLUMN_TRANSITION_POP, DbOpenHelper.COLUMN_TRANSITION_PUSH},
                    null, null, null, null, null);
            Log.v(TAG, "cursor initialized -> " + cursor.getCount() + " rows returned");

            if (cursor.getCount() > 0) {
                //create maps from lists
                LongSparseArray<Symbol> inputAlphabetMap = new LongSparseArray<>();
                for (Symbol symbol : inputAlphabetList) {
                    inputAlphabetMap.put(symbol.getId(), symbol);
                }
                LongSparseArray<Symbol> stackAlphabetMap = new LongSparseArray<>();
                for (Symbol symbol : stackAlphabetList) {
                    stackAlphabetMap.put(symbol.getId(), symbol);
                }
                LongSparseArray<State> stateMap = new LongSparseArray<>();
                for (State state : stateList) {
                    stateMap.put(state.getId(), state);
                }
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_TRANSITION_ID));
                    State fromState = stateMap.get(cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_TRANSITION_FROM_STATE)));
                    State toState = stateMap.get(cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_TRANSITION_TO_STATE)));
                    Symbol readSymbol = inputAlphabetMap.get(cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_TRANSITION_READ_SYMBOL)));
                    ArrayList<Symbol> popSymbolList = parsePDASymbolList(
                            cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_TRANSITION_POP)),
                            stackAlphabetMap
                    );
                    ArrayList<Symbol> pushSymbolList = parsePDASymbolList(
                            cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_TRANSITION_PUSH)),
                            stackAlphabetMap
                    );

                    transitionList.add(new PdaTransition(id, fromState, readSymbol, toState, popSymbolList, pushSymbolList));
                }
            }
            Log.i(TAG, "TRANSITION table content list returned");
        } catch (Exception e) {
            Log.e(TAG, "error while getting content of TRANSITION table", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return transitionList;
    }

    //method to get whole content of TRANSITION table for TM
    public List<Transition> getTmTransitionFullExtract
    (List<Symbol> inputAlphabetList, List<State> stateList) {
        Log.v(TAG, "getTmTransitionFullExtract method started");
        List<Transition> transitionList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(DbOpenHelper.TABLE_TRANSITION,
                    new String[]{DbOpenHelper.COLUMN_TRANSITION_ID, DbOpenHelper.COLUMN_TRANSITION_FROM_STATE,
                            DbOpenHelper.COLUMN_TRANSITION_TO_STATE, DbOpenHelper.COLUMN_TRANSITION_READ_SYMBOL,
                            DbOpenHelper.COLUMN_TRANSITION_WRITE_SYMBOL, DbOpenHelper.COLUMN_TRANSITION_DIRECTION},
                    null, null, null, null, null);
            Log.v(TAG, "cursor initialized -> " + cursor.getCount() + " rows returned");

            if (cursor.getCount() > 0) {
                //create maps from lists
                LongSparseArray<Symbol> inputAlphabetMap = new LongSparseArray<>();
                for (Symbol symbol : inputAlphabetList) {
                    inputAlphabetMap.put(symbol.getId(), symbol);
                }
                LongSparseArray<State> stateMap = new LongSparseArray<>();
                for (State state : stateList) {
                    stateMap.put(state.getId(), state);
                }
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_TRANSITION_ID));
                    State fromState = stateMap.get(cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_TRANSITION_FROM_STATE)));
                    State toState = stateMap.get(cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_TRANSITION_TO_STATE)));
                    Symbol readSymbol = inputAlphabetMap.get(cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_TRANSITION_READ_SYMBOL)));
                    Symbol writeSymbol = inputAlphabetMap.get(cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_TRANSITION_WRITE_SYMBOL)));
                    TmTransition.Direction direction = cursor.getInt(cursor.getColumnIndex(DbOpenHelper.COLUMN_TRANSITION_DIRECTION)) == 1 ? TmTransition.Direction.LEFT : TmTransition.Direction.RIGHT;

                    transitionList.add(new TmTransition(id, fromState, readSymbol, toState, writeSymbol, direction));
                }
            }
            Log.i(TAG, "TRANSITION table content list returned");
        } catch (Exception e) {
            Log.e(TAG, "error while getting content of TRANSITION table", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return transitionList;
    }

    //method to add tape element into database, throws exception if not successful

    public TapeElement addTapeElement(Symbol symbol, int order) {
        Log.v(TAG, "addTapeElement method started");
        TapeElement tapeElement;
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_TAPE_SYMBOL, symbol.getId());
        contentValues.put(DbOpenHelper.COLUMN_TAPE_ORDER, order);
        contentValues.put(DbOpenHelper.COLUMN_TAPE_BREAKPOINT, 0);
        Log.v(TAG, "contentValues prepared");

        long id = 0;
        try {
            database.insertOrThrow(DbOpenHelper.TABLE_TAPE, null, contentValues);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tapeElement = new TapeElement(id, symbol, order, false);
        Log.i(TAG, "tape element '" + tapeElement.getSymbol().getValue() + "' added into database");
        return tapeElement;
    }

    //method to update tape element in database, throws exception if not successful
    public void updateTapeElement(TapeElement tapeElement, Symbol symbol, int order) {
        Log.v(TAG, "updateTapeElement method started");
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_TAPE_SYMBOL, symbol.getId());
        contentValues.put(DbOpenHelper.COLUMN_TAPE_ORDER, order);
        contentValues.put(DbOpenHelper.COLUMN_TAPE_BREAKPOINT, tapeElement.isBreakpoint());
        Log.v(TAG, "contentValues prepared");

        database.update(DbOpenHelper.TABLE_TAPE, contentValues,
                DbOpenHelper.COLUMN_TAPE_ID + " = ?", new String[]{String.valueOf(tapeElement.getId())});
        tapeElement.setSymbol(symbol);
        tapeElement.setOrder(order);
        Log.i(TAG, "tape element '" + tapeElement.getSymbol().getValue() + "' updated in database");
    }

    //method to delete tape element from database, throws exception if not successful
    public void deleteTapeElement(TapeElement tapeElement) {
        Log.v(TAG, "deleteTapeElement method started");
        database.delete(DbOpenHelper.TABLE_TAPE, DbOpenHelper.COLUMN_INPUT_SYMBOL_ID + " = ?", new String[]{String.valueOf(tapeElement.getId())});
        Log.i(TAG, "tape element '" + tapeElement.getSymbol().getValue() + "' deleted from database");
    }

    //method to delete all tape elements from database
    public void dropTapeElements() {
        Log.v(TAG, "dropTapeElements method started");
        database.delete(DbOpenHelper.TABLE_TAPE, null, null);
        Log.i(TAG, "all tape elements deleted from database");
    }

    //method to get whole content of TAPE table
    public List<TapeElement> getTapeFullExtract(List<Symbol> inputAlphabetList) {
        Log.v(TAG, "getTapeFullExtract method started");
        List<TapeElement> tapeElementList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(DbOpenHelper.TABLE_TAPE,
                    new String[]{DbOpenHelper.COLUMN_TAPE_ID, DbOpenHelper.COLUMN_TAPE_SYMBOL,
                            DbOpenHelper.COLUMN_TAPE_BREAKPOINT, DbOpenHelper.COLUMN_TAPE_ORDER},
                    null, null, null, null, DbOpenHelper.COLUMN_TAPE_ORDER);
            Log.v(TAG, "cursor initialized -> " + cursor.getCount() + " rows returned");

            if (cursor.getCount() > 0) {
                //create maps from lists
                LongSparseArray<Symbol> inputAlphabetMap = new LongSparseArray<>();
                for (Symbol symbol : inputAlphabetList) {
                    inputAlphabetMap.put(symbol.getId(), symbol);
                }
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_TRANSITION_ID));
                    Symbol symbol = inputAlphabetMap.get(cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_TAPE_SYMBOL)));
                    int order = cursor.getInt(cursor.getColumnIndex(DbOpenHelper.COLUMN_TAPE_ORDER));
                    boolean breakpoint = cursor.getInt(cursor.getColumnIndex(DbOpenHelper.COLUMN_TAPE_BREAKPOINT)) == 1;

                    tapeElementList.add(new TapeElement(id, symbol, order, breakpoint));
                }
            }
            Log.i(TAG, "TAPE table content list returned");
        } catch (Exception e) {
            Log.e(TAG, "error while getting content of TAPE table", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return tapeElementList;
    }

    //method to add colors to database from resource file
    public List<MachineColor> addColorList(int[] colorsRaw) {
        Log.v(TAG, "addColorList method started");

        //remove all colors
        try {
            database.delete(DbOpenHelper.TABLE_COLOR, null, null);
            Log.i(TAG, "all colors deleted from database");
        } catch (Exception e) {
            Log.e(TAG, "error while removing content of COLOR table", e);
        }

        List<MachineColor> machineColorList = new ArrayList<>();
        for (int i = 0; i < colorsRaw.length; i++) {
            int color = colorsRaw[i];

            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(DbOpenHelper.COLUMN_COLOR_VALUE, color);
                contentValues.put(DbOpenHelper.COLUMN_COLOR_ORDER, i);
                Log.v(TAG, "contentValues prepared");

                long id = database.insertOrThrow(DbOpenHelper.TABLE_COLOR, null, contentValues);

                machineColorList.add(new MachineColor(id, color));
                Log.i(TAG, "color '" + color + "' added into database");
            } catch (Exception e) {
                Log.e(TAG, "error while adding content of COLOR table", e);
            }
        }
        return machineColorList;
    }

    //method to add color into database, throws exception if not successful
    public MachineColor addColor(int color, int order) {
        Log.v(TAG, "addColor method started");
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_COLOR_VALUE, color);
        contentValues.put(DbOpenHelper.COLUMN_COLOR_ORDER, order);
        Log.v(TAG, "contentValues prepared");

        long id = database.insertOrThrow(DbOpenHelper.TABLE_COLOR, null, contentValues);
        Log.i(TAG, "color '" + color + "' added into database");

        return new MachineColor(id, color);
    }

    //method to update color in database, throws exception if not successful
    public void updateColor(MachineColor machineColor, int value, int order) {
        Log.v(TAG, "updateColors method started");
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_COLOR_VALUE, value);
        contentValues.put(DbOpenHelper.COLUMN_COLOR_ORDER, order);
        Log.v(TAG, "contentValues prepared");

        database.update(DbOpenHelper.TABLE_COLOR, contentValues, DbOpenHelper.COLUMN_COLOR_ID + " = ?",
                new String[]{String.valueOf(machineColor.getId())});
        machineColor.setValue(value);

        Log.i(TAG, "color '" + machineColor.getValue() + "' updated in database");
    }

    //method to delete color from database, throws exception if not successful
    public void deleteColor(MachineColor machineColor) {
        Log.v(TAG, "deleteColor method started");
        database.delete(DbOpenHelper.TABLE_COLOR, DbOpenHelper.COLUMN_COLOR_ID + " = ?", new String[]{String.valueOf(machineColor.getId())});
        Log.i(TAG, "color '" + machineColor.getValue() + "' deleted from database");
    }

    //method to get whole content of COLOR table
    public List<MachineColor> getColors() {
        Log.v(TAG, "getColors method started");
        List<MachineColor> colorList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(DbOpenHelper.TABLE_COLOR,
                    new String[]{DbOpenHelper.COLUMN_COLOR_ID, DbOpenHelper.COLUMN_COLOR_VALUE,
                            DbOpenHelper.COLUMN_COLOR_ORDER},
                    null, null, null, null, DbOpenHelper.COLUMN_COLOR_ORDER);
            Log.v(TAG, "cursor initialized -> " + cursor.getCount() + " rows returned");

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndex(DbOpenHelper.COLUMN_COLOR_ID));
                    int value = cursor.getInt(cursor.getColumnIndex(DbOpenHelper.COLUMN_COLOR_VALUE));
                    colorList.add(new MachineColor(id, value));
                }
            }
            Log.i(TAG, "COLOR table content list returned");
        } catch (Exception e) {
            Log.e(TAG, "error while getting content of COLOR table", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return colorList;
    }

    public void updateMarkNondeterminism(boolean nondeterminism) {
        Log.v(TAG, "updateMarkNondeterminism method started");
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_OPTIONS_NONDETERMINISM, nondeterminism);
        Log.v(TAG, "contentValues prepared");

        database.update(DbOpenHelper.TABLE_OPTIONS, contentValues, null, null);

        Log.i(TAG, "markNondeterminism option updated in database");
    }

    public boolean getMarkNondeterminism() {
        Log.v(TAG, "getMarkNondeterminism method started");
        boolean markNondeterminism = Options.MARK_NONDETERMINISM_DEFAULT;
        Cursor cursor = null;
        try {
            cursor = database.query(DbOpenHelper.TABLE_OPTIONS,
                    new String[]{DbOpenHelper.COLUMN_OPTIONS_NONDETERMINISM},
                    null, null, null, null, null);
            Log.v(TAG, "cursor initialized -> " + cursor.getCount() + " rows returned");

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    markNondeterminism = cursor.getInt(cursor.getColumnIndex(DbOpenHelper.COLUMN_OPTIONS_NONDETERMINISM)) == 1;
                }
            } else {
                addOptions();
            }
        } catch (Exception e) {
            Log.e(TAG, "error while getting content of OPTIONS table", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return markNondeterminism;
    }

    public byte getNextRequestId() {
        Log.v(TAG, "getNextRequestId method started");

        byte result;
        Cursor cursor = database.query(DbOpenHelper.TABLE_OPTIONS,
                new String[]{DbOpenHelper.COLUMN_OPTIONS_REQUEST_ID},
                null, null, null, null, null);
        Log.v(TAG, "cursor initialized -> " + cursor.getCount() + " rows returned");
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            result = (byte) cursor.getInt(cursor.getColumnIndex(DbOpenHelper.COLUMN_OPTIONS_REQUEST_ID));
        } else {
            addOptions();
            result = Options.REQUEST_ID_DEFAULT;
        }
        cursor.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_OPTIONS_REQUEST_ID, (byte) (result + 1));
        Log.v(TAG, "contentValues prepared");

        database.update(DbOpenHelper.TABLE_OPTIONS, contentValues, null, null);
        Log.i(TAG, "request ID option updated in database");

        return result;
    }

    public String getUserName() {
        Log.v(TAG, "getUserName method started");

        String result;
        Cursor cursor = database.query(DbOpenHelper.TABLE_OPTIONS,
                new String[]{DbOpenHelper.COLUMN_OPTIONS_USER_NAME},
                null, null, null, null, null);
        Log.v(TAG, "cursor initialized -> " + cursor.getCount() + " rows returned");
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            result = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_OPTIONS_USER_NAME));
        } else {
            addOptions();
            result = Options.USER_NAME_DEFAULT;
        }
        cursor.close();

        return result;
    }

    public int getRegexDepth() {
        Log.v(TAG, "getRegexDepth method started");

        int result;
        Cursor cursor = database.query(DbOpenHelper.TABLE_OPTIONS,
                new String[]{DbOpenHelper.COLUMN_OPTIONS_REGEX_DEPTH},
                null, null, null, null, null);
        Log.v(TAG, "cursor initialized -> " + cursor.getCount() + " rows returned");
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            result = cursor.getInt(cursor.getColumnIndex(DbOpenHelper.COLUMN_OPTIONS_REGEX_DEPTH));
        } else {
            addOptions();
            result = Options.REGEX_DEPTH_DEFAULT;
        }
        cursor.close();

        return result;
    }

    public void updateUserName(String userName) {
        Log.v(TAG, "updateUserName method started");

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_OPTIONS_USER_NAME, userName);
        Log.v(TAG, "contentValues prepared");

        database.update(DbOpenHelper.TABLE_OPTIONS, contentValues, null, null);
        Log.i(TAG, "user name option updated in database");
    }

    public int getMaxSteps() {
        Log.v(TAG, "getMaxSteps method started");

        int result;
        Cursor cursor = database.query(DbOpenHelper.TABLE_OPTIONS,
                new String[]{DbOpenHelper.COLUMN_OPTIONS_MAX_STEPS},
                null, null, null, null, null);
        Log.v(TAG, "cursor initialized -> " + cursor.getCount() + " rows returned");
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            result = cursor.getInt(cursor.getColumnIndex(DbOpenHelper.COLUMN_OPTIONS_MAX_STEPS));
        } else {
            addOptions();
            result = Options.MAX_STEPS_DEFAULT;
        }
        cursor.close();

        return result;
    }

    public void updateMaxSteps(int maxSteps) {
        Log.v(TAG, "updateUserName method started");

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_OPTIONS_MAX_STEPS, maxSteps);
        Log.v(TAG, "contentValues prepared");

        database.update(DbOpenHelper.TABLE_OPTIONS, contentValues, null, null);
        Log.i(TAG, "user name option updated in database");
    }

    public void updateRegexDepth(int regexDepth)
    {
        Log.v(TAG, "updateRegexDepth method started");

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_OPTIONS_REGEX_DEPTH, regexDepth);
        Log.v(TAG, "contentValues prepared");

        database.update(DbOpenHelper.TABLE_OPTIONS, contentValues, null, null);
        Log.i("TAG", "regex depth updated in database");
    }



    private void addOptions() {
        Log.v(TAG, "addOptions started");
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbOpenHelper.COLUMN_OPTIONS_NONDETERMINISM, Options.MARK_NONDETERMINISM_DEFAULT ? 1 : 0);
        contentValues.put(DbOpenHelper.COLUMN_OPTIONS_USER_NAME, Options.USER_NAME_DEFAULT);
        contentValues.put(DbOpenHelper.COLUMN_OPTIONS_REQUEST_ID, Options.REQUEST_ID_DEFAULT);
        contentValues.put(DbOpenHelper.COLUMN_OPTIONS_MAX_STEPS, Options.MAX_STEPS_DEFAULT);
        contentValues.put(DbOpenHelper.COLUMN_OPTIONS_REGEX_DEPTH, Options.REGEX_DEPTH_DEFAULT);
        Log.v(TAG, "contentValues prepared");

        database.insertOrThrow(DbOpenHelper.TABLE_OPTIONS, null, contentValues);
    }

    /**
     * Deletes all transient data (i.e. data related to the open automaton,
     * not global settings) from the database
     */
    public void globalDrop() {
        dbHelper.dropTransientTables(database);

        dbHelper.onCreate(database);
    }
}
