async function login({
	email,
	password
}: {
	email: string;
	password: string;
}): Promise<LoginResponse> {
	const res = await fetch(`${process.env.API_URL}/login`, {
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
	const token = res.headers.get('Authorization');

	let message = '';
	if (res.status === 401 || res.status === 403) {
		message = 'Invalid login';
	} else if (res.status !== 200) {
		message = 'Something went wrong';
	} else {
		message = 'Success';
	}

	return {
		status: res.status,
		body,
		message,
		token
	};
}

async function logout(): Promise<Response> {
	return fetch('/logout', {
		method: 'DELETE'
	});
}

export default {
	login,
	logout
};
