import type { GetSession, Handle } from '@sveltejs/kit';
import { parse } from 'cookie';

const doHandle: Handle = async function handle({ request, resolve }) {
	const cookies = parse(request.headers['cookie'] ?? '');
	const token = cookies.token;
	const user = cookies.user ?? null;

	const hasTokenCookie: boolean = token !== undefined;
	request.locals['isLoggedIn'] = hasTokenCookie;
	request.locals['user'] = JSON.parse(user);

	return resolve(request);
};

const doGetSession: GetSession = async function getSession(request) {
	return {
		isLoggedIn: request.locals['isLoggedIn'],
		user: request.locals['user']
	};
};

export const handle = doHandle;
export const getSession = doGetSession;
