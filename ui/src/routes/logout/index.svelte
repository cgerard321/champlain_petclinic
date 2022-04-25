<script context="module" lang="ts">
	import { browser } from '$app/env';
	import { session as sessionStore } from '$app/stores';

	import authClient from '$lib/clients/auth';

	import type { LoadInput } from '@sveltejs/kit';

	export async function load({}: LoadInput) {
		const { status } = await authClient.logout();

		if (status < 400) {
			if (browser) {
				sessionStore.update((store) => ({
					...store,
					isLoggedIn: false,
					user: null
				}));
			}

			return {
				status: 307,
				redirect: '/'
			};
		}

		return {
			status
		};
	}
</script>

<script lang="ts">
	import { goto } from '$app/navigation';

	sessionStore.subscribe(({ isLoggedIn, user }) => {
		if (!browser) return;
		if (!isLoggedIn) goto('/');
	});
</script>

{#if sessionStore.isLoggedIn}
	<p>Unable to logout</p>
{:else}
	<p>Logging out...</p>
{/if}
