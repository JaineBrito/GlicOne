package com.example.glicone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

public class ConfigFragment extends Fragment {

    private Switch switchModoEscuro;
    private Button btnLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_config, container, false);

        switchModoEscuro = view.findViewById(R.id.switchModoEscuro);
        btnLogout = view.findViewById(R.id.btnLogout);

        SharedPreferences preferences = getActivity().getSharedPreferences("AppSettings", getContext().MODE_PRIVATE);
        boolean isNightMode = preferences.getBoolean("night_mode", false);
        switchModoEscuro.setChecked(isNightMode);

        switchModoEscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("night_mode", isChecked);
            editor.apply();
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences userPreferences = getActivity().getSharedPreferences("UserSession", getContext().MODE_PRIVATE);
            SharedPreferences.Editor editor = userPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }
}