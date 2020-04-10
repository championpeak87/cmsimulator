package fiitstu.gulis.cmsimulator.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Class responsible for creating SQLite database tables and obtaining the database connection.
 *
 * Created by Martin on 28. 2. 2017.
 */
class DbOpenHelper extends SQLiteOpenHelper {

    //constants
    //log tag
    private static final String TAG = DbOpenHelper.class.getName();

    private static final String DATABASE_NAME = "DATABASE";
    private static final int DATABASE_VERSION = 3;

    //table input alphabet
    static final String TABLE_INPUT_ALPHABET = "INPUT_ALPHABET";
    static final String COLUMN_INPUT_SYMBOL_ID = "ID";
    static final String COLUMN_INPUT_SYMBOL_VALUE = "VALUE";
    static final String COLUMN_INPUT_SYMBOL_PROPERTIES = "PROPERTIES";
    //table stack alphabet
    static final String TABLE_STACK_ALPHABET = "STACK_ALPHABET";
    static final String COLUMN_STACK_SYMBOL_ID = "ID";
    static final String COLUMN_STACK_SYMBOL_VALUE = "VALUE";
    static final String COLUMN_STACK_SYMBOL_PROPERTIES = "PROPERTIES";
    //table state
    static final String TABLE_STATE = "STATE";
    static final String COLUMN_STATE_ID = "ID";
    static final String COLUMN_STATE_VALUE = "VALUE";
    static final String COLUMN_STATE_POS_X = "POSX";
    static final String COLUMN_STATE_POS_Y = "POSY";
    static final String COLUMN_STATE_INITIAL = "INITIAL";
    static final String COLUMN_STATE_FINAL = "FINAL";
    //table transition
    static final String TABLE_TRANSITION = "TRANSITION";
    static final String COLUMN_TRANSITION_ID = "ID";
    static final String COLUMN_TRANSITION_FROM_STATE = "FROM_STATE";
    static final String COLUMN_TRANSITION_TO_STATE = "TO_STATE";
    static final String COLUMN_TRANSITION_READ_SYMBOL = "READ_SYMBOL";
    static final String COLUMN_TRANSITION_WRITE_SYMBOL = "WRITE_SYMBOL";
    static final String COLUMN_TRANSITION_DIRECTION = "DIRECTION";
    static final String COLUMN_TRANSITION_POP = "POP";
    static final String COLUMN_TRANSITION_PUSH = "PUSH";
    //table tape
    static final String TABLE_TAPE = "TAPE";
    static final String COLUMN_TAPE_ID = "ID";
    static final String COLUMN_TAPE_SYMBOL = "TAPE_SYMBOL";
    static final String COLUMN_TAPE_ORDER = "TAPE_ORDER";
    static final String COLUMN_TAPE_BREAKPOINT = "BREAKPOINT";
    //table tests
    static final String TABLE_TEST = "TEST";
    static final String TABLE_NEGATIVE_TEST = "NEGATIVE_TEST";
    static final String COLUMN_TEST_ID = "ID";
    static final String COLUMN_TEST_INPUT = "INPUT";
    static final String COLUMN_TEST_OUTPUT = "OUTPUT";
    //table color
    static final String TABLE_COLOR = "COLOR";
    static final String COLUMN_COLOR_ID = "ID";
    static final String COLUMN_COLOR_VALUE = "VALUE";
    static final String COLUMN_COLOR_ORDER = "MACHINE_ORDER";
    //table options
    static final String TABLE_OPTIONS = "OPTIONS";
    static final String COLUMN_OPTIONS_NONDETERMINISM = "NONDETERMINISM";
    static final String COLUMN_OPTIONS_USER_NAME = "USER_NAME";
    static final String COLUMN_OPTIONS_REQUEST_ID = "REQUEST_ID";
    static final String COLUMN_OPTIONS_MAX_STEPS = "MAX_STEPS";
    static final String COLUMN_OPTIONS_REGEX_DEPTH = "REGEX_DEPTH";
    //table grammar rule
    static final String TABLE_GRAMMAR_RULE = "GRAMMAR_RULE";
    static final String COLUMN_RIGHT_RULE = "RIGHT_RULE";
    static final String COLUMN_LEFT_RULE = "LEFT_RULE";
    static final String COLUMN_GRAMMAR_RULE_ID = "ID";
    //tasks
    static final String AUTOLOGIN = "AUTOLOGIN";
    static final String AUTOLOGIN_USERNAME = "USERNAME";
    static final String AUTOLOGIN_AUTHKEY = "AUTHKEY";
    static final String AUTOLOGIN_VALUES = "AUTOLOGIN_VALUES";
    //table grammar tests
    static final String TABLE_GRAMMAR_TEST = "GRAMMAR_TESTS";
    static final String COLUMN_INPUT_WORD = "INPUT_WORD";
    //table chess game path
    static final String TABLE_CHESS_GAME_PATH = "CHESS_GAME_PATH";
    static final String PATH_FIELD_X = "PATH_FIELD_X";
    static final String PATH_FIELD_Y = "PATH_FIELD_Y";
    // table chess game starting field
    static final String TABLE_CHESS_GAME_START_FIELD = "CHESS_GAME_START_FIELD";
    static final String START_FIELD_X = "START_FIELD_X";
    static final String START_FIELD_Y = "START_FIELD_Y";
    // table chess game finish field
    static final String TABLE_CHESS_GAME_FINISH_FIELD = "CHESS_GAME_FINISH_FIELD";
    static final String FINISH_FIELD_X = "FINISH_FIELD_X";
    static final String FINISH_FIELD_Y = "FINISH_FIELD_Y";
    // table chess game field size
    static final String TABLE_CHESS_GAME_FIELD_SIZE = "CHESS_GAME_FIELD_SIZE";
    static final String FIELD_SIZE_X = "FIELD_SIZE_X";
    static final String FIELD_SIZE_Y = "FIELD_SIZE_Y";
    // table chess game max states
    static final String TABLE_CHESS_GAME_MAX_STATES = "CHESS_GAME_MAX_STATES";
    static final String MAX_STATES = "MAX_STATES";

    // create table chess_game_path
    private static final String CREATE_TABLE_CHESS_GAME_PATH =
            "CREATE TABLE " + TABLE_CHESS_GAME_PATH + " (" +
                    PATH_FIELD_X + " INTEGER, " +
                    PATH_FIELD_Y + " INTEGER )";

    // create table chess_game_start_field
    private static final String CREATE_TABLE_CHESS_GAME_START_FIELD =
            "CREATE TABLE " + TABLE_CHESS_GAME_START_FIELD + " (" +
                    START_FIELD_X + " INTEGER, " +
                    START_FIELD_Y + " INTEGER )";

    // create table chess_game_finish_field
    private static final String CREATE_TABLE_CHESS_GAME_FINISH_FIELD =
            "CREATE TABLE " + TABLE_CHESS_GAME_FINISH_FIELD + " (" +
                    FINISH_FIELD_X + " INTEGER ," +
                    FINISH_FIELD_Y + " INTEGER )";

    // create table chess_game_field_size
    private static final String CREATE_TABLE_CHESS_GAME_FIELD_SIZE =
            "CREATE TABLE " + TABLE_CHESS_GAME_FIELD_SIZE + " (" +
                    FIELD_SIZE_X + " INTEGER ," +
                    FIELD_SIZE_Y + " INTEGER )";

    // create table chess_game_max_states
    private static final String CREATE_TABLE_CHESS_GAME_MAX_STATES =
            "CREATE TABLE " + TABLE_CHESS_GAME_MAX_STATES + " (" +
                    MAX_STATES + " INTEGER )";

    //create table input alphabet sqlite query
    private static final String TABLE_INPUT_ALPHABET_CREATE =
            "CREATE TABLE " + TABLE_INPUT_ALPHABET + " ( " +
                    COLUMN_INPUT_SYMBOL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_INPUT_SYMBOL_VALUE + " TEXT UNIQUE, " +
                    COLUMN_INPUT_SYMBOL_PROPERTIES + " INTEGER " +
                    ")";
    //create table stack alphabet sqlite query
    private static final String TABLE_STACK_ALPHABET_CREATE =
            "CREATE TABLE " + TABLE_STACK_ALPHABET + " ( " +
                    COLUMN_STACK_SYMBOL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_STACK_SYMBOL_VALUE + " TEXT UNIQUE, " +
                    COLUMN_STACK_SYMBOL_PROPERTIES + " INTEGER " +
                    ")";
    //create table state sqlite query
    private static final String TABLE_STATE_CREATE =
            "CREATE TABLE " + TABLE_STATE + " ( " +
                    COLUMN_STATE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_STATE_VALUE + " TEXT UNIQUE, " +
                    COLUMN_STATE_POS_X + " INTEGER, " +
                    COLUMN_STATE_POS_Y + " INTEGER, " +
                    //boolean type not supported in sqlite, values need to be stored as integer (0 = false, 1 = true),
                    COLUMN_STATE_INITIAL + " INTEGER CHECK("+ COLUMN_STATE_INITIAL + " = 0 OR " + COLUMN_STATE_INITIAL + " = 1), " +
                    COLUMN_STATE_FINAL + " INTEGER CHECK("+ COLUMN_STATE_FINAL + " = 0 OR " + COLUMN_STATE_FINAL + " = 1)" +
                    ")";
    //create table transition sqlite query (write symbol foreign key is not enforced because not all machine types use it)
    private static final String TABLE_TRANSITION_CREATE =
            "CREATE TABLE " + TABLE_TRANSITION + " ( " +
                    COLUMN_TRANSITION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TRANSITION_FROM_STATE + " INTEGER, " +
                    COLUMN_TRANSITION_TO_STATE + " INTEGER, " +
                    COLUMN_TRANSITION_READ_SYMBOL + " INTEGER, " +
                    COLUMN_TRANSITION_WRITE_SYMBOL + " INTEGER, " +
                    //0 = right, 1 = left
                    COLUMN_TRANSITION_DIRECTION + " INTEGER, " +
                    COLUMN_TRANSITION_POP + " TEXT, " +
                    COLUMN_TRANSITION_PUSH + " TEXT, " +
                    " UNIQUE (" + COLUMN_TRANSITION_FROM_STATE + ", " + COLUMN_TRANSITION_TO_STATE + ", " +
                    COLUMN_TRANSITION_READ_SYMBOL + ", " + COLUMN_TRANSITION_WRITE_SYMBOL + ", " +
                    COLUMN_TRANSITION_DIRECTION + ", " + COLUMN_TRANSITION_POP + ", " +
                    COLUMN_TRANSITION_PUSH + "), " +
                    " FOREIGN KEY (" + COLUMN_TRANSITION_TO_STATE + ") REFERENCES " + TABLE_STATE + "(" + COLUMN_STATE_ID + ") ON UPDATE CASCADE ON DELETE RESTRICT" +
                    " FOREIGN KEY (" + COLUMN_TRANSITION_FROM_STATE + ") REFERENCES " + TABLE_STATE + "(" + COLUMN_STATE_ID + ") ON UPDATE CASCADE ON DELETE RESTRICT" +
                    " FOREIGN KEY (" + COLUMN_TRANSITION_READ_SYMBOL + ") REFERENCES " + TABLE_INPUT_ALPHABET + "(" + COLUMN_INPUT_SYMBOL_ID + ") ON UPDATE CASCADE ON DELETE RESTRICT" +
                    " FOREIGN KEY (" + COLUMN_TRANSITION_WRITE_SYMBOL + ") REFERENCES " + TABLE_INPUT_ALPHABET + "(" + COLUMN_INPUT_SYMBOL_ID + ") ON UPDATE CASCADE ON DELETE RESTRICT" +
                    ")";
    //create table tape sqlite query
    private static final String TABLE_TAPE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_TAPE + " ( " +
                    COLUMN_TAPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TAPE_SYMBOL + " INTEGER, " +
                    COLUMN_TAPE_ORDER + " INTEGER UNIQUE, " +
                    COLUMN_TAPE_BREAKPOINT + " INTEGER CHECK("+ COLUMN_TAPE_BREAKPOINT + " = 0 OR " + COLUMN_TAPE_BREAKPOINT + " = 1), " +
                    " FOREIGN KEY (" + COLUMN_TAPE_SYMBOL + ") REFERENCES " + TABLE_INPUT_ALPHABET + "(" + COLUMN_INPUT_SYMBOL_ID + ") ON UPDATE CASCADE ON DELETE RESTRICT" +
                    ")";
    //create table test sqlite query
    private static final String TABLE_TEST_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_TEST + " ( " +
                    COLUMN_TEST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TEST_INPUT + " TEXT, " +
                    COLUMN_TEST_OUTPUT + " TEXT " +
                    ")";
    //create table negative test sqlite query
    private static final String TABLE_NEGATIVE_TEST_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NEGATIVE_TEST + " ( " +
                    COLUMN_TEST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TEST_INPUT + " TEXT, " +
                    COLUMN_TEST_OUTPUT + " TEXT " +
                    ")";
    //create table color sqlite query
    private static final String TABLE_COLOR_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_COLOR + " ( " +
                    COLUMN_COLOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_COLOR_VALUE + " INTEGER, " +
                    COLUMN_COLOR_ORDER + " INTEGER UNIQUE " +
                    ")";
    //create table options query
    private static final String TABLE_OPTIONS_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_OPTIONS + " ( " +
                    //boolean type not supported in sqlite, values need to be stored as integer (0 = false, 1 = true),
                    COLUMN_OPTIONS_NONDETERMINISM + " INTEGER " +
                        "CHECK(" + COLUMN_OPTIONS_NONDETERMINISM + " = 0 OR " + COLUMN_OPTIONS_NONDETERMINISM + " = 1)," +
                    COLUMN_OPTIONS_USER_NAME + " TEXT, " +
                    COLUMN_OPTIONS_REQUEST_ID + " INTEGER " +
                        "CHECK(" + COLUMN_OPTIONS_REQUEST_ID + ">= -128 AND " + COLUMN_OPTIONS_REQUEST_ID + " <= 127)," +
                    COLUMN_OPTIONS_MAX_STEPS + " INTEGER, " +
                    COLUMN_OPTIONS_REGEX_DEPTH + " INTEGER " +
                    ")";
    //create table grammar rule query
    private static final String TABLE_GRAMMAR_RULE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_GRAMMAR_RULE + "(" +
                    COLUMN_GRAMMAR_RULE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_RIGHT_RULE + " TEXT, " +
                    COLUMN_LEFT_RULE + " TEXT " +
                    ")";
    //create table grammar tests
    private static final String TABLE_GRAMMAR_TEST_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_GRAMMAR_TEST + "(" +
            COLUMN_INPUT_WORD + " TEXT " +
            ")";

    //singleton pattern
    private static DbOpenHelper instance = null;

    //private constructor
    private DbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //method to get instance of open helper
    static synchronized DbOpenHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DbOpenHelper(context.getApplicationContext());
            Log.i(TAG, "new instance created");
        }
        Log.i(TAG, "instance returned");
        return instance;
    }

    //method creates basic tables in sqlite database
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_INPUT_ALPHABET_CREATE);
        Log.i(TAG, "INPUT ALPHABET table created (query command executed)");

        db.execSQL(TABLE_STACK_ALPHABET_CREATE);
        Log.i(TAG, "STACK ALPHABET table created (query command executed)");

        db.execSQL(TABLE_STATE_CREATE);
        Log.i(TAG, "STATE table created (query command executed)");

        db.execSQL(TABLE_TRANSITION_CREATE);
        Log.i(TAG, "TRANSITION table created (query command executed)");

        db.execSQL(TABLE_TAPE_CREATE);
        Log.i(TAG, "TAPE table created (query command executed)");

        db.execSQL(TABLE_TEST_CREATE);
        Log.i(TAG, "TEST table created (query command executed)");

        db.execSQL(TABLE_NEGATIVE_TEST_CREATE);
        Log.i(TAG, "TEST table created (query command executed)");

        db.execSQL(TABLE_COLOR_CREATE);
        Log.i(TAG, "COLOR table created (query command executed)");

        db.execSQL(TABLE_OPTIONS_CREATE);
        Log.i(TAG, "OPTIONS table created (query command executed)");

        db.execSQL(TABLE_GRAMMAR_RULE_CREATE);
        Log.i(TAG, "GRAMMAR RULE table created (query command executed)");

        db.execSQL(TABLE_GRAMMAR_TEST_CREATE);
        Log.i(TAG, "GRAMMAR TEST table created (query command executed)");

        db.execSQL(CREATE_TABLE_CHESS_GAME_PATH);
        Log.i(TAG, "CHESS GAME PATH table created (query command executed)");

        db.execSQL(CREATE_TABLE_CHESS_GAME_START_FIELD);
        Log.i(TAG, "CHESS GAME START FIELD table created (query command executed)");

        db.execSQL(CREATE_TABLE_CHESS_GAME_FINISH_FIELD);
        Log.i(TAG, "CHESS GAME FINISH FIELD table created (query command executed)");

        db.execSQL(CREATE_TABLE_CHESS_GAME_FIELD_SIZE);
        Log.i(TAG, "CHESS GAME FIELD SIZE table created (query command executed)");

        db.execSQL(CREATE_TABLE_CHESS_GAME_MAX_STATES);
        Log.i(TAG, "CHESS GAME MAX STATES table created (query command executed)");
    }

    //method updates basic tables in sqlite database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropAllTables(db);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropAllTables(db);
        onCreate(db);
    }

    //method is called every time, database connection is opened
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys = ON");
    }

    void dropTransientTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + DbOpenHelper.TABLE_TEST);
        Log.w(TAG, "TEST table dropped (query command executed)");

        db.execSQL("DROP TABLE IF EXISTS " + DbOpenHelper.TABLE_NEGATIVE_TEST);
        Log.w(TAG, "NEGATIVE TEST table dropped (query command executed)");

        db.execSQL("DROP TABLE IF EXISTS " + DbOpenHelper.TABLE_TAPE);
        Log.w(TAG, "TAPE table dropped (query command executed)");

        db.execSQL("DROP TABLE IF EXISTS " + DbOpenHelper.TABLE_TRANSITION);
        Log.w(TAG, "TRANSITION table dropped (query command executed)");

        db.execSQL("DROP TABLE IF EXISTS " + DbOpenHelper.TABLE_STATE);
        Log.w(TAG, "STATE table dropped (query command executed)");

        db.execSQL("DROP TABLE IF EXISTS " + DbOpenHelper.TABLE_STACK_ALPHABET);
        Log.w(TAG, "STACK ALPHABET table dropped (query command executed)");

        db.execSQL("DROP TABLE IF EXISTS " + DbOpenHelper.TABLE_INPUT_ALPHABET);
        Log.w(TAG, "INPUT ALPHABET table dropped (query command executed)");

        db.execSQL("DROP TABLE IF EXISTS " + DbOpenHelper.TABLE_GRAMMAR_RULE);
        Log.w(TAG, "GRAMMAR RULE table dropped (query command executed)");

        db.execSQL("DROP TABLE IF EXISTS " + DbOpenHelper.TABLE_GRAMMAR_TEST);
        Log.w(TAG, "GRAMMAR TEST table dropped (query command executed)");

        db.execSQL("DROP TABLE IF EXISTS " + DbOpenHelper.TABLE_CHESS_GAME_PATH);
        Log.w(TAG, "CHESS GAME PATH table dropped (query command executed)");

        db.execSQL("DROP TABLE IF EXISTS " + DbOpenHelper.TABLE_CHESS_GAME_START_FIELD);
        Log.w(TAG, "CHESS GAME START FIELD table dropped (query command executed)");

        db.execSQL("DROP TABLE IF EXISTS " + DbOpenHelper.TABLE_CHESS_GAME_FINISH_FIELD);
        Log.w(TAG, "CHESS GAME FINISH FIELD table dropped (query command executed)");

        db.execSQL("DROP TABLE IF EXISTS " + DbOpenHelper.TABLE_CHESS_GAME_FIELD_SIZE);
        Log.w(TAG, "CHESS GAME FIELD SIZE table dropped (query command executed)");

        db.execSQL("DROP TABLE IF EXISTS " + DbOpenHelper.TABLE_CHESS_GAME_MAX_STATES);
        Log.w(TAG, "CHESS GAME MAX STATES table dropped (query command executed)");
    }

    private void dropAllTables(SQLiteDatabase db) {
        dropTransientTables(db);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OPTIONS);
        Log.w(TAG, "OPTIONS table dropped (query command executed)");

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COLOR);
        Log.w(TAG, "COLOR table dropped (query command executed)");
    }
}
