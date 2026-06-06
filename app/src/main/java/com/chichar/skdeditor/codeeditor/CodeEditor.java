package com.chichar.skdeditor.codeeditor;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.AttributeSet;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chichar.skdeditor.R;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeEditor extends FrameLayout {

    private RememberCursorEditText editText;
    private TextView lineNumbers;

    private int currentMatchedIndex = -1;
    private final ArrayList<Integer[]> results = new ArrayList<>(); // start,end pairs
    private Pattern lastPattern = null;

    public CodeEditor(Context context) {
        super(context);
        init(context);
    }

    public CodeEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CodeEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_code_editor, this, true);
        editText = findViewById(R.id.editorText);
        lineNumbers = findViewById(R.id.lineNumbers);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateLineNumbers();
                // if text changed, re-run last pattern to keep highlights in sync
                if (lastPattern != null) {
                    findMatches(lastPattern);
                }
            }
        });
    }

    public String getText() {
        if (editText == null) return "";
        return editText.getText().toString();
    }

    public void setText(String text) {
        if (editText == null) return;
        editText.setText(text);
        updateLineNumbers();
        clearMatches();
    }

    private void updateLineNumbers() {
        if (editText == null || lineNumbers == null) return;
        String txt = editText.getText().toString();
        // preserve trailing newline count
        int lines = 1;
        if (!txt.isEmpty()) {
            String[] arr = txt.split("\\n", -1);
            lines = arr.length;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines; i++) {
            sb.append(i);
            if (i != lines) sb.append('\n');
        }
        lineNumbers.setText(sb.toString());
    }

    public ArrayList<Integer> findMatches(Pattern pattern) {
        results.clear();
        if (pattern == null) return new ArrayList<>();
        String text = getText();
        if (text.isEmpty()) return new ArrayList<>();
        lastPattern = pattern;

        Matcher matcher = pattern.matcher(text);
        Spannable spannable = editText.getText();
        // clear existing highlight spans
        BackgroundColorSpan[] spans = spannable.getSpans(0, spannable.length(), BackgroundColorSpan.class);
        for (BackgroundColorSpan span : spans) {
            spannable.removeSpan(span);
        }

        ArrayList<Integer> ret = new ArrayList<>();
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            results.add(new Integer[]{start, end});
            ret.add(start);
            try {
                spannable.setSpan(new BackgroundColorSpan(0xFFBB86FC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (Exception ignored) {
            }
        }

        if (results.isEmpty()) {
            // leave lastPattern set but show nothing
            return ret;
        }
        currentMatchedIndex = -1;
        findNextMatch();
        return ret;
    }

    public void findNextMatch() {
        if (results.isEmpty()) {
            Toast.makeText(getContext(), "Nothing found", Toast.LENGTH_SHORT).show();
            return;
        }
        currentMatchedIndex++;
        if (currentMatchedIndex >= results.size()) {
            currentMatchedIndex = -1;
            Toast.makeText(getContext(), "Reached end of the document", Toast.LENGTH_SHORT).show();
            findNextMatch();
            return;
        }

        Integer[] range = results.get(currentMatchedIndex);
        if (range == null) return;
        int start = range[0];
        int end = range[1];
        try {
            editText.requestFocus();
            editText.post(() -> {
                try {
                    editText.setSelection(start, end);
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }

    public void findPrevMatch() {
        if (results.isEmpty()) {
            Toast.makeText(getContext(), "Nothing found", Toast.LENGTH_SHORT).show();
            return;
        }
        currentMatchedIndex--;
        if (currentMatchedIndex < 0) {
            Toast.makeText(getContext(), "Reached start of the document", Toast.LENGTH_SHORT).show();
            currentMatchedIndex = results.size() - 1;
        }
        Integer[] range = results.get(currentMatchedIndex);
        if (range == null) return;
        int start = range[0];
        int end = range[1];
        try {
            editText.requestFocus();
            editText.post(() -> {
                try {
                    editText.setSelection(start, end);
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }

    public void clearMatches() {
        currentMatchedIndex = -1;
        results.clear();
        Spannable spannable = editText.getText();
        BackgroundColorSpan[] spans = spannable.getSpans(0, spannable.length(), BackgroundColorSpan.class);
        for (BackgroundColorSpan span : spans) {
            spannable.removeSpan(span);
        }
    }

    public boolean replaceAllMatches(Pattern pattern, String replacement) {
        try {
            String txt = getText();
            Matcher matcher = pattern.matcher(txt);
            if (!matcher.find()) return false;
            String replaced = matcher.replaceAll(replacement);
            setText(replaced);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void removeMatch(Integer index) {
        // Re-run last pattern to refresh highlights after edits
        if (lastPattern != null) findMatches(lastPattern);
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        if (editText != null) return editText.requestFocus(direction, previouslyFocusedRect);
        return super.requestFocus(direction, previouslyFocusedRect);
    }
}