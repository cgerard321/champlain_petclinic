import { UserList } from './pages/user-list/user-list';
import routes from './routes';

describe('Users routes', () => {
  it('should have the user list route', () => {
    // Assert
    expect(routes[0]?.component).toBe(UserList);
  });
});
