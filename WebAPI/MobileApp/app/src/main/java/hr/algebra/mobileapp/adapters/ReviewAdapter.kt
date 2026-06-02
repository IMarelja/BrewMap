package hr.algebra.mobileapp.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.models.review.Review

class ReviewAdapter : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private val items = mutableListOf<Review>()

    private var onItemLongClickListener: ((Review) -> Unit)? = null

    fun submitData(reviews: List<Review>) {
        items.clear()
        items.addAll(reviews)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)

        val viewHolder = ReviewViewHolder(view)

        view.setOnLongClickListener { v ->
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onItemLongClickListener?.invoke(items[position])
            }
            true
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tv_review_username)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_review_rating)
        private val tvComment: TextView = itemView.findViewById(R.id.tv_review_comment)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_review_date)

        fun bind(review: Review) {
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
        }
    }

    fun setOnItemLongClickListener(listener: (Review) -> Unit) {
        this.onItemLongClickListener = listener
    }
}
