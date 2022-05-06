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
	import Table from '$lib/components/Table.svelte';
	import DeleteButton from '$lib/components/DeleteButton.svelte';
	import { toast } from '@zerodevx/svelte-toast';
	import * as verClient from '$lib/clients/vets';

	export let vets: Vet[] = [];

	const columns: TableColumn<Vet>[] = [
		{
			key: 'name',
			title: 'Name',
			value: (vet) => `${vet.firstName} ${vet.lastName}`
		},
		{
			key: 'phoneNumber',
			title: 'Phone',
			value: (vet) => vet.phoneNumber
		},
		{
			key: 'email',
			title: 'Email',
			value: (vet) => vet.email
		},
		{
			key: 'specialties',
			title: 'Specialties',
			value: (vet) => vet.specialties.map((specialty) => specialty.name).join(', ')
		},
		{
			key: 'actions',
			title: 'Actions',
			value: (v) => v.vetId,
			renderComponent: {
				component: DeleteButton,
				props: {
					onClick(row: Vet) {
						verClient.del(row.vetId).then((n) => {
							if (n.status >= 400) {
								toast.push(`Unable to delete vet ${row.firstName} ${row.lastName}`);
								return;
							}

							const index = vets.findIndex((r) => r.vetId === row.vetId);
							if (index > -1) vets = [...vets.slice(0, index), ...vets.slice(index + 1)];

							toast.push(`${row.firstName} ${row.lastName} deleted successfully`, {
								classes: ['success'],
								duration: 2500,
								pausable: true
							});
						});
					},
					buttonClass: 'btn btn-sm btn-error'
				}
			}
		}
	];
</script>

{#if vets.length === 0}
	<p>No vets found.</p>
{:else}
	<div class="mx-20 mt-10">
		<Table
			classNameTable="table table-zebra"
			classNameThead="text-left bg-neutral rounded"
			classNameRow="hover children:text-primary-focus children:even:text-secondary-focus"
			{columns}
			rows={vets}
		/>
	</div>
{/if}
