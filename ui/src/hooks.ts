import type { Handle } from '@sveltejs/kit';
import { parse } from 'cookie';

const doHandle: Handle = async ({ request, resolve }) => {
	if (request.url.pathname === '/login' || request.url.pathname === '/register') {
		return resolve(request);
	}

	const cookie = request.headers.cookie;
	const pCookie = parse(cookie ?? '');
	if (pCookie.token) {
		request.headers['Authorization'] = `Bearer ${pCookie.token}`;
	}

	return resolve(request);
};

export const handle = doHandle;
