<script lang="ts">
	export let row: unknown;
	export let col: unknown;

	export let onClick: <T>(row: T) => void = () => {};

	export let buttonClass: string = 'btn btn-sm btn-outline-secondary';
	export let text: string = 'Delete';
</script>

<button on:click={() => onClick(row)} class={buttonClass}>{text}</button>
