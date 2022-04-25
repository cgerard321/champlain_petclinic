<script context="module" lang="ts">
	import Nav from '$lib/components/Nav.svelte';

	import type { LoadInput } from '@sveltejs/kit';

	export async function load({ url, session }: LoadInput) {
		const { isLoggedIn, user }: { isLoggedIn: boolean; user: unknown } = session;

		if (url.pathname === '/login' || (isLoggedIn === true && user !== null)) {
			return {
				status: 200,
				props: {
					isLoggedIn: isLoggedIn
				}
			};
		}

		return {
			status: 307, // 307 is not cached while 301 and 302 are cached permanently and temporarily (respectively)
			redirect: '/login'
		};
	}
</script>

<script lang="ts">
	import { session } from '$app/stores';

	export let isLoggedIn: boolean = false;

	const pages: NavItem[] = [
		{
			text: 'Home',
			href: '/'
		}
	];
	const authPages: NavItem[] = [];

	if (isLoggedIn) {
		authPages.push({
			text: 'Logout',
			href: '/logout'
		});
	} else {
		authPages.push(
			{
				text: 'Login',
				href: '/login'
			},
			{
				text: 'Register',
				href: '/register'
			}
		);
	}
</script>

<Nav {pages} {authPages} />
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
