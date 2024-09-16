import { getAllProducts } from '@/features/products/api/getAllProducts.ts';
import { useState, useEffect} from 'react';
import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel.ts';


export default function TrendingList(){
    const [trendingList, setTrendingList] = useState<ProductModel[]>([]);

    const fetchProducts = async (): Promise<void> => {
      const list = await getAllProducts();
      setTrendingList(list);
    };

    useEffect(() => {
        fetchProducts();
      }, []);

      const sortMostTrending = trendingList.sort((a,b) => b.requestCount - a.requestCount);

      const topThreeTrending = sortMostTrending.slice(0,4);


      return (
        <div>
          <div className="grid">
            {topThreeTrending
              .map(
                (product: {
                  productId: string;
                  productName: string;
                  productDescription: string;
                  productSalePrice: number;
                }) => (
                  <div className="card" key={product.productId}>
                    <h2>{product.productName}</h2>
                    <p>{product.productDescription}</p>
                    <p>Price: ${product.productSalePrice.toFixed(2)}</p>
                  </div>
                )
              )}
          </div>
        </div>
      );




    


}