package com.lsjwzh.widget.powerfulscrollview;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewParent;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 按照内容自动适配高度，直到高度等于PowerfulScrollView
 */
public class AutoMeasureRecyclerView extends RecyclerView {

    private ObserverForReMeasure observerForReMeasure = new ObserverForReMeasure();

    public AutoMeasureRecyclerView(Context context) {
        super(context);
    }

    public AutoMeasureRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoMeasureRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        if (getAdapter() != null) {
            getAdapter().unregisterAdapterDataObserver(observerForReMeasure);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observerForReMeasure);
        }
    }

    @Override
    public void scrollToPosition(int position) {
        scrollToPositionInternal(position, false);
    }

    @Override
    public void smoothScrollToPosition(final int position) {
        scrollToPositionInternal(position, true);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        PowerfulScrollView multiRVScrollView = findMultiRVScrollView();
        final Adapter adapter = getAdapter();
        if (adapter != null && adapter.getItemCount() > 0) {
            if (MeasureSpec.getSize(heightSpec) == 0 &&
                    multiRVScrollView != null && multiRVScrollView.getMeasuredHeight() > 0) {
                heightSpec = MeasureSpec.makeMeasureSpec(multiRVScrollView.getMeasuredHeight(),
                        MeasureSpec.EXACTLY);
            }
            super.onMeasure(widthSpec, heightSpec);
            if (getChildCount() == adapter.getItemCount()) {
                final Rect lastItemRect = getLastItemRect();
                int targetHeight = lastItemRect.bottom
                        + getPaddingTop() + getPaddingBottom();
                targetHeight = Math.max(targetHeight, getSuggestedMinimumHeight());
                heightSpec = MeasureSpec.makeMeasureSpec(targetHeight, MeasureSpec.EXACTLY);
                super.onMeasure(widthSpec, heightSpec);
            } else if (getChildCount() == 0) {
                // 第一次layout时，可能出现getChildCount=0，此时需要多layout一次进行校正
                getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        getViewTreeObserver().removeOnPreDrawListener(this);
                        requestLayout();
                        return false;
                    }
                });
            }
        } else {
            if (MeasureSpec.getSize(heightSpec) < getSuggestedMinimumHeight()) {
                heightSpec = MeasureSpec.makeMeasureSpec(getSuggestedMinimumHeight(), MeasureSpec.EXACTLY);
            }
            super.onMeasure(widthSpec, heightSpec);
        }
    }

    private Rect getLastItemRect() {
        Rect outRect = new Rect();
        if (getChildCount() > 0) {
            getDecoratedBoundsWithMargins(getChildAt(getChildCount() - 1), outRect);
        }
        return outRect;
    }

    public void refreshHeightIfNeed() {
        int scrollRange = computeVerticalScrollRange();
        if (scrollRange > getHeight()) {
            PowerfulScrollView multiRVScrollView = findMultiRVScrollView();
            if (multiRVScrollView != null && multiRVScrollView.getMeasuredHeight() > 0) {
                if (multiRVScrollView.getHeight() != getHeight()) {
                    requestLayout();
                }
            }
        }
    }

    private void scrollToPositionInternal(int position, boolean isSmooth) {
        PowerfulScrollView multiRVScrollView = findMultiRVScrollView();
        if (multiRVScrollView != null && multiRVScrollView.isCoordinatedWith(this)
                && getChildCount() > 0) {
            ViewHolder childViewHolder = getChildViewHolder(getChildAt(getChildCount() - 1));
            if (childViewHolder != null) {
                float coordinatedTop = multiRVScrollView.getCoordinatedTop(this);
                if (childViewHolder.getAdapterPosition() < position) {
                    multiRVScrollView.scrollTo(0, (int) coordinatedTop);
                    rvScrollToPosition(position, isSmooth);
                } else {
                    ViewHolder targetViewHolder = findViewHolderForAdapterPosition(position);
                    if (targetViewHolder != null &&
                            targetViewHolder.itemView.getBottom()
                                    + coordinatedTop
                                    - multiRVScrollView.getScrollY() > multiRVScrollView.getHeight()) {
                        multiRVScrollView.scrollTo(0, (int) coordinatedTop);
                    }
                    rvScrollToPosition(position, isSmooth);
                }
            }
        } else {
            rvScrollToPosition(position, isSmooth);
        }
    }

    private void rvScrollToPosition(int position, boolean isSmooth) {
        if (isSmooth) {
            AutoMeasureRecyclerView.super.smoothScrollToPosition(position);
        } else {
            AutoMeasureRecyclerView.super.scrollToPosition(position);
        }
    }

    private PowerfulScrollView findMultiRVScrollView() {
        ViewParent parent = getParent();
        while (parent != null) {
            if (parent instanceof PowerfulScrollView) {
                return (PowerfulScrollView) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    class ObserverForReMeasure extends AdapterDataObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            refreshHeightIfNeed();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            refreshHeightIfNeed();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            super.onItemRangeChanged(positionStart, itemCount, payload);
            refreshHeightIfNeed();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            refreshHeightIfNeed();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            refreshHeightIfNeed();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            refreshHeightIfNeed();
        }
    }
}
