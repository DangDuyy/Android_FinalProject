package fit24.duy.musicplayer.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.api.ApiClient;
import fit24.duy.musicplayer.api.ApiService;
import fit24.duy.musicplayer.models.UserResponse;
import fit24.duy.musicplayer.utils.SessionManager;
import fit24.duy.musicplayer.utils.UrlUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.File;

public class EditProfileFragment extends Fragment {

    private static final String TAG = "EditProfileFragment";

    private de.hdodenhof.circleimageview.CircleImageView editProfileImage;
    private EditText editId, editUsername, editEmail;
    private Button saveChangesButton;
    private ApiService apiService;
    private SessionManager sessionManager;
    private Uri selectedImageUri;
    private ActivityResultLauncher<String> pickImageLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        // Initialize views
        editProfileImage = view.findViewById(R.id.edit_profile_image);
        editId = view.findViewById(R.id.edit_id);
        editUsername = view.findViewById(R.id.edit_username);
        editEmail = view.findViewById(R.id.edit_email);
        saveChangesButton = view.findViewById(R.id.save_button);

        // Initialize SessionManager
        sessionManager = new SessionManager(requireActivity());

        // Khởi tạo ApiService
        apiService = ApiClient.getClient().create(ApiService.class);

        // Load user data from SessionManager
        String userId = sessionManager.getUserId() != null ? sessionManager.getUserId() : "Unknown";
        String username = sessionManager.getUsername() != null ? sessionManager.getUsername() : "Unknown";
        String email = sessionManager.getEmail() != null ? sessionManager.getEmail() : "No email";
        String profileImage = sessionManager.getProfileImage();

        // Update UI
        editId.setText(userId);
        editId.setEnabled(false); // Không cho phép chỉnh sửa ID
        editUsername.setText(username);
        editEmail.setText(email);
        editEmail.setEnabled(false); // Không cho phép chỉnh sửa Email
        String imageUrl = UrlUtils.getImageUrl(profileImage);
        Log.d(TAG, "Attempting to load profile image from URL: " + imageUrl);
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .centerCrop()
                .into(editProfileImage);

        // Initialize image picker
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        Log.d(TAG, "Selected image URI: " + uri.toString());
                        editProfileImage.setImageURI(uri);
                    } else {
                        Log.d(TAG, "No image selected");
                    }
                });

        // Set click listener for image to upload new photo
        editProfileImage.setOnClickListener(v -> {
            Log.d(TAG, "Opening image picker");
            pickImageLauncher.launch("image/*");
        });

        // Set click listener for save button
        saveChangesButton.setOnClickListener(v -> {
            Log.d(TAG, "Save changes button clicked");
            saveChanges();
        });

        return view;
    }

    private void saveChanges() {
        String newUsername = editUsername.getText().toString().trim();
        String userId = sessionManager.getUserId();
        Log.d(TAG, "Saving changes - UserId: " + userId + ", NewUsername: " + newUsername);

        if (userId == null) {
            Log.e(TAG, "User ID not found in SessionManager");
            Toast.makeText(getContext(), "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            try {
                // Chuyển Uri thành File
                String filePath = getRealPathFromURI(selectedImageUri);
                Log.d(TAG, "Selected image file path: " + filePath);
                File file = new File(filePath);
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                imagePart = MultipartBody.Part.createFormData("profileImage", file.getName(), requestFile);
                Log.d(TAG, "Created MultipartBody.Part for image: " + file.getName());
            } catch (Exception e) {
                Log.e(TAG, "Error processing image: " + e.getMessage(), e);
                Toast.makeText(getContext(), "Error processing image", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Log.d(TAG, "No new image selected for upload");
        }

        RequestBody usernamePart = RequestBody.create(MediaType.parse("text/plain"), newUsername.isEmpty() ? sessionManager.getUsername() : newUsername);
        Log.d(TAG, "Username for update: " + (newUsername.isEmpty() ? sessionManager.getUsername() : newUsername));

        Call<UserResponse> call = apiService.updateProfile(Long.parseLong(userId), usernamePart, imagePart);
        Log.d(TAG, "Sending update profile request for userId: " + userId);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                Log.d(TAG, "Update profile response - Code: " + response.code() + ", Body: " + (response.body() != null ? response.body().toString() : "null"));
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse updatedUser = response.body();
                    Log.d(TAG, "Updated user data - ID: " + updatedUser.getId() +
                            ", Username: " + updatedUser.getUsername() +
                            ", Email: " + updatedUser.getEmail() +
                            ", ProfileImage: " + updatedUser.getProfileImage());

                    int userStatus = sessionManager.getUserStatus();

                    // Cập nhật session với thông tin mới
                    sessionManager.createLoginSessionWithProfile(
                            String.valueOf(updatedUser.getId()),
                            updatedUser.getUsername(),
                            updatedUser.getEmail(),
                            updatedUser.getToken(),
                            updatedUser.getProfileImage() != null ? updatedUser.getProfileImage() : "",
                            userStatus
                    );

                    // Tải lại ảnh hồ sơ sau khi cập nhật
                    String newProfileImage = updatedUser.getProfileImage();
                    if (newProfileImage != null && !newProfileImage.isEmpty()) {
                        String imageUrl = UrlUtils.getImageUrl(newProfileImage);
                        Log.d(TAG, "Loading updated profile image from URL: " + imageUrl);
                        Glide.with(EditProfileFragment.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .centerCrop()
                                .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                        Log.e(TAG, "Glide load failed for updated URL: " + imageUrl + ", error: " + (e != null ? e.getMessage() : "unknown"));
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                        Log.d(TAG, "Glide load successful for updated URL: " + imageUrl);
                                        return false;
                                    }
                                })
                                .into(editProfileImage);
                    } else {
                        Log.d(TAG, "No updated profile image in response, using default ic_profile");
                        editProfileImage.setImageResource(R.drawable.ic_profile);
                    }

                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                } else {
                    Log.e(TAG, "Update profile failed - Response code: " + response.code() + ", Message: " + response.message());
                    Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e(TAG, "Update profile request failed: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to get real path from URI
    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        android.database.Cursor cursor = requireActivity().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            Log.d(TAG, "Real path from URI: " + path);
            return path;
        }
        Log.d(TAG, "Failed to get real path, using URI path: " + uri.getPath());
        return uri.getPath();
    }
}