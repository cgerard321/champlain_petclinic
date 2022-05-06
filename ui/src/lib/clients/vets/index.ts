export const del = async (vetId: Vet['vetId']): Promise<Response> => {
	return await fetch(`/api/vets/${vetId}`, {
		method: 'DELETE',
		headers: {
			'Content-Type': 'application/json',
			Accept: 'application/json'
		}
	});
};
