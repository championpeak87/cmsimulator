package fiitstu.gulis.cmsimulator.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * A RecyclerView that can expands based on number of items, up to certain limits.
 *
 * Note: This is a hacky solution, this should probably be done by a custom LayoutManager, but I could
 * not get it to work.
 *
 * Created by Jakub Sedlář on 10.02.2018.
 */
public class AdaptiveRecyclerView extends RecyclerView {

    public interface IntMethod {
        int get();
    }

    private IntMethod dimension;
    private IntMethod itemCount;
    private float maxCount;

    public AdaptiveRecyclerView(Context context) {
        super(context);
    }

    public AdaptiveRecyclerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public AdaptiveRecyclerView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
    }

    /**
     * Sets the method that returns the dimension of one displayed item
     * @param dimension the method that returns the dimension of one displayed item
     */
    public void setDimension(IntMethod dimension) {
        this.dimension = dimension;
    }

    /**
     * Sets the method that returns the number of displayed items
     * @param itemCount the method that returns the number of displayed items
     */
    public void setItemCount(IntMethod itemCount) {
        this.itemCount = itemCount;
    }

    /**
     * Sets the maximum effective item count (if itemCount gets bigger than this, the extra items will no
     * longer cause the view to resize)
     * @param maxCount the maximum effective item count
     */
    public void setMaxCount(float maxCount) {
        this.maxCount = maxCount;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int orientation = ((LinearLayoutManager)getLayoutManager()).getOrientation();

        if (dimension != null && itemCount != null) {
            int maxDimension = Math.round(dimension.get() * Math.min(itemCount.get(), maxCount));
            if (orientation == LinearLayoutManager.VERTICAL) {
                heightSpec = View.MeasureSpec.makeMeasureSpec(maxDimension, View.MeasureSpec.EXACTLY);
            } else {
                widthSpec = View.MeasureSpec.makeMeasureSpec(maxDimension, View.MeasureSpec.EXACTLY);
            }
        }
        super.onMeasure(widthSpec, heightSpec);
    }
}
