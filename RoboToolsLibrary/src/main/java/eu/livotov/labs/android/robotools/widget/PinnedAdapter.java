package eu.livotov.labs.android.robotools.widget;

import android.view.View;
import android.widget.BaseAdapter;

public abstract class PinnedAdapter extends BaseAdapter {
    public static final String TAG = PinnedAdapter.class.getSimpleName();

    public interface HasMorePagesListener {
        void noMorePages();

        void mayHaveMorePages();
    }

    /**
     * The <em>current</em> page, not the page that is going to be loaded.
     */
    int page = 1;
    int initialPage = 1;
    boolean automaticNextPageLoading = false;
    HasMorePagesListener hasMorePagesListener;

    public void setHasMorePagesListener(HasMorePagesListener hasMorePagesListener) {
        this.hasMorePagesListener = hasMorePagesListener;
    }

    /**
     * Pinned header state: don't show the header.
     */
    public static final int PINNED_HEADER_GONE = 0;

    /**
     * Pinned header state: show the header at the top of the list.
     */
    public static final int PINNED_HEADER_VISIBLE = 1;

    /**
     * Pinned header state: show the header. If the header extends beyond the
     * bottom of the first shown element, push it up and clip.
     */
    public static final int PINNED_HEADER_PUSHED_UP = 2;

    /**
     * Computes the desired state of the pinned header for the given position of
     * the first visible list item. Allowed return values are
     * {@link #PINNED_HEADER_GONE}, {@link #PINNED_HEADER_VISIBLE} or
     * {@link #PINNED_HEADER_PUSHED_UP}.
     */
    public int getPinnedHeaderState(int position) {
        if(position < 0 || getCount() == 0) {
            return PINNED_HEADER_GONE;
        }

        // The header should get pushed up if the top item shown
        // is the last item in a section for a particular letter.
        int section = getSectionForPosition(position);
        int nextSectionPosition = getPositionForSection(section + 1);
        if(nextSectionPosition != -1 && position == nextSectionPosition - 1) {
            return PINNED_HEADER_PUSHED_UP;
        }

        return PINNED_HEADER_VISIBLE;
    }

    /**
     * Sets the initial page when {@link #resetPage()} is called. Default is 1
     * (for APIs with 1-based page number).
     */
    public void setInitialPage(int initialPage) {
        this.initialPage = initialPage;
    }

    /**
     * Resets the current page to the page specified in
     * {@link #setInitialPage(int)}.
     */
    public void resetPage() {
        this.page = this.initialPage;
    }

    /**
     * Increases the current page number.
     */
    public void nextPage() {
        this.page++;
    }


  
    

    public void notifyNoMorePages() {
        automaticNextPageLoading = false;
        if(hasMorePagesListener != null)
            hasMorePagesListener.noMorePages();
    }

    public void notifyMayHaveMorePages() {
        automaticNextPageLoading = true;
        if(hasMorePagesListener != null)
            hasMorePagesListener.mayHaveMorePages();
    }


    protected abstract void onNextPageRequested(int page);



    /**
     * Configures the pinned header view to match the first visible list item.
     * 
     * @param header
     *            pinned header view.
     * @param position
     *            position of the first visible list item.
     * @param alpha
     *            fading of the header view, between 0 and 255.
     */
    public abstract void configurePinnedHeader(View header, int position, int alpha);

    public int getPositionForSection(int section) {
        return 0;
    }

    public int getSectionForPosition(int position) {
        return 0;
    }

    public  Object[] getSections() {
        return null;
    }
}
