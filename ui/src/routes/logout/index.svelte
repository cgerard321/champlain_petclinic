<script context="module" lang="ts">
	import { user } from '$lib/stores/auth';
	import authService from '$lib/services/auth';
	import { goto } from '$app/navigation';

	export async function load() {
		const { status } = await authService.logout();

		if (status < 400) {
			console.log('set user to null');

			user.set(null);
		}

		return {
			status,
			props: {
				isLoggedIn: false
			}
		};
	}
</script>

<script lang="ts">
	goto('/');
</script>

<p>Logging out...</p>
