package fiitstu.gulis.cmsimulator.adapters;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class InfiniteScrollListener extends RecyclerView.OnScrollListener {
    private final static int VISIBLE_THRESHOLD = 2;
    private GridLayoutManager linearLayoutManager;
    private boolean loading;
    private OnLoadMoreListener listener;
    private boolean pauseListening = false;

    private boolean END_OF_FEED_ADDED = false;
    private int NUM_LOAD_ITEMS = 10;

    public interface OnLoadMoreListener{
        void onLoadMore();
    }

    public InfiniteScrollListener(GridLayoutManager linearLayoutManager, OnLoadMoreListener listener)
    {
        this.linearLayoutManager = linearLayoutManager;
        this.listener = listener;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (dx == 0 && dy == 0)
                return;
        int totalItemCount = linearLayoutManager.getItemCount();
        int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
        if (!loading && totalItemCount <= lastVisibleItem + VISIBLE_THRESHOLD && totalItemCount != 0 && !END_OF_FEED_ADDED && !pauseListening) {
            if (listener != null) {
                listener.onLoadMore();
            }
            this.loading = true;
        }
    }

    public void setLoaded() {
        this.loading = false;
    }

    public void addEndOfRequests()
    {
        this.END_OF_FEED_ADDED = true;
    }

    public void pauseScrollListener(boolean value)
    {
        this.pauseListening = pauseListening;
    }
}
