import CustomerReviewsList from '@/features/visits/Review/CustomerReviewsList';
import { NavBar } from '@/layouts/AppNavBar';

export default function ReviewsCustomer(): JSX.Element {
  return (
    <div>
      <NavBar />
      <CustomerReviewsList />
    </div>
  );
}
