package ggn.lecture.verb.Features.Internal.Base;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

public abstract class InfiniteAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter {

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    private static final int VIEW_TYPE_LOADING = 0;

    private boolean mShouldLoadMore = true;
    private boolean mIsLoading = false;
    // Used to indicate the infinite scrolling should be bottom up
    private boolean mIsReversedScrolling = false;
    private OnLoadMoreListener mLoadMoreListener;

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_LOADING) {
            return getLoadingViewHolder(parent);
        } else {
            return onCreateView(parent, viewType);
        }
    }



    public boolean ismIsLoading()
    {
        return mIsLoading;
    }

    /**
     * Subclasses should override this method, to actually bind the view data,
     * but always call <code>super.onBindViewHolder(holder, position)</code>
     * to enable the adapter to calculate whether the load more callback should be invoked
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mShouldLoadMore && !mIsLoading) {
            int threshold = getVisibleThreshold();
            boolean hasReachedThreshold = mIsReversedScrolling ? position <= threshold
                                                               : position >= getCount() - threshold;
            if (hasReachedThreshold) {
                mIsLoading = true;
                if (mLoadMoreListener != null) {
                    mLoadMoreListener.onLoadMore();
                }
            }
        }
    }

    @Override
    public final int getItemCount() {
        int actualCount = getCount();
        // So as to avoid nasty index calculations, having reversed scrolling does
        // not affect the item count.
        // The consequence of this is, while there is more data to load, the first item on
        // the list will be replaced by the loading view
        if(actualCount == 0 || !mShouldLoadMore || mIsReversedScrolling) {
            return actualCount;
        } else {
            return actualCount + 1;
        }
    }

    @Override
    public final int getItemViewType(int position) {
        if(isLoadingView(position)) {
            return VIEW_TYPE_LOADING;
        } else {
            int viewType = getViewType(position);
            if (viewType == VIEW_TYPE_LOADING) {
                throw new IndexOutOfBoundsException("0 index is reserved for the loading view");
            } else {
                return viewType;
            }
        }
    }

    private boolean isLoadingView(int position) {
        // For reversed scrolling, the loading view is always the top one
        int loadingViewPosition = mIsReversedScrolling ? 0 : getCount();
        return position == loadingViewPosition && mShouldLoadMore;
    }

    /**
     * Set as false when you don't want the recycler view to load more data.
     * This will also remove the loading view
     */
    public void setShouldLoadMore(boolean shouldLoadMore) {
        this.mShouldLoadMore = shouldLoadMore;
    }

    /**
     * Set as true if you want the endless scrolling to be as the user scrolls
     * to the top of the list, instead of bottom
     */
    public void setIsReversedScrolling(boolean reversed) {
        this.mIsReversedScrolling = reversed;
    }

    /**
     * Registers a callback to be notified when there is a need to load more data
     */
    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.mLoadMoreListener = listener;
    }

    /**
     * This informs the adapter that <code>itemCount</code> more data has been loaded,
     * starting from <code>positionStart</code>
     *
     * This also calls <code>notifyItemRangeInserted(int, int)</code>,
     * so the implementing class only needs to call this method
     *
     * @param positionStart Position of the first item that was inserted
     * @param itemCount Number of items inserted
     *
     */
    public void moreDataLoaded(int positionStart, int itemCount) {
        try {
            mIsLoading = false;
            notifyItemRemoved(positionStart); // remove the loading view
            notifyItemRangeInserted(positionStart, itemCount);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeLoadingView(int positionStart) {
        mIsLoading = false;
        notifyItemRemoved(positionStart); // remove the loading view
    }

    /**
     * Returns the number of scrollable items that should be left (threshold) in the
     * list before <code>OnLoadMoreListener</code> will be called
     *
     * You can override this to return a preffered threshold,
     * or leave it to use the default
     *
     * @return integer threshold
     */
    public int getVisibleThreshold() {
        return 5;
    }

    /**
     * Returns the loading view to be shown at the bottom of the list.
     * @return loading view
     */
    public abstract RecyclerView.ViewHolder getLoadingViewHolder(ViewGroup parent);

    /**
     * The count of the number of items in the list. This does not include the loading item
     * @return number of items in list
     */
    public abstract int getCount();

    /**
     * Return the view type of the item at <code>position</code> for the purposes
     * of view recycling.
     *
     * <p>0 index is reserved for the loading view. So this function cannot return 0.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at
     *                 <code>position</code>. Type codes need not be contiguous.
     */
    public abstract int getViewType(int position);

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent
     * an item. This is the same as the onCreateViewHolder method in RecyclerView.Adapter,
     * except that it internally detects and handles the creation on the loading footer
     * @param parent
     * @param viewType
     * @return
     */
    public abstract VH onCreateView(ViewGroup parent, int viewType);
}