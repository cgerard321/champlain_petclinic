import {Promo} from '@/features/promos/models/PromoModel.tsx';
import {PromoCodeRequestModel} from '@/features/promos/models/PromoCodeRequestModel.tsx';

export class PromoApi {
  private static BASE_URL = 'http://localhost:8080/api/v2/gateway/promos';
  // http://localhost:7008/api/v1/promos/actives
  static async fetchPromos(): Promise<Promo[]> {
    const response = await fetch(this.BASE_URL, {
      headers: {
        Accept: 'application/json',
      },
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(`Error: ${response.status} ${response.statusText}`);
    }

    return await response.json();
  }

  static async fetchActivePromos(): Promise<Promo[]> {
    const response = await fetch(`${this.BASE_URL}/actives`, {
      headers: {
        Accept: 'application/json',
      },
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(`Error: ${response.status} ${response.statusText}`);
    }

    return await response.json();
  }

  static async fetchPromoById(promoId: string): Promise<Promo> {
    const response = await fetch(`${this.BASE_URL}/${promoId}`, {
      headers: {
        Accept: 'application/json',
      },
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(`Error: ${response.status} ${response.statusText}`);
    }

    return await response.json();
  }

  static async updatePromo(
    promoId: string,
    updatedPromo: PromoCodeRequestModel
  ): Promise<void> {
    const response = await fetch(`${this.BASE_URL}/${promoId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
      body: JSON.stringify(updatedPromo),
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(
        `Failed to update promo: ${response.status} ${response.statusText}`
      );
    }
  }

  static async addPromo(newPromo: PromoCodeRequestModel): Promise<void> {
    const response = await fetch(this.BASE_URL, {
            method: 'POST',
            headers: {
      'Content-Type': 'application/json',
              Accept: 'application/json',
    },
    body: JSON.stringify(newPromo),
            credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(
              `Failed to add promo: ${response.status} ${response.statusText}`
      );
    }
  }

  static async deletePromo(promoCode: string): Promise<void> {
    const response = await fetch(`${this.BASE_URL}/${promoCode}`, {
      method: 'DELETE',
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(`Error deleting promo: ${response.status} ${response.statusText}`);
    }
  }

}
