export interface User {
  id: number;
  email: string;
  isSuperadmin: boolean;
  isGlobalSuperAdmin: boolean;
  organizationId: number;
  createdAt: string;
}

export interface Organization {
  id: number;
  name: string;
  createdAt: string;
}

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  userId: number;
  email: string;
  isGlobalSuperAdmin: boolean;
}

export interface CreateUserRequest {
  email: string;
  password: string;
  organizationId: number;
}

export interface UpdateUserRequest {
  email?: string;
  password?: string;
}

export interface CreateOrganizationRequest {
  name: string;
}

export interface UpdateOrganizationRequest {
  name: string;
}