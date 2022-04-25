<script context="module" lang="ts">
	import authClient from '$lib/clients/auth';
	import { goto } from '$app/navigation';
	import type { LoadInput } from '@sveltejs/kit';

	export async function load({ session }: LoadInput) {
		const { status } = await authClient.logout();

		if (status < 400) {
			session['user'] = null;
			session['isLoggedIn'] = false;
		}

		return {
			status
		};
	}
</script>

<script lang="ts">
	window.location.href = '/';
</script>

<p>Logging out...</p>
