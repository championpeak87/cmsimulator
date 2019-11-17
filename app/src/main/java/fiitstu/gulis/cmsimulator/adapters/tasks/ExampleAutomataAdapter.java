package fiitstu.gulis.cmsimulator.adapters.tasks;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.activities.MainActivity;
import fiitstu.gulis.cmsimulator.activities.SimulationActivity;
import fiitstu.gulis.cmsimulator.models.tasks.automata_tasks.*;
import fiitstu.gulis.cmsimulator.models.tasks.deterministic;

import java.util.ArrayList;
import java.util.List;

public class ExampleAutomataAdapter extends RecyclerView.Adapter<ExampleAutomataAdapter.CardViewBuilder> {

    private Context mContext;
    private List<AutomataExampleTask> listOfTasks;

    public ExampleAutomataAdapter(Context mContext) {
        this.mContext = mContext;

        this.listOfTasks = new ArrayList<>();
        listOfTasks.add(
                new AutomataExampleTask(
                        mContext.getString(R.string.finite_state_automaton_example1),
                        mContext.getString(R.string.example_automata_description1),
                        deterministic.DETERMINISTIC,
                        new FiniteAutomataTask()
                        ));

        listOfTasks.add(
                new AutomataExampleTask(
                        mContext.getString(R.string.finite_state_automaton_example2),
                        mContext.getString(R.string.example_automata_description2),
                        deterministic.DETERMINISTIC,
                        new FiniteAutomataTask()
                        ));
        listOfTasks.add(
                new AutomataExampleTask(
                        mContext.getString(R.string.finite_state_automaton_example3),
                        mContext.getString(R.string.example_automata_description3),
                        deterministic.NONDETERMINISTIC,
                        new FiniteAutomataTask()));
        listOfTasks.add(
                new AutomataExampleTask(
                        mContext.getString(R.string.pushdown_automaton_example1),
                        mContext.getString(R.string.example_automata_description4),
                        deterministic.DETERMINISTIC,
                        new PushdownAutomataTask()));
        listOfTasks.add(
                new AutomataExampleTask(
                        mContext.getString(R.string.pushdown_automaton_example2),
                        mContext.getString(R.string.example_automata_description5),
                        deterministic.DETERMINISTIC,
                        new PushdownAutomataTask()));
        listOfTasks.add(
                new AutomataExampleTask(
                        mContext.getString(R.string.pushdown_automaton_example3),
                        mContext.getString(R.string.example_automata_description6),
                        deterministic.NONDETERMINISTIC,
                        new PushdownAutomataTask()));
        listOfTasks.add(
                new AutomataExampleTask(
                        mContext.getString(R.string.linear_bounded_automaton_example1),
                        mContext.getString(R.string.example_automata_description7),
                        deterministic.DETERMINISTIC,
                        new LinearBoundedAutomataTask()));
        listOfTasks.add(
                new AutomataExampleTask(
                        mContext.getString(R.string.linear_bounded_automaton_example2),
                        mContext.getString(R.string.example_automata_description8),
                        deterministic.NONDETERMINISTIC,
                        new LinearBoundedAutomataTask()));
        listOfTasks.add(
                new AutomataExampleTask(
                        mContext.getString(R.string.turing_machine_example1),
                        mContext.getString(R.string.example_automata_description9),
                        deterministic.DETERMINISTIC,
                        new TuringMachineTask()));
        listOfTasks.add(
                new AutomataExampleTask(
                        mContext.getString(R.string.turing_machine_example2),
                        mContext.getString(R.string.example_automata_description10),
                        deterministic.NONDETERMINISTIC,
                        new TuringMachineTask()));
    }

    @NonNull
    @Override
    public CardViewBuilder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.list_view_task_example_item, parent, false);

        return new CardViewBuilder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewBuilder holder, final int position) {
        final AutomataExampleTask currentTask = listOfTasks.get(position);
        Animation showUpAnimation = AnimationUtils.loadAnimation(mContext, R.anim.item_show_animation);

        holder.cardView.setAnimation(showUpAnimation);

        if (currentTask.getTask_type() instanceof FiniteAutomataTask) {
            holder.automataType.setText(R.string.finite_state_automaton);
        } else if (currentTask.getTask_type() instanceof PushdownAutomataTask) {
            holder.automataType.setText(R.string.pushdown_automaton);
        } else if (currentTask.getTask_type() instanceof LinearBoundedAutomataTask) {
            holder.automataType.setText(R.string.linear_bounded_automaton);
        } else if (currentTask.getTask_type() instanceof TuringMachineTask) {
            holder.automataType.setText(R.string.turing_machine);
        }

        String task_name = currentTask.getTask_name();
        holder.task_name.setText(task_name);

        holder.determinism.setText(currentTask.getDeterminism().toString());

        final int blue = mContext.getColor(R.color.primary_color);
        final int light_blue = mContext.getColor(R.color.primary_color_light);
        holder.topBar.setBackgroundColor(blue);
        holder.bottomBar.setBackgroundColor(light_blue);
        holder.determinism.setBackgroundColor(light_blue);

        holder.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle outputBundle = new Bundle();
                switch (position) {
                    case 0:
                        outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE1);
                        outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.FINITE_STATE_AUTOMATON);
                        break;
                    case 1:
                        outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE2);
                        outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.FINITE_STATE_AUTOMATON);
                        break;
                    case 2:
                        outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE3);
                        outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.FINITE_STATE_AUTOMATON);
                        break;
                    case 3:
                        outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE1);
                        outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.PUSHDOWN_AUTOMATON);
                        break;
                    case 4:
                        outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE2);
                        outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.PUSHDOWN_AUTOMATON);
                        break;
                    case 5:
                        outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE3);
                        outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.PUSHDOWN_AUTOMATON);
                        break;
                    case 6:
                        outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE1);
                        outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.LINEAR_BOUNDED_AUTOMATON);
                        break;
                    case 7:
                        outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE2);
                        outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.LINEAR_BOUNDED_AUTOMATON);
                        break;
                    case 8:
                        outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE1);
                        outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.TURING_MACHINE);
                        break;
                    case 9:
                        outputBundle.putInt(MainActivity.CONFIGURATION_TYPE, MainActivity.EXAMPLE_MACHINE2);
                        outputBundle.putInt(MainActivity.MACHINE_TYPE, MainActivity.TURING_MACHINE);
                        break;
                }
                Intent simulation = new Intent(mContext, SimulationActivity.class);
                simulation.putExtras(outputBundle);
                mContext.startActivity(simulation);
            }
        });

        holder.hintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.task_hint)
                        .setMessage(currentTask.getTask_description())
                        .setPositiveButton("OK", null)
                        .show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return listOfTasks.size();
    }

    public static class CardViewBuilder extends RecyclerView.ViewHolder {
        TextView automataType;
        TextView task_name;
        TextView determinism;
        LinearLayout bottomBar;
        TextView topBar;
        CardView cardView;
        Button startButton;
        Button hintButton;


        public CardViewBuilder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardview_task);
            automataType = itemView.findViewById(R.id.textview_automata_type);
            task_name = itemView.findViewById(R.id.textview_task_name);
            bottomBar = itemView.findViewById(R.id.task_bottom_bar);
            topBar = itemView.findViewById(R.id.textview_automata_type);
            determinism = itemView.findViewById(R.id.textview_automata_type_deterministic);
            startButton = itemView.findViewById(R.id.button_start_task);
            hintButton = itemView.findViewById(R.id.button_task_hint);
            determinism = itemView.findViewById(R.id.textview_automata_type_deterministic);

        }
    }

}
