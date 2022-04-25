import type { RequestHandler } from '@sveltejs/kit';
import { serialize } from 'cookie';

const handler: RequestHandler<unknown> = async () => {
	// Invalidate token cookie
	return {
		headers: {
			'Set-Cookie': serialize('token', null, {
				path: '/',
				expires: new Date(0),
				httpOnly: true,
				sameSite: 'strict',
				secure: true
			})
		},
		body: {
			success: true
		}
	};
};

export const del = handler;
