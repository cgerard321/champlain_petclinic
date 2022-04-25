async function login({
	email,
	password
}: {
	email: string;
	password: string;
}): Promise<LoginResponse> {
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

	let message = 'Success';
	if (res.status === 401 || res.status === 403) {
		message = 'Invalid login';
	} else if (res.status !== 200) {
		message = 'Something went wrong';
	}

	return {
		status: res.status,
		body,
		message
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
