import { ProductModel } from '@/features/products/models/ProductModels/ProductModel.ts';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import { JSX } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useUser } from '@/context/UserContext';
import { Role } from '@/models/Role';

export default function ProductDetails(): JSX.Element {
  const location = useLocation();
  const navigate = useNavigate();
  const { product } = location.state as { product: ProductModel };
  const { user } = useUser();

  const isAdmin = user?.roles?.some(role => role.name === 'ADMIN');

  const navigateToEditProduct = (): void => {
    navigate(`/products/edit/${product.productId}`, { state: { product } });
  };

  const renderProductDescription = (productName: string): JSX.Element => {
    switch (productName) {
      case 'Horse Saddle':
        return (
          <>
            Horse Saddle - Classic Leather Western Saddle Brand: EquiStyle Size:
            Size: 16&quot; seat The Classic Leather Western Saddle combines
            durability and comfort for both horse and rider. Crafted from
            high-quality leather, it features a padded seat and adjustable
            stirrups for a customized fit. Ideal for trail riding and everyday
            use, this saddle ensures a secure and enjoyable ride.
          </>
        );
      case 'Rabbit Hutch':
        return (
          <>
            Rabbit Hutch - Deluxe Wooden Enclosure Brand: Happy Bunnies
            Dimensions: 48&quot; L x 24&quot; W x 36&quot; H<br />
            Our Deluxe Wooden Rabbit Hutch features a spacious design with
            multiple levels, providing a safe and comfortable home for your
            rabbits. Made from weather-resistant wood, it includes a large run
            for exercise and a cozy sleeping area. The easy-access doors ensure
            convenient cleaning and feeding.
          </>
        );
      case 'Dog Food':
        return (
          <>
            Premium Dog Food - Chicken & Brown Rice Brand: Furry Friends 30lbs
            <br />
            Made with real chicken and brown rice, our Premium Dog Food offers
            balanced nutrition for dogs of all ages. Rich in omega fatty acids
            for a shiny coat and digestive health, this formula contains no
            artificial preservatives. Ideal for keeping your furry friend happy
            and energized!
          </>
        );
      case 'Fish Tank Heater':
        return (
          <>
            Fish Tank Heater - Adjustable 300W Submersible Heater AquaSafe
            Power: 300 watts The Adjustable 300W Submersible Heater is perfect
            for maintaining optimal water temperature in aquariums up to 75
            gallons. Featuring an easy-to-read LED display and adjustable
            temperature settings, this heater ensures a stable environment for
            your fish. Its durable design and automatic shut-off function
            provide added safety.
          </>
        );
      case 'Cat Litter':
        return (
          <>
            Cat Litter - Premium Clumping Cat Litter Brand: Purrfect Choice
            Weight: 20 lbs Our Premium Clumping Cat Litter offers exceptional
            odor control and easy cleanup. Made from natural clay, it forms
            tight clumps for effortless scooping and minimizes dust for a
            cleaner home. Safe for cats and the environment, this litter ensures
            your feline friend stays happy and healthy.
          </>
        );
      case 'Flea Collar':
        return (
          <>
            Flea Collar - Advanced Flea and Tick Control Brand: PetGuard
            Adjustable, fits necks up to 22&quot; The Advanced Flea and Tick
            Collar provides long-lasting protection for your pet against fleas
            and ticks for up to 8 months. Made with natural ingredients, it’s
            safe for dogs and cats. The adjustable design ensures a comfortable
            fit, keeping your furry friend protected and pest-free.
          </>
        );
      case 'Bird Cage':
        return (
          <>
            Bird Cage - Spacious Double-Door Aviary Brand: Feather Haven
            Dimensions: 36&quot; L x 24&quot; W x 60&quot; H<br />
            The Spacious Double-Door Aviary is designed for small to
            medium-sized birds, offering ample room for play and exercise.
            Featuring sturdy construction, multiple perches, and easy-access
            doors for cleaning, this cage ensures a safe and comfortable
            environment for your feathered friends.
          </>
        );
      case 'Aquarium Filter':
        return (
          <>
            Aquarium Filter - 50-Gallon Canister Filter Brand: Crystal Clear
            Flow Rate: 300 GPH The 50-Gallon Canister Filter provides powerful
            filtration for aquariums up to 50 gallons. Featuring a multi-stage
            filtration system, it effectively removes debris, toxins, and odors,
            ensuring a clean and healthy environment for your fish. The quiet
            operation and easy setup make it a perfect choice for any aquarium
            enthusiast.
          </>
        );
      default:
        return <>Description not available</>;
    }
  };

  return (
    <>
      <NavBar />
      <h1>{product.productName}</h1>
      <p>{renderProductDescription(product.productName)}</p>
      <p>Price: ${product.productSalePrice.toFixed(2)}</p>
      <p>Rating: {product.averageRating} / 5</p>

      {isAdmin && <button onClick={navigateToEditProduct}>Edit Product</button>}
    </>
  );
}
