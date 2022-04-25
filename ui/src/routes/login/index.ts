import type { RequestHandler } from '@sveltejs/kit';
import { serialize } from 'cookie';
import authService from '$lib/services/auth';

type PostBody = {
	email: string;
	password: string;
};

const postHandler: RequestHandler<Record<string, unknown>, PostBody> = async (req) => {
	const { status, token, body } = await authService.login({
		email: req.body.email,
		password: req.body.password
	});

	if (status !== 200 || token === null || token === undefined || token === '') {
		return {
			status,
			body
		};
	}

	const expires =
		JSON.parse(Buffer.from(token.split('.')[1], 'base64').toString('utf8')).exp * 1000; // See JWS spec

	return {
		status,
		headers: {
			'Set-Cookie': [
				serialize('token', token, {
					path: '/',
					expires: new Date(expires),
					httpOnly: true,
					sameSite: 'strict',
					secure: true
				}),
				serialize('user', JSON.stringify(body), {
					path: '/',
					expires: new Date(expires),
					httpOnly: false,
					sameSite: 'strict'
				})
			]
		},
		body
	};
};

export const post = postHandler;
