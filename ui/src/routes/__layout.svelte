<script context="module" lang="ts">
	import type { LoadInput } from '@sveltejs/kit';

	export async function load({ url, session }: LoadInput) {
		const { isLoggedIn }: { isLoggedIn: boolean } = session;

		if (url.pathname === '/login' || isLoggedIn === true) {
			return {
				status: 200
			};
		}

		return {
			status: 302,
			redirect: '/login'
		};
	}
</script>

<h1>Hihi</h1>
<slot />

<style global lang="postcss">
	@tailwind base;
	@tailwind components;
	@tailwind utilities;

	html {
		@apply h-screen;
	}

	body {
		@apply h-full;
	}
</style>
