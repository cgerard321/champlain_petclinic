import type { GetSession, Handle } from '@sveltejs/kit';
import { parse } from 'cookie';

const whiteListRoutes = ['/login', '/register', '/logout'];

const doHandle: Handle = async function handle({ request, resolve }) {
	const hasTokenCookie: boolean = parse(request.headers['cookie'] ?? '').token !== undefined;
	request.locals['isLoggedIn'] = hasTokenCookie;

	if (whiteListRoutes.includes(request.url.pathname)) {
		return resolve(request);
	}

	return resolve(request);
};

const doGetSession: GetSession = async function getSession(request) {
	return {
		isLoggedIn: request.locals['isLoggedIn']
	};
};

export const handle = doHandle;
export const getSession = doGetSession;
