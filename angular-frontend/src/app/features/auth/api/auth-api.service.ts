import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  User,
  LoginRequest,
  SignupRequest,
  TokenResponse,
  ForgotPasswordRequest,
  ResetPasswordRequest
} from '../models/user.model';
import { ApiConfigService } from '../../../shared/api/api-config.service';

@Injectable({
  providedIn: 'root'
})
export class AuthApiService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfigService);

  
  login(credentials: LoginRequest): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(this.apiConfig.getFullUrl('/users/login', false), credentials);
  }

 
  signup(userData: SignupRequest): Observable<any> {
    return this.http.post(this.apiConfig.getFullUrl('/users', false), userData); 
  }

  
  logout(): Observable<void> {
    return this.http.post<void>(this.apiConfig.getFullUrl('/users/logout', false), {});
  }

  
  forgotPassword(request: ForgotPasswordRequest): Observable<void> {
    return this.http.post<void>(this.apiConfig.getFullUrl('/users/forgot_password', false), request);
  }

  
  resetPassword(request: ResetPasswordRequest): Observable<void> {
    return this.http.post<void>(this.apiConfig.getFullUrl('/users/reset_password', false), request);
  }

 
  verifyEmail(token: string): Observable<any> {
    return this.http.get<any>(this.apiConfig.getFullUrl(`/verification/${token}`, false));
  }

  
  getUserById(userId: string): Observable<User> {
    return this.http.get<User>(this.apiConfig.getFullUrl(`/users/${userId}`, false));
  }

  
  updateUser(userId: string, userData: Partial<User>): Observable<User> {
    return this.http.put<User>(this.apiConfig.getFullUrl(`/users/${userId}`, false), userData);
  }

  
  updateUserRole(userId: string, roles: string[]): Observable<User> {
    return this.http.patch<User>(this.apiConfig.getFullUrl(`/users/${userId}`, true), { roles });
  }

  
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.apiConfig.getFullUrl('/users', true));
  }

  
  deleteUser(userId: string): Observable<void> {
    return this.http.delete<void>(this.apiConfig.getFullUrl(`/users/${userId}`, false));
  }

  
  getUserDetails(userId: string): Observable<User> {
    return this.getUserById(userId);
  }

 
  getAllRoles(): Observable<any[]> {
    return this.http.get<any[]>(this.apiConfig.getFullUrl('/roles', true));
  }

  
  getRoleById(roleId: string): Observable<any> {
    return this.http.get<any>(this.apiConfig.getFullUrl(`/roles/${roleId}`, true));
  }

  
  addRole(roleData: any): Observable<any> {
    return this.http.post<any>(this.apiConfig.getFullUrl('/roles', true), roleData);
  }

  
  updateRole(roleId: string, roleData: any): Observable<any> {
    return this.http.patch<any>(this.apiConfig.getFullUrl(`/roles/${roleId}`, true), roleData);
  }

  createInventoryManager(managerData: any): Observable<any> {
    return this.http.post(this.apiConfig.getFullUrl('/users/inventoryManager', false), managerData);
  }
}

