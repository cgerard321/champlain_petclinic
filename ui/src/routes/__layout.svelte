<script context="module" lang="ts">
	import Nav from '$lib/components/Nav.svelte';
	import { user } from '$lib/stores/auth';

	import type { LoadInput } from '@sveltejs/kit';
	import { get } from 'svelte/store';

	export async function load({ url, session }: LoadInput) {
		const { isLoggedIn }: { isLoggedIn: boolean } = session;
		const hasUser = get(user) !== null;

		if (url.pathname === '/login' || isLoggedIn === true || hasUser === true) {
			return {
				status: 200,
				props: {
					isLoggedIn: isLoggedIn
				}
			};
		}

		return {
			status: 302,
			redirect: '/login'
		};
	}
</script>

<script lang="ts">
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
