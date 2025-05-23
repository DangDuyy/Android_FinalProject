package fit24.duy.musicplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "MusicPlayerSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_PROFILE_IMAGE = "profileImage";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String userId, String username, String email, String token, int status) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TOKEN, token);
        editor.putInt("userStatus", status);  // Lưu status vào SharedPreferences
        editor.commit();

        Log.d(TAG, "User login session created for: " + username);
    }

    public void createLoginSessionWithProfile(String userId, String username, String email, String token, String profileImage, int status) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_PROFILE_IMAGE, profileImage);
        editor.putInt("userStatus", status);  // Lưu status vào SharedPreferences
        editor.commit();

        Log.d(TAG, "User login session created with profile for: " + username);
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, null);
    }

    public String getToken() {
        return pref.getString(KEY_TOKEN, null);
    }

    public String getProfileImage() {
        return pref.getString(KEY_PROFILE_IMAGE, null);
    }

    public int getUserStatus() {
        return pref.getInt("userStatus", -1); // Lấy status từ SharedPreferences
    }

    public void setUserStatus(int status) {
        editor.putInt("userStatus", status);
        editor.commit(); // hoặc dùng apply() nếu bạn muốn ghi bất đồng bộ
        Log.d(TAG, "User status updated to: " + status);
    }

    public void saveProfileImage(String profileImage) {
        editor.putString(KEY_PROFILE_IMAGE, profileImage);
        editor.apply();
        Log.d(TAG, "Profile image updated: " + profileImage);
    }

    public void logout() {
        editor.clear();
        editor.commit();
        Log.d(TAG, "User session cleared - logged out");
    }
}
