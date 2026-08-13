import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ApiResponse } from '@/shared/models/ApiResponse';

/**
 *
 * @param {string} inventoryId - The ID of the inventory to generate the PDF for.
 */
const createPdf = async (inventoryId: string): Promise<ApiResponse<void>> => {
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

    return { data: undefined, errorMessage: null };
  } catch (error: unknown) {
    const maybeMsg = (error as { response?: { data?: { message?: unknown } } })
      .response?.data?.message;

    const errorMessage =
      typeof maybeMsg === 'string' && maybeMsg.trim()
        ? maybeMsg.trim()
        : 'Unable to create PDF. Please try again later.';

    return { data: null, errorMessage };
  }
};

export default createPdf;
