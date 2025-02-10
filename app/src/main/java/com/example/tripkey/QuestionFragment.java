package com.example.tripkey;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.tripkey.QuestionData;
import com.example.tripkey.Question;
import java.util.List;

public class QuestionFragment extends Fragment {
    private static final String ARG_START_INDEX = "start_index";
    private List<Question> questions;
    private int startIndex;
    private Button[] selectedButtons = new Button[4];

    public static QuestionFragment newInstance(int startIndex) {
        QuestionFragment fragment = new QuestionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_START_INDEX, startIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            startIndex = getArguments().getInt(ARG_START_INDEX);
        }
        questions = QuestionData.getQuestions();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question, container, false);

        for (int i = 0; i < 4; i++) {
            int questionIndex = startIndex + i;
            if (questionIndex >= questions.size()) break;

            Question q = questions.get(questionIndex);
            int qId = getResources().getIdentifier("question" + i, "id", requireContext().getPackageName());
            int btn1Id = getResources().getIdentifier("btn" + i + "_1", "id", requireContext().getPackageName());
            int btn2Id = getResources().getIdentifier("btn" + i + "_2", "id", requireContext().getPackageName());

            TextView questionView = view.findViewById(qId);
            Button btn1 = view.findViewById(btn1Id);
            Button btn2 = view.findViewById(btn2Id);

            questionView.setText(q.getQuestionText());
            btn1.setText(q.getOption1());
            btn2.setText(q.getOption2());

            int finalI = i;
            View.OnClickListener listener = v -> {
                if (selectedButtons[finalI] != null) {
                    selectedButtons[finalI].setBackgroundColor(Color.LTGRAY);
                }
                selectedButtons[finalI] = (Button) v;
                v.setBackgroundColor(Color.CYAN);
            };

            btn1.setOnClickListener(listener);
            btn2.setOnClickListener(listener);
        }

        return view;
    }
}
