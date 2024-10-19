export const exportVisitsCSV = async (): Promise<void> => {
  try {
    const response = await fetch(
      'http://localhost:8080/api/v2/gateway/visits/export',
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/octet-stream',
        },
        credentials: 'include',
      }
    );

    if (!response.ok) {
      throw new Error('Failed to download CSV file');
    }

    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);

    // Create a temporary anchor element to trigger the download
    const a = document.createElement('a');
    a.href = url;
    a.download = 'visits.csv';
    document.body.appendChild(a);
    a.click();

    // Clean up
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);

    alert('CSV file downloaded successfully!');
  } catch (error) {
    console.error('Error downloading CSV:', error);
    alert('Error downloading CSV file.');
  }
};
