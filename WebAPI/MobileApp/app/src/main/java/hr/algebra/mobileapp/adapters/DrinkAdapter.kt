package hr.algebra.mobileapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.models.drink.Drink

class DrinkAdapter : RecyclerView.Adapter<DrinkAdapter.DrinkViewHolder>() {

    private val items = mutableListOf<Drink>()

    private var onItemLongClickListener: ((Drink) -> Unit)? = null
    private var onAddReviewClickListener: ((Drink) -> Unit)? = null

    fun submitData(drinks: List<Drink>) {
        items.clear()
        items.addAll(drinks)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrinkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drink, parent, false)

        val viewHolder = DrinkViewHolder(view)

        view.setOnLongClickListener { v ->
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onItemLongClickListener?.invoke(items[position])
            }
            true
        }

        viewHolder.btnAddReview.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onAddReviewClickListener?.invoke(items[position])
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: DrinkViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class DrinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_drink_name)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_drink_rating)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_drink_description)
        val btnAddReview: MaterialButton = itemView.findViewById(R.id.btn_add_drink_review)

        fun bind(drink: Drink) {
            tvName.text = drink.name
            tvRating.text = itemView.context.getString(R.string.drink_score_format, drink.aggregatedRating.average, drink.aggregatedRating.count)
            tvDescription.text = drink.description ?: ""
        }
    }

    fun setOnItemLongClickListener(listener: (Drink) -> Unit) {
        this.onItemLongClickListener = listener
    }

    fun setOnAddReviewClickListener(listener: (Drink) -> Unit) {
        this.onAddReviewClickListener = listener
    }
}
