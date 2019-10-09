package androidx.recyclerview.widget;


import android.view.View;
import android.widget.OverScroller;

import androidx.core.widget.ScrollerCompat;

import java.lang.reflect.Field;

public class RVScrollViewUtils {

  public static void setMeasureSpecs(RecyclerView.LayoutManager layoutManager, int wSpec, int hSpec) {
    layoutManager.setMeasureSpecs(wSpec, hSpec);
  }

  public static RecyclerView.Recycler getRecycler(RecyclerView recyclerView) {
    return recyclerView.mRecycler;
  }

  public static RecyclerView.State getState(RecyclerView recyclerView) {
    return recyclerView.mState;
  }

  public static void setScrollState(RecyclerView recyclerView, int state) {
    recyclerView.setScrollState(state);
  }

  public static int scrollVerticallyBy(RecyclerView recyclerView, int scroll) {
    final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
    if (layoutManager == null) {
      return 0;
    }
    // 如果不使用startInterceptRequestLayout/stopInterceptRequestLayout
    // 会导致RecyclerView频繁触发requestLayout
    recyclerView.startInterceptRequestLayout();
    final int scrollVerticallyBy = layoutManager
            .scrollVerticallyBy(scroll, recyclerView.mRecycler, recyclerView.mState);
    recyclerView.stopInterceptRequestLayout(false);
    return scrollVerticallyBy;
  }

  public static boolean isTopOverScrolled(RecyclerView recyclerView) {
    if (recyclerView.getChildCount() == 0) {
      return true;
    }
    View topChild = recyclerView.getChildAt(0);
    final int topChildAdapterPosition = recyclerView.getChildAdapterPosition(topChild);
    return topChildAdapterPosition == 0 && topChild.getScrollY() == 0;
  }

  public static boolean isBottomOverScrolled(RecyclerView recyclerView) {
    if (recyclerView.getChildCount() == 0) {
      return true;
    }
    View bottomChild = recyclerView.getChildAt(recyclerView.getChildCount() - 1);
    final int topChildAdapterPosition = recyclerView.getChildAdapterPosition(bottomChild);
    return topChildAdapterPosition == recyclerView.getAdapter().getItemCount() - 1
        && bottomChild.getBottom() + recyclerView.getPaddingBottom() == recyclerView.getHeight();
  }

  public static float getCurrentVelocityY(RecyclerView recyclerView) {
    try {
      Field mScrollerField = recyclerView.mViewFlinger.getClass().getDeclaredField("mScroller");
      mScrollerField.setAccessible(true);
      Object scroller = mScrollerField.get(recyclerView.mViewFlinger);
      return (scroller instanceof ScrollerCompat)
          ? ((ScrollerCompat) scroller).getCurrVelocity()
          : ((OverScroller) scroller).getCurrVelocity();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
      return 0;
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      return 0;
    }
  }
}
