import { Promo } from '@/features/promos/models/PromoModel.tsx';
import { PromoCodeRequestModel } from '@/features/promos/models/PromoCodeRequestModel.tsx';
import axiosInstance from '@/shared/api/axiosInstance';

export class PromoApi {
  static async fetchPromos(): Promise<Promo[]> {
    try {
      const response = await axiosInstance.get('/promos');
      return response.data;
    } catch (error) {
      console.error('Error fetching promos:', error);
      throw error;
    }
  }

  static async fetchActivePromos(): Promise<Promo[]> {
    try {
      const response = await axiosInstance.get('/promos/actives');
      return response.data;
    } catch (error) {
      console.error('Error fetching active promos:', error);
      throw error;
    }
  }

  static async fetchPromoById(promoId: string): Promise<Promo> {
    try {
      const response = await axiosInstance.get('/promos/' + promoId);
      return response.data;
    } catch (error) {
      console.error('Error fetching promos by id:', error);
      throw error;
    }
  }

  static async updatePromo(
    promoId: string,
    updatedPromo: PromoCodeRequestModel
  ): Promise<void> {
    try {
      const response = await axiosInstance.put(
        `/promos/${promoId}`,
        updatedPromo
      );
      return response.data;
    } catch (error) {
      console.error('Error updating promos:', error);
      throw error;
    }
  }

  static async addPromo(newPromo: PromoCodeRequestModel): Promise<void> {
    try {
      const response = await axiosInstance.post(`/promos/`, newPromo);
      return response.data;
    } catch (error) {
      console.error('Error adding promos:', error);
      throw error;
    }
  }

  static async deletePromo(promoId: string): Promise<void> {
    try {
      const response = await axiosInstance.delete(`/promos/` + promoId);
      return response.data;
    } catch (error) {
      console.error('Error deleting promos:', error);
      throw error;
    }
  }
}
