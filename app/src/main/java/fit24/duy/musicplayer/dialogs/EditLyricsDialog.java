package fit24.duy.musicplayer.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import fit24.duy.musicplayer.R;

public class EditLyricsDialog extends DialogFragment {
    private EditLyricsListener listener;
    private String currentLyrics;
    private String currentLanguage;

    public interface EditLyricsListener {
        void onLyricsSaved(String lyrics, String language);
    }

    public static EditLyricsDialog newInstance(String currentLyrics, String currentLanguage) {
        EditLyricsDialog dialog = new EditLyricsDialog();
        Bundle args = new Bundle();
        args.putString("lyrics", currentLyrics);
        args.putString("language", currentLanguage);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (EditLyricsListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement EditLyricsListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            currentLyrics = getArguments().getString("lyrics", "");
            currentLanguage = getArguments().getString("language", "vi");
        }

        View view = getLayoutInflater().inflate(R.layout.dialog_edit_lyrics, null);
        EditText lyricsEditText = view.findViewById(R.id.lyricsEditText);
        Spinner languageSpinner = view.findViewById(R.id.languageSpinner);

        // Thiết lập spinner ngôn ngữ
        String[] languages = {"Tiếng Việt", "English", "한국어", "日本語", "中文"};
        String[] languageCodes = {"vi", "en", "ko", "ja", "zh"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        // Chọn ngôn ngữ hiện tại
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLanguage)) {
                languageSpinner.setSelection(i);
                break;
            }
        }

        lyricsEditText.setText(currentLyrics);

        return new AlertDialog.Builder(requireContext())
                .setTitle("Chỉnh sửa lời bài hát")
                .setView(view)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newLyrics = lyricsEditText.getText().toString();
                    String selectedLanguage = languageCodes[languageSpinner.getSelectedItemPosition()];
                    listener.onLyricsSaved(newLyrics, selectedLanguage);
                })
                .setNegativeButton("Hủy", null)
                .create();
    }
} 