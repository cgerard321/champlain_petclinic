import ReviewsList from '@/features/visits/Review/ReviewList';
import { NavBar } from '@/layouts/AppNavBar';

export default function Reviews(): JSX.Element {
  return (
    <div>
      <NavBar />
      <ReviewsList />
    </div>
  );
}
