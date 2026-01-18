export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  email: string;
  token: string;
}

export interface ErrorResponse {
  code: string;
  message: string;
  status: number;
  timestamp: string;
  details?: string;
}

export interface Seller {
  userId: string;
  name: string;
  profileImageUrl?: string;
}

export interface ListingMedia {
  url: string;
  type?: string;
}

export interface ListingAttribute {
  key: string;
  label: string;
  type: string;
  stringValue?: string;
  numberValue?: number;
  booleanValue?: boolean;
  enumLabel?: string;
}

export interface WaitingListing {
  publicId: string;
  title: string;
  description?: string;
  priceAmount: number;
  currency: string;
  locationCity?: string;
  seller: Seller;
  media?: ListingMedia[];
  attributes?: ListingAttribute[];
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface Category {
  id: number;
  name: string;
  parentId?: number | null;
  children?: Category[];
}

export interface CategoryAttribute {
  id?: number;
  key: string;
  label: string;
  type: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'ENUM';
  unit?: string | null;
  sortOrder?: number;
  active?: boolean;
  options?: AttributeOption[];
}

export interface AttributeOption {
  id?: number;
  value: string;
  label: string;
  sortOrder?: number;
}

export interface CreateCategoryRequest {
  name: string;
  parentId?: number | null;
}

export interface RejectRequest {
  reason: string;
}
