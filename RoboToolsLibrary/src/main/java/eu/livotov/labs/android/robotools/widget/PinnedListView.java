package eu.livotov.labs.android.robotools.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * A ListView that maintains a header pinned at the top of the list. The pinned
 * header can be pushed up and dissolved as needed.
 *
 * It also supports pagination by setting a custom view as the loading
 * indicator.
 */
public class PinnedListView extends ListView implements PinnedAdapter.HasMorePagesListener, OnScrollListener {
    public static final String TAG = PinnedListView.class.getSimpleName();

    View listFooter;
    boolean footerViewAttached = false;

    private View mHeaderView;
    private boolean mHeaderViewVisible;

    private int mHeaderViewWidth;
    private int mHeaderViewHeight;

    private PinnedAdapter adapter;
    private OnScrollListener onScrollListener;

    public void setPinnedHeaderView(View view) {
        mHeaderView = view;

        // Disable vertical fading when the pinned header is present
        // TODO change ListView to allow separate measures for top and bottom
        // fading edge;
        // in this particular case we would like to disable the top, but not the
        // bottom edge.
        if(mHeaderView != null) {
            setFadingEdgeLength(0);
        }
        requestLayout();
    }

    public void setPinnedHeaderView(int resId) {
        setPinnedHeaderView(LayoutInflater.from(getContext()).inflate(resId, this, false))  ;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(mHeaderView != null) {
            measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
            mHeaderViewWidth = mHeaderView.getMeasuredWidth();
            mHeaderViewHeight = mHeaderView.getMeasuredHeight();

        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mHeaderView != null) {
            mHeaderView.layout(getPaddingLeft(), getPaddingTop(), mHeaderViewWidth + getPaddingLeft(), mHeaderViewHeight + getPaddingTop());
            configureHeaderView(getFirstVisiblePosition());
        }
    }

    public void configureHeaderView(int position) {
        if(mHeaderView == null) {
            return;
        }

        int state = adapter.getPinnedHeaderState(position);
        switch(state) {
            case PinnedAdapter.PINNED_HEADER_GONE: {
                mHeaderViewVisible = false;
                break;
            }

            case PinnedAdapter.PINNED_HEADER_VISIBLE: {
                adapter.configurePinnedHeader(mHeaderView, position, 255);
                if(mHeaderView.getTop() != 0) {
                    mHeaderView.layout(getPaddingLeft(), getPaddingTop(), mHeaderViewWidth + getPaddingLeft(), mHeaderViewHeight + getPaddingTop());
                }
                mHeaderView.layout(getPaddingLeft(), getPaddingTop(), mHeaderViewWidth + getPaddingLeft(), mHeaderViewHeight + getPaddingTop());
                mHeaderViewVisible = true;
                break;
            }

            case PinnedAdapter.PINNED_HEADER_PUSHED_UP: {
                View firstView = getChildAt(0);
                if(firstView != null) {
                    int bottom = firstView.getBottom();
                    int headerHeight = mHeaderView.getHeight();
                    int y;
                    int alpha;
                    if(bottom < headerHeight) {
                        y = (bottom - headerHeight);
                        alpha = 255 * (headerHeight + y) / headerHeight;
                    } else {
                        y = 0;
                        alpha = 255;
                    }
                    adapter.configurePinnedHeader(mHeaderView, position, alpha);
                    if(mHeaderView.getTop() != y) {
                        mHeaderView.layout(getPaddingLeft(), getPaddingTop() + y, mHeaderViewWidth + getPaddingLeft(), mHeaderViewHeight + y + getPaddingTop());
                    }
                    mHeaderView.layout(getPaddingLeft(), getPaddingTop() + y, mHeaderViewWidth + getPaddingLeft(), mHeaderViewHeight + y + getPaddingTop());
                    mHeaderViewVisible = true;
                }
                break;
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if(mHeaderView != null && mHeaderViewVisible) {
            drawChild(canvas, mHeaderView, getDrawingTime());
        }
    }

    public PinnedListView(Context context) {
        super(context);
        super.setOnScrollListener(this);
    }

    public PinnedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOnScrollListener(this);
    }

    public PinnedListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.setOnScrollListener(this);
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        this.onScrollListener = l;
    }

    public void setLoadingView(View listFooter) {
        this.listFooter = listFooter;
    }

    public View getLoadingView() {
        return listFooter;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if(!(adapter instanceof PinnedAdapter)) {
            throw new IllegalArgumentException(PinnedListView.class.getSimpleName() + " must use adapter of type " + PinnedAdapter.class.getSimpleName());
        }

        // previous adapter
        if(this.adapter != null) {
            this.adapter.setHasMorePagesListener(null);
        }

        this.adapter = (PinnedAdapter) adapter;
        ((PinnedAdapter) adapter).setHasMorePagesListener(this);

        View dummy = new View(getContext());
        super.addFooterView(dummy);
        super.setAdapter(adapter);
        super.removeFooterView(dummy);
    }

    @Override
    public PinnedAdapter getAdapter() {
        return adapter;
    }

    public void noMorePages() {
        if(listFooter != null) {
            this.removeFooterView(listFooter);
        }
        footerViewAttached = false;
    }

    public void mayHaveMorePages() {
        if(!footerViewAttached && listFooter != null) {
            this.addFooterView(listFooter);
            footerViewAttached = true;
        }
    }

    public boolean isLoadingViewVisible() {
        return footerViewAttached;
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        configureHeaderView(firstVisibleItem);
        if(onScrollListener != null) {
            onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            // System.gc();
        }
        if(onScrollListener != null) {
            onScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    public int getVisibleCount() {
        return getLastVisiblePosition() - getFirstVisiblePosition() + 1;
    }
}
