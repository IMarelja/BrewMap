package hr.algebra.mobileapp.fragments.main

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * LayoutManager for RecyclerViews embedded in a parent ScrollView.
 * Disables internal vertical scroll so the list expands to full content height.
 */
class NonScrollableLinearLayoutManager(context: Context) : LinearLayoutManager(context) {
    override fun canScrollVertically(): Boolean = false
}
