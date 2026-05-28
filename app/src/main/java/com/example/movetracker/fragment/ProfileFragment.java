package com.example.movetracker.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.movetracker.LoginActivity;
import com.example.movetracker.R;

public class ProfileFragment extends Fragment {
    private TextView tvNickname;
    private Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        tvNickname = view.findViewById(R.id.tvNickname);
        btnLogout = view.findViewById(R.id.btnLogout);

        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String nickname = prefs.getString("nickname", "用户");
        tvNickname.setText("昵称：" + nickname);

        btnLogout.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
        });
        return view;
    }
}