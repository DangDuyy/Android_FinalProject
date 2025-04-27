package fit24.duy.musicplayer.models;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private int spanCount; // Số cột
    private int spacing; // Khoảng cách tính bằng pixel
    private boolean includeEdge; // Có bao gồm khoảng cách ở các cạnh hay không

    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // Vị trí mục
        int column = position % spanCount; // Cột của mục

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount;
            outRect.right = (column + 1) * spacing / spanCount;

            if (position < spanCount) { // Cạnh trên
                outRect.top = spacing;
            }
            outRect.bottom = spacing; // Khoảng cách dưới cho tất cả các mục
        } else {
            outRect.left = column * spacing / spanCount;
            outRect.right = spacing - (column + 1) * spacing / spanCount;

            if (position >= spanCount) {
                outRect.top = spacing; // Khoảng cách trên cho các mục không thuộc hàng đầu tiên
            }
        }
    }
}