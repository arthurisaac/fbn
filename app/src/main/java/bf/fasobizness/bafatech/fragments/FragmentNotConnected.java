package bf.fasobizness.bafatech.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Objects;

import bf.fasobizness.bafatech.R;
import bf.fasobizness.bafatech.activities.user.LoginActivity;


public class FragmentNotConnected extends AppCompatDialogFragment {

    public FragmentNotConnected() {
        // Required empty public constructor
    }

    public static FragmentNotConnected newInstance() {
        return new FragmentNotConnected();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.fragment_not_connected, null);
        Button btn_continuer = view.findViewById(R.id.btn_continuer);
        btn_continuer.setOnClickListener(v -> startActivity(new Intent(getContext(), LoginActivity.class)));

        builder.setView(view);
        return builder.create();
    }
}
