import EditReviewForm from '@/features/visits/Review/EditReviewForm'; // Import the EditReviewForm component
import { NavBar } from '@/layouts/AppNavBar.tsx'; // Import the NavBar component

export default function UpdateReviewPage(): JSX.Element {
  return (
    <div>
      <NavBar />
      <h1>Update Review</h1>
      <EditReviewForm />
    </div>
  );
}
