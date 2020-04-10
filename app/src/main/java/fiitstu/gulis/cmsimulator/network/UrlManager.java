package fiitstu.gulis.cmsimulator.network;

import android.net.Uri;
import fiitstu.gulis.cmsimulator.BuildConfig;
import fiitstu.gulis.cmsimulator.activities.MainActivity;
import fiitstu.gulis.cmsimulator.elements.Task;
import fiitstu.gulis.cmsimulator.models.ChessGame;
import fiitstu.gulis.cmsimulator.models.tasks.automata_type;
import fiitstu.gulis.cmsimulator.models.tasks.grammar_tasks.GrammarTask;
import fiitstu.gulis.cmsimulator.models.users.User;
import fiitstu.gulis.cmsimulator.network.users.PasswordManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.util.Date;

public class UrlManager {

    private static String URI;

    // PATHS
    private final static String LOGIN_PATH = "/api/user/login";
    private final static String CHANGE_PASSWORD_PATH = "/api/user/changePassword";
    private final static String ADD_NEW_USER_PATH = "/api/user/signup";
    private final static String GET_ALL_USERS_PATH = "/api/user/getUsers";
    private final static String SEARCH_USER_PATH = "/api/user/findUsers";
    private final static String PUBLISH_TASK_PATH = "/api/tasks/upload";
    private final static String ADD_TASK_TO_TABLE = "/api/tasks/add";
    private final static String GET_ALL_AUTOMATA_TASKS_PATH = "/api/tasks/getTasks";
    private final static String DELETE_USER_PATH = "/api/user/delete";
    private final static String UPDATE_USER_PATH = "/api/user/update";
    private final static String GET_USER_SALT_PATH = "/api/user/login_salt";
    private final static String DELETE_AUTOMATA_TASK_PATH = "/api/tasks/delete";
    private final static String DOWNLOAD_AUTOMATA_TASK_PATH = "/api/tasks/download";
    private final static String SAVE_AUTOMATA_TASK_PATH = "/api/tasks/save";
    private final static String CHANGE_TASK_FLAG_PATH = "/api/tasks/changeFlag";
    private final static String GET_TASK_FLAG_PATH = "/api/tasks/getFlag";
    private final static String SUBMIT_AUTOMATA_TASK_PATH = "/api/tasks/submit";
    private final static String GET_USERS_COUNT_PATH = "/api/user/getCount";
    private final static String UPDATE_TIMER_PATH = "/api/tasks/updateTimer";
    private final static String ORDER_USERS_URL = "/api/user/filterUsers";
    private final static String UPLOAD_GRAMMAR_TASK_URL = "/api/grammarTasks/upload";
    private final static String ADD_GRAMMAR_TASK_TO_TABLE = "/api/grammarTasks/add";
    private final static String GET_ALL_GRAMMAR_TASKS_PATH = "/api/grammarTasks/getTasks";
    private final static String DELETE_GRAMMAR_TASK_PATH = "/api/grammarTasks/delete";
    private final static String CHANGE_GRAMMAR_TASK_FLAG_PATH = "/api/grammarTasks/changeFlag";
    private final static String DOWNLOAD_GRAMMAR_TASK_PATH = "/api/grammarTasks/download";
    private final static String UPDATE_TIMER_GRAMMAR_TASK_PATH = "/api/grammarTasks/updateTimer";
    private final static String SUBMIT_GRAMMAR_TASK_PATH = "/api/grammarTasks/submit";
    private final static String SAVE_GRAMMAR_TASK_PATH = "/api/grammarTasks/save";
    private final static String USER_RESULTS_OVERVIEW_PATH = "/api/tasks/results";
    private final static String GET_AUTOMATA_TASKS_COUNT_PATH = "/api/tasks/taskCount";
    private final static String GET_GRAMMAR_TASKS_COUNT_PATH = "/api/grammarTasks/taskCount";
    private static final String GRAMMAR_RESULTS_OVERVIEW_PATH = "/api/grammarTasks/results";
    private static final String ADD_GAME_TO_DATABASE_PATH = "/api/game/add";
    private static final String UPLOAD_GAME_PATH = "/api/game/upload";

    // LOGIN QUERY KEYS
    private final static String USERNAME_QUERY_KEY = "username";
    private final static String AUTHKEY_QUERY_KEY = "auth_key";

    // SIGN UP QUERY KEYS
    private final static String FIRST_NAME_KEY = "first_name";
    private final static String LAST_NAME_KEY = "last_name";
    private final static String USER_TYPE_KEY = "user_type";
    private final static String SALT_KEY = "salt";

    // CHANGE PASSWORD QUERY KEYS
    private final static String USER_ID_KEY = "user_id";
    private final static String NEW_AUTH_KEY = "new_auth_key";
    private final static String OLD_AUTH_KEY = "auth_key";

    // ADD TASK TO TABLE QUERY KEYS
    private final static String TASK_NAME_KEY = "task_name";
    private final static String TASK_DESCRIPTION_KEY = "task_description";
    private final static String TIME_KEY = "time";
    private final static String ASSIGNER_KEY = "assigner";
    private final static String PUBLIC_INPUT_KEY = "public_input";
    private final static String FILE_NAME_KEY = "file_name";
    private final static String AUTOMATA_TYPE_KEY = "automata_type";

    // DELETE USER QUERIES
    private final static String LOGGED_USER_ID_KEY = "logged_user_id";

    // UPDATE_USER_QUERIES
    private final static String TYPE_KEY = "type";
    private final static String PASSWORD_HASH_KEY = "password_hash";

    // DELETE TASK QUERIES
    private final static String TASK_ID_KEY = "task_id";

    // GET ALL USERS QUERIES
    private final static String OFFSET_QUERY_KEY = "offset";

    // CHANGE FLAG QUERIES
    private final static String TASK_STATUS_KEY = "task_status";

    // UPDATE TIMER QUERIES
    private final static String ELAPSED_TIME_KEY = "time_elapsed";

    // SEARCH QUERIES
    private final static String QUERY_SEARCH_STRING_KEY = "string";
    private final static String SEARCH_BY_KEY = "find_by";

    // FILTER QUERIES
    private final static String ORDER_BY_KEY = "order_by";
    private final static String ASCENDING_KEY = "ascending";

    // SUBMIT TASK
    private final static String SUBMISSION_TIME = "submission_time";

    public UrlManager() {
        String buildType = BuildConfig.FLAVOR;
        if (buildType.equals("alpha")) {
            // CHANGE YOUR IP ADDRESS TO LOCALHOST, leave port 3000 unless changed in CMServer.js
            // USE "ipconfig" in cmd (WINDOWS) to find your LOCAL IP ADDRESS, "ifconfig" for *nix system
            //
            // DEBIAN BASED SYSTEM "ifconfig installation"
            // sudo apt install net-tools -y
            URI = "http://192.168.1.235:8080";
        } else {
            URI = "http://147.175.150.53:8080";
        }
    }

    public URL addGameToDatabaseURL(String task_name, String task_description, int assigner_id, automata_type automata_type) {
        Uri uri = Uri.parse(URI + ADD_GAME_TO_DATABASE_PATH).buildUpon()
                .appendQueryParameter(TASK_NAME_KEY, task_name)
                .appendQueryParameter(TASK_DESCRIPTION_KEY, task_description)
                .appendQueryParameter(ASSIGNER_KEY, Integer.toString(assigner_id))
                .appendQueryParameter(AUTOMATA_TYPE_KEY, automata_type.getApiKey())
                .build();

        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }

    public URL getUploadGameURL(String file_name) {
        Uri uri = Uri.parse(URI + UPLOAD_GAME_PATH).buildUpon()
                .appendQueryParameter(FILE_NAME_KEY, file_name)
                .build();


        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }


    public URL getAutomataTasksCountURL() {
        Uri uri = Uri.parse(URI + GET_AUTOMATA_TASKS_COUNT_PATH);

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }

    public URL getGrammarTasksCountURL() {
        Uri uri = Uri.parse(URI + GET_GRAMMAR_TASKS_COUNT_PATH);

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }

    public URL getGrammarResultsOverviewURL(int user_id) {
        Uri uri = Uri.parse(URI + GRAMMAR_RESULTS_OVERVIEW_PATH).buildUpon()
                .appendQueryParameter(USER_ID_KEY, Integer.toString(user_id))
                .build();

        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }

    public URL getUserResultsOverviewURL(int user_id) {
        Uri uri = Uri.parse(URI + USER_RESULTS_OVERVIEW_PATH).buildUpon()
                .appendQueryParameter(USER_ID_KEY, Integer.toString(user_id))
                .build();

        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }

    public URL getSaveGrammarURL(int user_id, String file_name) {
        Uri uri = Uri.parse(URI + SAVE_GRAMMAR_TASK_PATH).buildUpon()
                .appendQueryParameter(USER_ID_KEY, Integer.toString(user_id))
                .appendQueryParameter(FILE_NAME_KEY, file_name)
                .build();

        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }

    public URL getUpdateGrammarTimerURL(int user_id, int task_id, Time time) {
        Uri uri = Uri.parse(URI + UPDATE_TIMER_GRAMMAR_TASK_PATH).buildUpon()
                .appendQueryParameter(USER_ID_KEY, Integer.toString(user_id))
                .appendQueryParameter(TASK_ID_KEY, Integer.toString(task_id))
                .appendQueryParameter(ELAPSED_TIME_KEY, time.toString())
                .build();

        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }

    public URL getSubmitGrammarTaskUrl(int user_id, int task_id, Task.TASK_STATUS status, Date submission_time) {
        Uri uri = Uri.parse(URI + SUBMIT_GRAMMAR_TASK_PATH).buildUpon()
                .appendQueryParameter(USER_ID_KEY, Integer.toString(user_id))
                .appendQueryParameter(TASK_ID_KEY, Integer.toString(task_id))
                .appendQueryParameter(TASK_STATUS_KEY, status.toString())
                .appendQueryParameter(SUBMISSION_TIME, submission_time.toString())
                .build();

        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }

    public URL getDownloadGrammarTaskURL(int user_id, int task_id) {
        Uri uri = Uri.parse(URI + DOWNLOAD_GRAMMAR_TASK_PATH).buildUpon()
                .appendQueryParameter(USER_ID_KEY, Integer.toString(user_id))
                .appendQueryParameter(TASK_ID_KEY, Integer.toString(task_id))
                .build();

        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }

    public URL getChangeGrammarTaskFlagURL(Task.TASK_STATUS status, int task_id, int user_id) {
        Uri uri = Uri.parse(URI + CHANGE_GRAMMAR_TASK_FLAG_PATH).buildUpon()
                .appendQueryParameter(TASK_ID_KEY, Integer.toString(task_id))
                .appendQueryParameter(USER_ID_KEY, Integer.toString(user_id))
                .appendQueryParameter(TASK_STATUS_KEY, status.toString())
                .build();

        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }


    public URL getDeleteGrammarTaskURL(int task_id) {
        Uri uri = Uri.parse(URI + DELETE_GRAMMAR_TASK_PATH).buildUpon()
                .appendQueryParameter(TASK_ID_KEY, Integer.toString(task_id))
                .build();

        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }

    }

    public URL getAllGrammarTasksUrl(int user_id) {
        Uri uri = Uri.parse(URI + GET_ALL_GRAMMAR_TASKS_PATH).buildUpon()
                .appendQueryParameter(USER_ID_KEY, Integer.toString(user_id))
                .build();

        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }

    public URL getUploadGrammarTaskURL(String file_name) {

        Uri uri = Uri.parse(URI + UPLOAD_GRAMMAR_TASK_URL).buildUpon()
                .appendQueryParameter(FILE_NAME_KEY, file_name)
                .build();

        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }


    public URL getUpdateTimerURL(Time elapsed_time, int user_id, int task_id) {
        Uri uri = Uri.parse(URI + UPDATE_TIMER_PATH).buildUpon()
                .appendQueryParameter(ELAPSED_TIME_KEY, elapsed_time.toString())
                .appendQueryParameter(USER_ID_KEY, Integer.toString(user_id))
                .appendQueryParameter(TASK_ID_KEY, Integer.toString(task_id))
                .build();

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }

    public URL getUsersCountURL() {
        Uri uri = Uri.parse(URI + GET_USERS_COUNT_PATH);

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }

    public URL getTaskFlagURL(int user_id, int task_id) {
        Uri uri = Uri.parse(URI + GET_TASK_FLAG_PATH).buildUpon()
                .appendQueryParameter(USER_ID_KEY, Integer.toString(user_id))
                .appendQueryParameter(TASK_ID_KEY, Integer.toString(task_id))
                .build();

        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }

    public URL getSubmitAutomataTaskUrl(int user_id, int task_id, Task.TASK_STATUS status, Date submission_time) {
        Uri uri = Uri.parse(URI + SUBMIT_AUTOMATA_TASK_PATH).buildUpon()
                .appendQueryParameter(USER_ID_KEY, Integer.toString(user_id))
                .appendQueryParameter(TASK_ID_KEY, Integer.toString(task_id))
                .appendQueryParameter(TASK_STATUS_KEY, status.toString())
                .appendQueryParameter(SUBMISSION_TIME, submission_time.toString())
                .build();

        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }

    public URL getChangeFlagUrl(Task.TASK_STATUS status, int user_id, int task_id) {
        Uri uri = Uri.parse(URI + CHANGE_TASK_FLAG_PATH).buildUpon()
                .appendQueryParameter(USER_ID_KEY, Integer.toString(user_id))
                .appendQueryParameter(TASK_ID_KEY, Integer.toString(task_id))
                .appendQueryParameter(TASK_STATUS_KEY, status.toString())
                .build();

        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }

    public URL getDeleteAutomataTaskURL(int task_id) {
        Uri uri = Uri.parse(URI + DELETE_AUTOMATA_TASK_PATH).buildUpon()
                .appendQueryParameter(TASK_ID_KEY, Integer.toString(task_id))
                .build();

        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }

    public URL getAutomataTaskDownloadURL(int task_id, int user_id) {
        Uri uri = Uri.parse(URI + DOWNLOAD_AUTOMATA_TASK_PATH).buildUpon()
                .appendQueryParameter(TASK_ID_KEY, Integer.toString(task_id))
                .appendQueryParameter(USER_ID_KEY, Integer.toString(user_id))
                .build();

        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }

    public URL getLoginSaltUrl(String username) {
        Uri builtUri = Uri.parse(URI + GET_USER_SALT_PATH).buildUpon()
                .appendQueryParameter(USERNAME_QUERY_KEY, username)
                .build();

        URL url = null;


        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }

    public URL getLoginUrl(String username, String authkey) {
        Uri builtUri = Uri.parse(URI + LOGIN_PATH).buildUpon()
                .appendQueryParameter(USERNAME_QUERY_KEY, username)
                .appendQueryParameter(AUTHKEY_QUERY_KEY, authkey)
                .build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public URL getLoginUrlWithAuthkey(String username, String authkey) {
        Uri builtUri = Uri.parse(URI + LOGIN_PATH).buildUpon()
                .appendQueryParameter(USERNAME_QUERY_KEY, username)
                .appendQueryParameter(AUTHKEY_QUERY_KEY, authkey)
                .build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public URL getUpdateUserUrl(String auth_key, int logged_user_id, int user_id, String first_name, String last_name, String password_hash, String type, String username) {
        Uri builtUri = Uri.parse(URI + UPDATE_USER_PATH).buildUpon()
                .appendQueryParameter(AUTHKEY_QUERY_KEY, auth_key)
                .appendQueryParameter(LOGGED_USER_ID_KEY, Integer.toString(logged_user_id))
                .appendQueryParameter(USER_ID_KEY, Integer.toString(user_id))
                .appendQueryParameter(FIRST_NAME_KEY, first_name)
                .appendQueryParameter(LAST_NAME_KEY, last_name)
                .appendQueryParameter(PASSWORD_HASH_KEY, password_hash)
                .appendQueryParameter(TYPE_KEY, type)
                .appendQueryParameter(USERNAME_QUERY_KEY, username)
                .build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public URL getAllUsersUrl(String authkey, int offset) {
        Uri builtUri = Uri.parse(URI + GET_ALL_USERS_PATH).buildUpon()
                .appendQueryParameter(AUTHKEY_QUERY_KEY, authkey)
                .appendQueryParameter(OFFSET_QUERY_KEY, Integer.toString(offset))
                .build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public enum USER_ATTRIBUTE {
        FIRST_NAME,
        LAST_NAME,
        USERNAME
    }

    public URL getSearchedUser(String query, USER_ATTRIBUTE USERATTRIBUTE) {
        Uri builtUri = Uri.parse(URI + SEARCH_USER_PATH).buildUpon()
                .appendQueryParameter(QUERY_SEARCH_STRING_KEY, query)
                .appendQueryParameter(SEARCH_BY_KEY, USERATTRIBUTE.toString().toLowerCase())
                .build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public URL getChangePasswordUrl(int user_id, String new_password, String salt) {
        PasswordManager passwordManager = new PasswordManager();
        String new_auth = passwordManager.getAuthkey(new_password, salt);

        Uri builtUri = Uri.parse(URI + CHANGE_PASSWORD_PATH).buildUpon()
                .appendQueryParameter(USER_ID_KEY, Integer.toString(user_id))
                .appendQueryParameter(NEW_AUTH_KEY, new_auth)
                .build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public URL getOrderUserURL(USER_ATTRIBUTE user_attribute, boolean ascending) {
        Uri uri = Uri.parse(URI + ORDER_USERS_URL).buildUpon()
                .appendQueryParameter(ORDER_BY_KEY, user_attribute.toString().toLowerCase())
                .appendQueryParameter(ASCENDING_KEY, Boolean.toString(ascending).toLowerCase())
                .build();

        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }

    public URL getPublishAutomataTaskURL(String file_name) {

        Uri builtUri = Uri.parse(URI + PUBLISH_TASK_PATH).buildUpon()
                .appendQueryParameter(FILE_NAME_KEY, file_name)
                .build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public URL getDeleteUserUrl(int loggedUserID, int user_id, String auth_key) {

        Uri builtUri = Uri.parse(URI + DELETE_USER_PATH).buildUpon()
                .appendQueryParameter(AUTHKEY_QUERY_KEY, auth_key)
                .appendQueryParameter(USER_ID_KEY, Integer.toString(user_id))
                .appendQueryParameter(LOGGED_USER_ID_KEY, Integer.toString(loggedUserID))
                .build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public URL getFetchAllAutomataTasksUrl(int user_id, String auth_key) {

        Uri builtUri = Uri.parse(URI + GET_ALL_AUTOMATA_TASKS_PATH).buildUpon()
                .appendQueryParameter(AUTHKEY_QUERY_KEY, auth_key)
                .appendQueryParameter(USER_ID_KEY, Integer.toString(user_id))
                .build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public URL getPushGrammarTaskToTable(GrammarTask task, int user_id) {
        final int minutes = task.getAvailable_time().getMinutes();
        final String sTime = String.format("00:%02d:00", minutes);

        Uri builtUri = Uri.parse(URI + ADD_GRAMMAR_TASK_TO_TABLE).buildUpon()
                .appendQueryParameter(TASK_NAME_KEY, task.getTitle())
                .appendQueryParameter(TASK_DESCRIPTION_KEY, task.getText())
                .appendQueryParameter(TIME_KEY, sTime)
                .appendQueryParameter(PUBLIC_INPUT_KEY, Boolean.toString(task.isPublicInputs()))
                .appendQueryParameter(ASSIGNER_KEY, Integer.toString(user_id))
                .build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public URL getPushAutomataTaskToTable(Task task, int user_id, int machine_id) {
        String type = null;
        switch (machine_id) {
            case MainActivity
                    .FINITE_STATE_AUTOMATON:
                type = automata_type.FINITE_AUTOMATA.getApiKey();
                break;
            case MainActivity.PUSHDOWN_AUTOMATON:
                type = automata_type.PUSHDOWN_AUTOMATA.getApiKey();
                break;
            case MainActivity.LINEAR_BOUNDED_AUTOMATON:
                type = automata_type.LINEAR_BOUNDED_AUTOMATA.getApiKey();
                break;
            case MainActivity.TURING_MACHINE:
                type = automata_type.TURING_MACHINE.getApiKey();
                break;
        }

        final int minutes = task.getAvailable_time().getMinutes();
        final String sTime = String.format("00:%02d:00", minutes);

        Uri builtUri = Uri.parse(URI + ADD_TASK_TO_TABLE).buildUpon()
                .appendQueryParameter(TASK_NAME_KEY, task.getTitle())
                .appendQueryParameter(TASK_DESCRIPTION_KEY, task.getText())
                .appendQueryParameter(TIME_KEY, sTime)
                .appendQueryParameter(PUBLIC_INPUT_KEY, Boolean.toString(task.getPublicInputs()))
                .appendQueryParameter(ASSIGNER_KEY, Integer.toString(user_id))
                .appendQueryParameter(FILE_NAME_KEY, task.getTitle() + ".cmst")
                .appendQueryParameter(AUTOMATA_TYPE_KEY, type)
                .build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public URL getSaveTaskURL(String file_name, int user_id) {
        Uri uri = Uri.parse(URI + SAVE_AUTOMATA_TASK_PATH).buildUpon()
                .appendQueryParameter(USER_ID_KEY, Integer.toString(user_id))
                .appendQueryParameter(FILE_NAME_KEY, file_name)
                .build();

        URL url = null;

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            return url;
        }
    }


    public URL getAddUserUrl(String username, String first_name, String last_name, String authkey, User.user_type user_type, String salt) {
        PasswordManager passwordManager = new PasswordManager();

        String user_type_string = "";

        switch (user_type) {
            case admin:
                user_type_string = "admin";
                break;
            case lector:
                user_type_string = "lector";
                break;
            case student:
                user_type_string = "student";
                break;
        }

        if (authkey.length() % 2 != 0) {
            authkey = authkey + '0';
        }
        if (salt.length() % 2 != 0) {
            salt = salt + '0';
        }
        Uri builtUri = Uri.parse(URI + ADD_NEW_USER_PATH).buildUpon()
                .appendQueryParameter(FIRST_NAME_KEY, first_name)
                .appendQueryParameter(LAST_NAME_KEY, last_name)
                .appendQueryParameter(USERNAME_QUERY_KEY, username)
                .appendQueryParameter(AUTHKEY_QUERY_KEY, authkey)
                .appendQueryParameter(USER_TYPE_KEY, user_type_string)
                .appendQueryParameter(SALT_KEY, salt)
                .build();

        URL builtURL = null;

        try {
            builtURL = new URL(builtUri.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builtURL;
    }


}
