<script context="module" lang="ts">
	import { user } from '$lib/stores/auth';
	import authClient from '$lib/clients/auth';
	import { goto } from '$app/navigation';

	export async function load() {
		const { status } = await authClient.logout();

		if (status < 400) {
			console.log('user set to null');
			user.set(null);
		}

		return {
			status
		};
	}
</script>

<script lang="ts">
	goto('/login');
</script>

<p>Logging out...</p>
