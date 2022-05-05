<script context="module" lang="ts">
	import type { LoadInput, LoadOutput } from '@sveltejs/kit';

	export async function load({ fetch }: LoadInput): Promise<LoadOutput> {
		const res = await fetch('/api/vets');

		if (res.status >= 400) {
			return {
				status: res.status,
				error: await res.text()
			};
		}
		return {
			status: 200,
			props: { vets: await res.json() }
		};
	}
</script>

<script lang="ts">
	export let vets: Vet[] = [];
</script>

Length: {vets.length}
