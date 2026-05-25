package hr.algebra.mobileapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.models.Review

class ReviewAdapter : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private val items = mutableListOf<Review>()

    fun submitData(reviews: List<Review>) {
        items.clear()
        items.addAll(reviews)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRating: TextView = itemView.findViewById(R.id.tv_review_rating)
        private val tvComment: TextView = itemView.findViewById(R.id.tv_review_comment)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_review_date)

        fun bind(review: Review) {
            tvRating.text = "Rating: ${review.rating} / 5"
            tvComment.text = review.comment?.takeIf { it.isNotBlank() } ?: "No comment"
            tvDate.text = "Created: ${review.createdAt.take(10)}"
        }
    }
}
