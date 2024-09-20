import { getAllProducts } from '@/features/products/api/getAllProducts.ts';
import { useState, useEffect} from 'react';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';


export default function TrendingList(){
    const [trendingList, setTrendingList] = useState<ProductModel[]>([]);

    const fetchProducts = async (): Promise<void> => {
      const list = await getAllProducts();
      setTrendingList(list);
    };

    useEffect(() => {
        fetchProducts();
      }, []);

      const sortMostTrending = [...trendingList].sort((a, b) => b.requestCount - a.requestCount);
      const topFourTrending = sortMostTrending.slice(0,4);


      return (
        <div>
          <div className="grid">
            {topFourTrending
              .map(
                (product: {
                  productId: string;
                  productName: string;
                  productDescription: string;
                  averageRating: number;
                  productSalePrice: number;

                }) => (
                  <div className="card" key={product.productId}>
                    <h2>{product.productName}</h2>
                    <p>{product.productDescription}</p>
                    <p>{product.averageRating}</p>
                    <p>Price: ${product.productSalePrice.toFixed(2)}</p>
                  </div>
                )
              )}
          </div>
        </div>
      );




    


}