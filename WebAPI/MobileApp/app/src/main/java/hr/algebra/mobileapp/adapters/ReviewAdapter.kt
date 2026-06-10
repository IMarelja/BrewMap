package hr.algebra.mobileapp.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import hr.algebra.mobileapp.MainActivity
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.auth.TokenManager
import hr.algebra.mobileapp.models.review.Review

class ReviewAdapter(private val showLocationButton: Boolean = false) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private val items = mutableListOf<Review>()

    private var onEditClickListener: ((Review) -> Unit)? = null
    private var onDeleteClickListener: ((Review) -> Unit)? = null
    private var onReportReviewClickListener: ((Review) -> Unit)? = null
    private var onReportUserClickListener: ((Review) -> Unit)? = null
    private var onViewLocationClickListener: ((Review) -> Unit)? = null

    fun submitData(reviews: List<Review>) {
        items.clear()
        items.addAll(reviews)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)

        val viewHolder = ReviewViewHolder(view)

        viewHolder.btnEdit.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onEditClickListener?.invoke(items[position])
            }
        }

        viewHolder.btnDelete.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onDeleteClickListener?.invoke(items[position])
            }
        }

        viewHolder.btnViewLocation.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onViewLocationClickListener?.invoke(items[position])
            }
        }

        viewHolder.btnMenu.setOnClickListener { anchor ->
            val position = viewHolder.adapterPosition
            if (position == RecyclerView.NO_POSITION) return@setOnClickListener
            val review = items[position]

            PopupMenu(anchor.context, anchor).apply {
                inflate(R.menu.menu_review_item)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_view_profile -> {
                            val activity = anchor.context as? MainActivity
                            if (TokenManager.getUserId() == review.userId) {
                                activity?.openMyProfile()
                            } else {
                                activity?.openStrangerProfile(review.userId)
                            }
                            true
                        }
                        R.id.action_report_review -> { onReportReviewClickListener?.invoke(review); true }
                        R.id.action_report_user -> { onReportUserClickListener?.invoke(review); true }
                        else -> false
                    }
                }
            }.show()
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(items[position], showLocationButton)
    }

    override fun getItemCount(): Int = items.size

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tv_review_username)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_review_rating)
        private val tvComment: TextView = itemView.findViewById(R.id.tv_review_comment)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_review_date)
        private val llOwnerActions: LinearLayout = itemView.findViewById(R.id.ll_review_owner_actions)
        val btnMenu: ImageButton = itemView.findViewById(R.id.btn_review_menu)
        val btnEdit: MaterialButton = itemView.findViewById(R.id.btn_edit_review)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btn_delete_review)
        val btnViewLocation: MaterialButton = itemView.findViewById(R.id.btn_view_location)

        fun bind(review: Review, showLocationButton: Boolean) {
            val ctx = itemView.context
            val displayName = review.username?.takeIf { it.isNotBlank() }
            tvUsername.text = displayName ?: ctx.getString(R.string.review_deleted_user)
            tvUsername.paintFlags = if (displayName == null) {
                tvUsername.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                tvUsername.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            tvRating.text = ctx.getString(R.string.review_rating_format, review.rating)
            tvComment.text = review.comment?.takeIf { it.isNotBlank() } ?: ctx.getString(R.string.review_no_comment)
            tvDate.text = ctx.getString(R.string.review_created_format, review.createdAt.take(10))

            val currentUserId = TokenManager.getUserId()
            val isOwnReview = currentUserId != null && currentUserId == review.userId
            btnEdit.visibility = if (isOwnReview) View.VISIBLE else View.GONE
            btnDelete.visibility = if (isOwnReview) View.VISIBLE else View.GONE
            btnViewLocation.visibility = if (showLocationButton) View.VISIBLE else View.GONE
            llOwnerActions.visibility = if (isOwnReview || showLocationButton) View.VISIBLE else View.GONE
        }
    }

    fun setOnEditClickListener(listener: (Review) -> Unit) {
        this.onEditClickListener = listener
    }

    fun setOnDeleteClickListener(listener: (Review) -> Unit) {
        this.onDeleteClickListener = listener
    }

    fun setOnReportReviewClickListener(listener: (Review) -> Unit) {
        this.onReportReviewClickListener = listener
    }

    fun setOnReportUserClickListener(listener: (Review) -> Unit) {
        this.onReportUserClickListener = listener
    }

    fun setOnViewLocationClickListener(listener: (Review) -> Unit) {
        this.onViewLocationClickListener = listener
    }
}
