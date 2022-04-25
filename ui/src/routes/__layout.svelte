<script context="module" lang="ts">
	import Nav from '$lib/components/Nav.svelte';

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

<Nav />
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
