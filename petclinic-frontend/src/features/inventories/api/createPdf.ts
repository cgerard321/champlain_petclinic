import axiosInstance from '@/shared/api/axiosInstance.ts';

/**
 *
 * @param {string} inventoryId - The ID of the inventory to generate the PDF for.
 */
const createPdf = async (inventoryId: string): Promise<void> => {
  try {
    // Send a GET request to the backend to get the PDF as a Blob
    const response = await axiosInstance.get(
      `inventories/${inventoryId}/products/download`,
      {
        responseType: 'blob', // Ensure the response is treated as a binary file (Blob)
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
    console.error('Error generating PDF:', error);
    alert('Failed to download PDF. Please try again later.');
  }
};

export default createPdf;
