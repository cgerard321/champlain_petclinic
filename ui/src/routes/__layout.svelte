<script context="module" lang="ts">
	import { browser } from '$app/env';
	import { parse } from 'cookie';
	import type { LoadInput } from '@sveltejs/kit';

	export async function load({ url, session }: LoadInput) {
		const { isLoggedIn }: { isLoggedIn: boolean; user: unknown } = session;

		let allow: boolean = isLoggedIn;

		if (browser && parse(document?.cookie ?? '')?.user === undefined) {
			allow = false;
		}

		if (url.pathname === '/login' || allow === true) {
			return {
				status: 200,
				props: {
					isLoggedIn,
					currentPath: url.pathname
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
	import Nav from '$lib/components/Nav.svelte';

	let pages: NavItem[] = [
		{
			text: 'Home',
			href: '/'
		},
		{
			text: 'Vets',
			href: '/vets'
		}
	];

	let authPages: NavItem[] = [];

	session.subscribe((store) => {
		if (store.isLoggedIn === true) {
			authPages = [
				{
					text: 'Logout',
					href: '/logout'
				}
			];
		} else {
			authPages = [
				{
					text: 'Login',
					href: '/login'
				},
				{
					text: 'Register',
					href: '/register'
				}
			];
		}
	});
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
