package hr.algebra.mobileapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hr.algebra.mobileapp.R
import hr.algebra.mobileapp.models.location.Location

class LocationSearchAdapter(
    private val onLocationClick: (Location) -> Unit
) : RecyclerView.Adapter<LocationSearchAdapter.LocationViewHolder>() {

    private val items = mutableListOf<Location>()

    fun submitData(locations: List<Location>) {
        items.clear()
        items.addAll(locations)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location_result, parent, false)
        return LocationViewHolder(view, onLocationClick)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class LocationViewHolder(
        itemView: View,
        private val onLocationClick: (Location) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tv_result_name)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_result_rating)
        private val tvAddress: TextView = itemView.findViewById(R.id.tv_result_address)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_result_description)

        fun bind(location: Location) {
            val ctx = itemView.context
            tvName.text = location.name
            tvRating.text = ctx.getString(R.string.location_rating_format, location.averageRating, location.totalReviews)
            tvAddress.text = "${location.address.street}, ${location.address.city}"
            tvDescription.text = location.description?.take(120).orEmpty()

            itemView.setOnClickListener { onLocationClick(location) }
        }
    }
}
