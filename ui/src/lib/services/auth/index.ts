async function login({
	email,
	password
}: {
	email: string;
	password: string;
}): Promise<[number, unknown, string]> {
	const res = await fetch('/login', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({ email, password })
	});

	let message = '';
	if (res.status === 401 || res.status === 403) {
		message = 'Invalid login';
	} else if (res.status !== 200) {
		message = 'Something went wrong';
	} else {
		message = 'Success';
	}

	return [res.status, await res.json(), message];
}

const toExport = {
	login
};

export default toExport;
