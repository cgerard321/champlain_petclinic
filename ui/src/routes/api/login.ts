import type { RequestHandler } from '@sveltejs/kit';
import { serialize } from 'cookie';

type PostBody = {
	email: string;
	password: string;
};

const postHandler: RequestHandler<Record<string, unknown>, PostBody> = async (req) => {
	const res = await fetch(`${process.env.API_URL}/login`, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({
			email: req.body.email,
			password: req.body.password
		})
	});

	const body = await res.json();

	if (res.status !== 200) {
		return {
			status: res.status,
			body
		};
	}

	const token = res.headers.get('Authorization');
	const expires =
		JSON.parse(Buffer.from(token.split('.')[1], 'base64').toString('utf8')).exp * 1000; // See JWS spec

	return {
		headers: {
			'Set-Cookie': serialize('token', token, {
				path: '/',
				expires: new Date(expires),
				httpOnly: true,
				sameSite: 'strict',
				secure: true
			})
		},
		body
	};
};

export const post = postHandler;
