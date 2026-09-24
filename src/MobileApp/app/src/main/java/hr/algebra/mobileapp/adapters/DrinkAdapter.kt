package hr.algebra.mobileapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.models.drink.Drink

class DrinkAdapter : RecyclerView.Adapter<DrinkAdapter.DrinkViewHolder>() {

    private val items = mutableListOf<Drink>()

    private var onEditClickListener: ((Drink) -> Unit)? = null
    private var onReportClickListener: ((Drink) -> Unit)? = null
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

        viewHolder.btnMenu.setOnClickListener { anchor ->
            val position = viewHolder.adapterPosition
            if (position == RecyclerView.NO_POSITION) return@setOnClickListener
            val drink = items[position]

            PopupMenu(anchor.context, anchor).apply {
                inflate(R.menu.menu_drink_item)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit_drink -> { onEditClickListener?.invoke(drink); true }
                        R.id.action_report_drink -> { onReportClickListener?.invoke(drink); true }
                        else -> false
                    }
                }
            }.show()
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
        val btnMenu: ImageButton = itemView.findViewById(R.id.btn_drink_menu)
        val btnAddReview: MaterialButton = itemView.findViewById(R.id.btn_add_drink_review)

        fun bind(drink: Drink) {
            tvName.text = drink.name
            tvRating.text = itemView.context.getString(R.string.drink_score_format, drink.aggregatedRating.average, drink.aggregatedRating.count)
            tvDescription.text = drink.description ?: ""
        }
    }

    fun setOnEditClickListener(listener: (Drink) -> Unit) {
        this.onEditClickListener = listener
    }

    fun setOnReportClickListener(listener: (Drink) -> Unit) {
        this.onReportClickListener = listener
    }

    fun setOnAddReviewClickListener(listener: (Drink) -> Unit) {
        this.onAddReviewClickListener = listener
    }
}
