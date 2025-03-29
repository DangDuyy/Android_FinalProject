package fit24.duy.musicplayer.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import java.util.List;
import java.util.Random;

import fit24.duy.musicplayer.R;
import fit24.duy.musicplayer.models.MediaType;

public class MediaTypeAdapter extends BaseAdapter {
    private Context context;
    private List<MediaType> mediaTypes;

    public MediaTypeAdapter(Context context, List<MediaType> mediaTypes) {
        this.context = context;
        this.mediaTypes = mediaTypes;
    }

    @Override
    public int getCount() {
        return mediaTypes == null ? 0 : mediaTypes.size();
    }

    @Override
    public Object getItem(int position) {
        return mediaTypes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView txtName;
        ImageView imgIcon;

        View background;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_media_type, parent, false);
            holder = new ViewHolder();
            holder.txtName = convertView.findViewById(R.id.txName);
            holder.imgIcon = convertView.findViewById(R.id.imgIcon);

            holder.background = convertView.findViewById(R.id.rootLayout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        MediaType mediaType = mediaTypes.get(position);
        holder.txtName.setText(mediaType.getName());

        // Danh sách các màu
        int[] colors = {
                Color.parseColor("#E91E63"), // Hồng
                Color.parseColor("#4CAF50"), // Xanh lá
                Color.parseColor("#9C27B0"), // Tím
                Color.parseColor("#03A9F4"), // Xanh nhạt
                Color.parseColor("#F44336"), // Đỏ
                Color.parseColor("#2196F3"), // Xanh biển
                Color.parseColor("#795548"), // Nâu
                Color.parseColor("#FF9800"), // Cam
                Color.parseColor("#8BC34A"), // Xanh lá mạ
                Color.parseColor("#607D8B"), // Xám xanh
                Color.parseColor("#F06292")  // Hồng sáng
        };

        // Chọn màu ngẫu nhiên
        int randomColor = colors[new Random().nextInt(colors.length)];

        // Thay đổi màu nền mà vẫn giữ bo góc
        GradientDrawable drawable = (GradientDrawable) holder.background.getBackground();
        drawable.setColor(randomColor);

        Glide.with(context)
                .load("http://10.0.2.2:8080/uploads/" + mediaType.getIcon() + "?t=" + System.currentTimeMillis())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .centerCrop()
                .into(holder.imgIcon);

        convertView.setOnClickListener(v -> Toast.makeText(context, "Đã chọn: " + mediaType.getName(), Toast.LENGTH_SHORT).show());
        return convertView;
    }
}
