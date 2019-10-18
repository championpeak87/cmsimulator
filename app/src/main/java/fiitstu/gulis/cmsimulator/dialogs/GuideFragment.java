package fiitstu.gulis.cmsimulator.dialogs;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ScrollView;
import fiitstu.gulis.cmsimulator.R;

/**
 * A fragment that displays the user guide.
 *
 * Created by Jakub Sedlář on 25.01.2018.
 */
public class GuideFragment extends DialogFragment {

    //log tag
    private static final String TAG = GuideFragment.class.getName();

    public static final String PAGE = "PAGE";

    public static final int CONTENTS = 0;
    public static final int ABOUT = 1;
    public static final int CREDITS = 2;
    public static final int GETTING_STARTED = 3;
    public static final int CONFIGURATION = 4;
    public static final int SPECIAL_SYMBOLS = 5;
    public static final int SIMULATION = 6;
    public static final int BULK_TEST = 7;
    public static final int SAVE_LOAD = 8;
    public static final int TASKS = 9;
    public static final int SOLVING_TASKS = 10;
    public static final int CREATING_TASKS = 11;
    public static final int SETTINGS = 12;
    public static final int GRAMMAR = 13;
    public static final int SIMULATION_GRAMMAR = 14;
    public static final int MULTIPLE_TESTS = 15;

    private ImageButton prevPageButton;
    private ImageButton nextPageButton;

    private ScrollView contentScrollView;

    /**
     * Creates a new GuideFragment
     * @param page the page to be shown (a static member of this class)
     * @return the created instance
     */
    public static GuideFragment newInstance(int page) {
        GuideFragment guideFragment = new GuideFragment();
        Bundle args = new Bundle();
        args.putInt(PAGE, page);
        guideFragment.setArguments(args);

        return guideFragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            d.setCancelable(true);
            d.setTitle(getContext().getResources().getString(R.string.help));
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.light_dialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guide, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prevPageButton = view.findViewById(R.id.imageButton_guide_previous);
        nextPageButton = view.findViewById(R.id.imageButton_guide_next);
        contentScrollView = view.findViewById(R.id.scrollView_guide_content);

        Bundle args = getArguments();
        int page = args == null ? CONTENTS : args.getInt(PAGE, CONTENTS);

        switch (page) {
            case CONTENTS:
                showContents(view);
                break;
            case ABOUT:
                showAbout(view);
                break;
            case CREDITS:
                showCredits(view);
                break;
            case GETTING_STARTED:
                showGettingStarted(view);
                break;
            case CONFIGURATION:
                showConfiguration(view);
                break;
            case SPECIAL_SYMBOLS:
                showSpecialSymbols(view);
                break;
            case SIMULATION:
                showSimulation(view);
                break;
            case BULK_TEST:
                showBulkTest(view);
                break;
            case SAVE_LOAD:
                showSaveLoad(view);
                break;
            case TASKS:
                showTasks(view);
                break;
            case SOLVING_TASKS:
                showSolvingTasks(view);
                break;
            case CREATING_TASKS:
                showCreatingTasks(view);
                break;
            case SETTINGS:
                showSettings(view);
                break;
            case GRAMMAR:
                showGrammar(view);
                break;
            case SIMULATION_GRAMMAR:
                showSimulationGrammar(view);
                break;
            case MULTIPLE_TESTS:
                showMultipleTests(view);
                break;
        }
    }

    /**
     * Finds cross-references in the view and sets their OnClickListeners to show the appropriate page
     * @param view the view that contains the GuideFragment
     */
    private void activateLinks(final View view) {
        View link;

        link = view.findViewById(R.id.linearLayout_guide_about_link);
        if (link != null) {
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAbout(view);
                }
            });
        }

        link = view.findViewById(R.id.linearLayout_guide_credits_link);
        if (link != null) {
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCredits(view);
                }
            });
        }

        link = view.findViewById(R.id.linearLayout_guide_getting_started_link);
        if (link != null) {
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showGettingStarted(view);
                }
            });
        }

        link = view.findViewById(R.id.linearLayout_guide_special_symbols_link);
        if (link != null) {
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSpecialSymbols(view);
                }
            });
        }

        link = view.findViewById(R.id.linearLayout_guide_configuration_link);
        if (link != null) {
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showConfiguration(view);
                }
            });
        }

        link = view.findViewById(R.id.linearLayout_guide_simulation_link);
        if (link != null) {
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSimulation(view);
                }
            });
        }

        link = view.findViewById(R.id.linearLayout_guide_bulk_test_link);
        if (link != null) {
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showBulkTest(view);
                }
            });
        }

        link = view.findViewById(R.id.linearLayout_guide_save_load_link);
        if (link != null) {
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSaveLoad(view);
                }
            });
        }

        link = view.findViewById(R.id.linearLayout_guide_tasks_link);
        if (link != null) {
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTasks(view);
                }
            });
        }

        link = view.findViewById(R.id.linearLayout_guide_solving_tasks_link);
        if (link != null) {
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSolvingTasks(view);
                }
            });
        }

        link = view.findViewById(R.id.linearLayout_guide_creating_tasks_link);
        if (link != null) {
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCreatingTasks(view);
                }
            });
        }

        link = view.findViewById(R.id.linearLayout_guide_settings_link);
        if (link != null) {
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSettings(view);
                }
            });
        }

        link = view.findViewById(R.id.linearLayout_guide_grammar_link);
        if (link != null){
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showGrammar(view);
                }
            });
        }

        link = view.findViewById(R.id.linearLayout_guide_grammar_simulation_link);
        if (link != null){
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSimulationGrammar(view);
                }
            });
        }

        link = view.findViewById(R.id.linearLayout_guide_multiple_tests_link);
        if (link != null){
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMultipleTests(view);
                }
            });
        }

        link = view.findViewById(R.id.linearLayout_guide_grammar_special_symbols_link);
        if (link != null){
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showGrammarSpecialSymbols(view);
                }
            });
        }

        link = view.findViewById(R.id.linearLayout_guide_grammar_load_save_link);
        if(link != null){
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showGrammarLoadSave(view);
                }
            });
        }

    }

    private void showContents(final View view) {
        prevPageButton.setVisibility(View.INVISIBLE);
        nextPageButton.setVisibility(View.VISIBLE);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAbout(view);
            }
        });
        resetScrollView();
        ScrollView.inflate(contentScrollView.getContext(), R.layout.guide_contents, contentScrollView);
        activateLinks(view);
    }

    private void showAbout(final View view) {
        prevPageButton.setVisibility(View.VISIBLE);
        prevPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showContents(view);
            }
        });
        nextPageButton.setVisibility(View.VISIBLE);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCredits(view);
            }
        });
        resetScrollView();
        ScrollView.inflate(contentScrollView.getContext(), R.layout.guide_about, contentScrollView);
        activateLinks(view);
    }

    private void showCredits(final View view) {
        prevPageButton.setVisibility(View.VISIBLE);
        prevPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAbout(view);
            }
        });
        nextPageButton.setVisibility(View.VISIBLE);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGettingStarted(view);
            }
        });
        resetScrollView();
        ScrollView.inflate(contentScrollView.getContext(), R.layout.guide_credits, contentScrollView);
        activateLinks(view);
    }

    private void showGettingStarted(final View view) {
        prevPageButton.setVisibility(View.VISIBLE);
        prevPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCredits(view);
            }
        });
        nextPageButton.setVisibility(View.VISIBLE);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfiguration(view);
            }
        });
        resetScrollView();
        ScrollView.inflate(contentScrollView.getContext(), R.layout.guide_getting_started, contentScrollView);
        activateLinks(view);
    }

    private void showConfiguration(final View view) {
        prevPageButton.setVisibility(View.VISIBLE);
        prevPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGettingStarted(view);
            }
        });
        nextPageButton.setVisibility(View.VISIBLE);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSpecialSymbols(view);
            }
        });
        resetScrollView();
        ScrollView.inflate(contentScrollView.getContext(), R.layout.guide_configuration, contentScrollView);
        activateLinks(view);
    }

    private void showSpecialSymbols(final View view) {
        prevPageButton.setVisibility(View.VISIBLE);
        prevPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfiguration(view);
            }
        });
        nextPageButton.setVisibility(View.VISIBLE);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSimulation(view);
            }
        });
        resetScrollView();
        ScrollView.inflate(contentScrollView.getContext(), R.layout.guide_special_symbols, contentScrollView);
        activateLinks(view);
    }

    private void showSimulation(final View view) {
        prevPageButton.setVisibility(View.VISIBLE);
        prevPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSpecialSymbols(view);
            }
        });
        nextPageButton.setVisibility(View.VISIBLE);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBulkTest(view);
            }
        });
        resetScrollView();
        ScrollView.inflate(contentScrollView.getContext(), R.layout.guide_simulation, contentScrollView);
        activateLinks(view);
    }

    private void showBulkTest(final View view) {
        prevPageButton.setVisibility(View.VISIBLE);
        prevPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSimulation(view);
            }
        });
        nextPageButton.setVisibility(View.VISIBLE);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSaveLoad(view);
            }
        });
        resetScrollView();
        ScrollView.inflate(contentScrollView.getContext(), R.layout.guide_bulktest, contentScrollView);
        activateLinks(view);
    }

    private void showSaveLoad(final View view) {
        prevPageButton.setVisibility(View.VISIBLE);
        prevPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBulkTest(view);
            }
        });
        nextPageButton.setVisibility(View.VISIBLE);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTasks(view);
            }
        });
        resetScrollView();
        ScrollView.inflate(contentScrollView.getContext(), R.layout.guide_save_load, contentScrollView);
        activateLinks(view);
    }

    private void showTasks(final View view) {
        prevPageButton.setVisibility(View.VISIBLE);
        prevPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSaveLoad(view);
            }
        });
        nextPageButton.setVisibility(View.VISIBLE);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSolvingTasks(view);
            }
        });
        resetScrollView();
        ScrollView.inflate(contentScrollView.getContext(), R.layout.guide_tasks, contentScrollView);
        activateLinks(view);
    }

    private void showSolvingTasks(final View view) {
        prevPageButton.setVisibility(View.VISIBLE);
        prevPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTasks(view);
            }
        });
        nextPageButton.setVisibility(View.VISIBLE);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreatingTasks(view);
            }
        });
        resetScrollView();
        ScrollView.inflate(contentScrollView.getContext(), R.layout.guide_solving_tasks, contentScrollView);
        activateLinks(view);
    }

    private void showCreatingTasks(final View view) {
        prevPageButton.setVisibility(View.VISIBLE);
        prevPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSolvingTasks(view);
            }
        });
        nextPageButton.setVisibility(View.VISIBLE);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGrammar(view);
            }
        });
        resetScrollView();
        ScrollView.inflate(contentScrollView.getContext(), R.layout.guide_creating_tasks, contentScrollView);
        activateLinks(view);
    }

    private void showSettings(final View view) {
        prevPageButton.setVisibility(View.VISIBLE);
        prevPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGrammarLoadSave(view);
            }
        });
        nextPageButton.setVisibility(View.INVISIBLE);
        resetScrollView();
        ScrollView.inflate(contentScrollView.getContext(), R.layout.guide_settings, contentScrollView);
        activateLinks(view);
    }

    private void showGrammar(final View view){
        prevPageButton.setVisibility(View.VISIBLE);
        prevPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreatingTasks(view);
            }
        });
        nextPageButton.setVisibility(View.VISIBLE);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSimulationGrammar(view);
            }
        });
        resetScrollView();
        ScrollView.inflate(contentScrollView.getContext(), R.layout.guide_grammar, contentScrollView);
        activateLinks(view);
    }

    private void showSimulationGrammar(final View view){
        prevPageButton.setVisibility(View.VISIBLE);
        prevPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGrammar(view);
            }
        });
        nextPageButton.setVisibility(View.VISIBLE);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMultipleTests(view);
            }
        });
        resetScrollView();
        ScrollView.inflate(contentScrollView.getContext(), R.layout.guide_simulation_grammar, contentScrollView);
        activateLinks(view);
    }

    private void showMultipleTests(final View view){
        prevPageButton.setVisibility(View.VISIBLE);
        prevPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSimulationGrammar(view);
            }
        });
        nextPageButton.setVisibility(View.VISIBLE);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGrammarSpecialSymbols(view);
            }
        });
        resetScrollView();
        ScrollView.inflate(contentScrollView.getContext(), R.layout.guide_multiple_tests, contentScrollView);
        activateLinks(view);
    }

    private void showGrammarSpecialSymbols(final View view){
        prevPageButton.setVisibility(View.VISIBLE);
        prevPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMultipleTests(view);
            }
        });
        nextPageButton.setVisibility(View.VISIBLE);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGrammarLoadSave(view);
            }
        });
        resetScrollView();
        ScrollView.inflate(contentScrollView.getContext(), R.layout.guide_grammar_special_symbols, contentScrollView);
        activateLinks(view);
    }

    private void showGrammarLoadSave(final View view){
        prevPageButton.setVisibility(View.VISIBLE);
        prevPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGrammarSpecialSymbols(view);
            }
        });
        nextPageButton.setVisibility(View.VISIBLE);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettings(view);
            }
        });
        resetScrollView();
        ScrollView.inflate(contentScrollView.getContext(), R.layout.guide_grammar_load_save, contentScrollView);
        activateLinks(view);
    }

    /**
     * Clears the content of the scrollView and scrolls it to top, preparing it
     * to display a new page
     */
    private void resetScrollView() {
        contentScrollView.scrollTo(0, 0);
        contentScrollView.removeAllViews();
    }
}
