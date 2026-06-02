package hr.algebra.mobileapp.fragments.main

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView that expands to wrap all children when nested in a ScrollView.
 */
class ExpandedRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val expandedHeightSpec = MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        super.onMeasure(widthSpec, expandedHeightSpec)
        layoutParams.height = measuredHeight
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
        requestLayout()
    }
}
