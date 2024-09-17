import AddReviewForm from '@/features/visits/Review/AddReviewForm';
import { NavBar } from '@/layouts/AppNavBar';

export default function Reviews(): JSX.Element {
  return (
    <div>
      <NavBar />
      <h1>Let us a review</h1>
      <AddReviewForm />
    </div>
  );
}
