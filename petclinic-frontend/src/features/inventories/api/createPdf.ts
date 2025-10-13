import axiosInstance from '@/shared/api/axiosInstance.ts';
import axios from 'axios';

/**
 *
 * @param {string} inventoryId - The ID of the inventory to generate the PDF for.
 */
const createPdf = async (inventoryId: string): Promise<void> => {
  try {
    // Send a GET request to the backend to get the PDF as a Blob
    const response = await axiosInstance.get(
      `/inventories/${inventoryId}/products/download`,
      {
        responseType: 'blob', // Ensure the response is treated as a binary file (Blob)
        useV2: false,
      }
    );

    // Create a blob URL for the PDF
    const url = window.URL.createObjectURL(new Blob([response.data]));

    // Create a link element to trigger the download
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `Inventory_${inventoryId}.pdf`); // Set file name

    // Append the link to the document and programmatically click it to trigger download
    document.body.appendChild(link);
    link.click();

    // Cleanup: remove the link and revoke the object URL
    link.parentNode?.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (error) {
    if (!axios.isAxiosError(error)) {
      throw error instanceof Error ? error : new Error('Failed to create PDF');
    }

    console.error('[createPdf]', {
      url: (error.config?.baseURL || '') + (error.config?.url || ''),
      method: (error.config?.method || '').toUpperCase(),
      status: error.response?.status,
      dataReceived: error.response?.data,
    });

    const status = error.response?.status ?? 0;
    const message =
      status === 400
        ? 'Invalid inventory ID. Please check and try again.'
        : status === 404
          ? 'Inventory or supplies not found.'
          : status === 422
            ? 'No products available to generate PDF.'
            : 'Failed to create PDF. Please try again later.';
    throw new Error(message);
  }
};

export default createPdf;
