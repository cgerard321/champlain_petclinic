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
import { environment } from '../../../../environments/environment.dev';

@Injectable({
  providedIn: 'root'
})
export class AuthApiService {
  private readonly BASE_URL_V1 = `${environment.apiUrl}/users`; 
  private readonly BASE_URL_V2 = `${environment.apiUrlV2}/users`;
  private http = inject(HttpClient);

  
  login(credentials: LoginRequest): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.BASE_URL_V1}/login`, credentials);
  }

 
  signup(userData: SignupRequest): Observable<any> {
    return this.http.post(`${this.BASE_URL_V1}`, userData); 
  }

  
  logout(): Observable<void> {
    return this.http.post<void>(`${this.BASE_URL_V1}/logout`, {});
  }

  
  forgotPassword(request: ForgotPasswordRequest): Observable<void> {
    return this.http.post<void>(`${this.BASE_URL_V1}/forgot_password`, request);
  }

  
  resetPassword(request: ResetPasswordRequest): Observable<void> {
    return this.http.post<void>(`${this.BASE_URL_V1}/reset_password`, request);
  }

 
  verifyEmail(token: string): Observable<any> {
    return this.http.get<any>(`${environment.apiUrl}/verification/${token}`);
  }

  
  getUserById(userId: string): Observable<User> {
    return this.http.get<User>(`${this.BASE_URL_V1}/${userId}`);
  }

  
  updateUser(userId: string, userData: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.BASE_URL_V1}/${userId}`, userData);
  }

  
  updateUserRole(userId: string, roles: string[]): Observable<User> {
    return this.http.patch<User>(`${this.BASE_URL_V2}/${userId}`, { roles });
  }

  
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.BASE_URL_V2}`);
  }

  
  deleteUser(userId: string): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL_V1}/${userId}`);
  }

  
  getUserDetails(userId: string): Observable<User> {
    return this.getUserById(userId);
  }

 
  getAllRoles(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrlV2}/roles`);
  }

  
  getRoleById(roleId: string): Observable<any> {
    return this.http.get<any>(`${environment.apiUrlV2}/roles/${roleId}`);
  }

  
  addRole(roleData: any): Observable<any> {
    return this.http.post<any>(`${environment.apiUrlV2}/roles`, roleData);
  }

  
  updateRole(roleId: string, roleData: any): Observable<any> {
    return this.http.patch<any>(`${environment.apiUrlV2}/roles/${roleId}`, roleData);
  }

  createInventoryManager(managerData: any): Observable<any> {
    return this.http.post(`${this.BASE_URL_V1}/inventoryManager`, managerData);
  }
}

