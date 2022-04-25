<script context="module" lang="ts">
	import Nav from '$lib/components/Nav.svelte';
	import { user as userStore } from '$lib/stores/auth';

	import type { LoadInput } from '@sveltejs/kit';
	import { get } from 'svelte/store';

	export async function load({ url, session }: LoadInput) {
		const { isLoggedIn, user }: { isLoggedIn: boolean; user: string } = session;
		let hasUser = get(userStore) !== null;

		if (!hasUser && user !== null && user !== undefined && user !== '') {
			userStore.set(JSON.parse(user) as User);
			hasUser = true;
		}

		if (url.pathname === '/login' || (isLoggedIn === true && hasUser === true)) {
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
	const user = get(userStore);

	const computedLoggedIn = user !== null || isLoggedIn;

	const pages: NavItem[] = [
		{
			text: 'Home',
			href: '/'
		}
	];
	const authPages: NavItem[] = [];

	if (computedLoggedIn) {
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
