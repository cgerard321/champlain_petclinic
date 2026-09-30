import axiosInstance from '@/shared/api/axiosInstance.ts';

export const exportVisitsCSV = async (): Promise<void> => {
  try {
    const response = await axiosInstance.get('/visits/export', {
      responseType: 'blob',
      useV2: false,
    });

    const url = window.URL.createObjectURL(new Blob([response.data]));

    const a = document.createElement('a');
    a.href = url;
    a.download = 'visits.csv';
    document.body.appendChild(a);
    a.click();

    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);

    alert('CSV file downloaded successfully!');
  } catch (error) {
    console.error('Error downloading CSV:', error);
    alert('Error downloading CSV file.');
  }
};
