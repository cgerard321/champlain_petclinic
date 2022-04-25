async function login({
	email,
	password
}: {
	email: string;
	password: string;
}): Promise<Omit<LoginResponse, 'token'>> {
	const res = await fetch('/login', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({
			email,
			password
		})
	});

	const body = await res.json();

	return body as Omit<LoginResponse, 'token'>;
}

export default {
	login
};
