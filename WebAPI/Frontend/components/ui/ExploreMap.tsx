import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';

// Define the custom coffee marker icon
const coffeeIcon = new L.Icon({
    iconUrl: '/coffee.jpg', // Path to your file in public folder
    iconSize: [35, 35],      // Size of the icon
    iconAnchor: [17, 35],    // Point of the icon which will correspond to marker's location
    popupAnchor: [0, -35],   // Point from which the popup should open relative to the iconAnchor
    className: 'custom-coffee-marker' // Optional: for extra CSS styling like border-radius
});

export default function ExploreMap({ center, cafes }) {
  return (
    <MapContainer center={center} zoom={13} style={{ height: '100%', width: '100%' }}>
      <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
      
      {cafes.map((cafe) => (
        <Marker 
          key={cafe.id} 
          position={[cafe.latitude, cafe.longitude]} 
          icon={coffeeIcon} // This line replaces the blue marker with your image
        >
          <Popup>
            <div style={{ textAlign: 'center' }}>
              <img src="/placeholder.png" style={{ width: '100px', borderRadius: '4px' }} />
              <h4 style={{ margin: '5px 0' }}>{cafe.name}</h4>
              <p style={{ margin: 0, fontSize: '12px' }}>{cafe.address?.street}</p>
            </div>
          </Popup>
        </Marker>
      ))}
    </MapContainer>
  );
}