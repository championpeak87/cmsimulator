package fiitstu.gulis.cmsimulator.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.adapters.configuration.StackAlphabetAdapter;
import fiitstu.gulis.cmsimulator.database.DataSource;
import fiitstu.gulis.cmsimulator.elements.Symbol;

import java.util.List;
import java.util.Stack;

@SuppressLint("ValidFragment")
public class GameStackAlphabetDialog extends DialogFragment {

    private RecyclerView recyclerview_stack_alphabet;
    private EditText edittext_new_symbol;
    private ImageButton imagebutton_add_new_stack_symbol;

    private StackAlphabetAdapter adapter;
    private List<Symbol> stackAlphabet;

    @SuppressLint("ValidFragment")
    public GameStackAlphabetDialog(List<Symbol> stackAlphabet) {
        this.stackAlphabet = stackAlphabet;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View view = layoutInflater.inflate(R.layout.dialog_chess_game_stack_alphabet, null, false);

        adapter = new StackAlphabetAdapter(stackAlphabet, getContext());

        recyclerview_stack_alphabet = view.findViewById(R.id.recyclerview_stack_alphabet);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerview_stack_alphabet.setAdapter(adapter);
        recyclerview_stack_alphabet.setLayoutManager(linearLayoutManager);
        Animation showUpAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.item_show_animation);
        recyclerview_stack_alphabet.setAnimation(showUpAnimation);

        edittext_new_symbol = view.findViewById(R.id.edittext_new_symbol);
        imagebutton_add_new_stack_symbol = view.findViewById(R.id.imagebutton_add_new_stack_symbol);
        imagebutton_add_new_stack_symbol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newStackSymbolValue = edittext_new_symbol.getText().toString().trim();
                for (Symbol s : stackAlphabet) {
                    final String currentValue = s.getValue();
                    if (newStackSymbolValue.equals(currentValue)) {
                        Toast.makeText(getContext(), R.string.symbol_duplicity, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                DataSource dataSource = DataSource.getInstance();
                dataSource.open();
                Symbol newSymbol = dataSource.addStackSymbol(newStackSymbolValue, 0);
                adapter.addSymbol(newSymbol);
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.stack_alphabet)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .create();

        return dialog;
    }
}
