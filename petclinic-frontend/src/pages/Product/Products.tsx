import {NavBar} from '@/layouts/AppNavBar.tsx';
import ProductsList from '@/features/products/ProductsList.tsx';
import ProductSearch from '@/features/products/components/ProductSearch';
import './Products.css';
import TrendingList from '@/features/products/TrendingList';

export default function Products(): JSX.Element {
    return (
        <div>
            <NavBar/>
            <header className="header-container">
                <div className="overlay-text">
                    <h1>Welcome to PetClinic Shop Page!</h1>
                    <p>Discover a range of quality items to keep your pets healthy and
                        happy.</p>
                </div>
                <img
                    src="https://cdn.pixabay.com/photo/2018/10/01/09/21/pets-3715733_1280.jpg"
                    alt="Pets"
                    className="full-width-image"
                />
            </header>
            <div className="block">
                <ProductSearch/>
            </div>
            <div className="block">
                <h1>Products</h1>
            </div>
            <div className="block">
                <h3>Here are the trending Products</h3>
            </div>
            <div className="block">
                <TrendingList/>
            </div>
            <div className="block">
                <hr/>
            </div>
            <ProductsList/>
        </div>
    );
}
