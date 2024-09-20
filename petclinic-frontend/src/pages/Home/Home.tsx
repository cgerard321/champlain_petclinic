import { NavBar } from '@/layouts/AppNavBar.tsx';
import './Home.css';

export default function Home(): JSX.Element {
  return (
    <div className="home-container">
      <NavBar />
      <div className="home-content">
        <h1>Welcome to Champlain Pet Clinic</h1>
        <p>
          At Champlain Pet Clinic, we offer a wide range of services to ensure
          the health and well-being of your beloved pets. Our experienced
          veterinarians and staff are dedicated to providing the best care
          possible.
        </p>
        <p>
          Our services include:
          <ul className="services-list">
            <li>✔️ General Check-ups</li>
            <li>✔️ Vaccinations</li>
            <li>✔️ Dental Care</li>
            <li>✔️ Surgery</li>
            <li>✔️ Emergency Services</li>
            <li>✔️ Pet Grooming</li>
            <li>✔️ Nutritional Advice</li>
          </ul>
        </p>
        <p>
          Exciting news for pet lovers! The Champlain Pet Clinic is pleased to
          announce the upcoming opening of our very own Champlain Pet Clinic
          Store. Soon, you’ll be able to purchase a variety of high-quality pet
          products, from nutritious food and treats to toys and grooming
          supplies. Our store will also feature a section for customer reviews
          and experiences. Stay tuned for more updates and special offers!
        </p>
      </div>
    </div>
  );
}
