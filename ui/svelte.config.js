import adapter from '@sveltejs/adapter-auto';
import preprocess from 'svelte-preprocess';
import { parse } from 'cookie';

/** @type {import('@sveltejs/kit').Config} */
const config = {
	// Consult https://github.com/sveltejs/svelte-preprocess
	// for more information about preprocessors
	preprocess: preprocess(),

	kit: {
		adapter: adapter(),

		// hydrate the <div id="svelte"> element in src/app.html
		target: '#svelte',

		vite: {
			server: {
				proxy: {
					'/proxy': {
						target: process.env.API_URL,
						changeOrigin: true,
						rewrite: (path) => path.replace(/^\/proxy/, '/api/gateway'),
						configure: (proxy) => {
							function getAuthHeader(req) {
								const cookie = parse(req.headers.cookie ?? '');

								if (!cookie.token) return {};
								return {
									Authorization: `Bearer ${cookie.token}`
								};
							}

							proxy.on('proxyReq', (proxyReq, req, res, options) => {
								const authHeader = getAuthHeader(req).Authorization;
								if (authHeader) {
									proxyReq.setHeader('Authorization', authHeader);
								}
							});
						}
					}
				}
			}
		}
	}
};

export default config;
