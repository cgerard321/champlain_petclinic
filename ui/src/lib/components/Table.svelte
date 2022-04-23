<script lang="ts">
	import SvelteTable from 'svelte-table';

	export let columns: object[];
	export let rows: object[];
	export let sortOrders: number[] = [1, -1];
	// READ AND WRITE
	export let sortBy: string = '';
	export let sortOrder: number = sortOrders?.[0] || 1;
	export let filterSelections: Object = {};
	// expand
	export let expanded: (string | number)[] = [];

	// READ ONLY
	export let expandRowKey: string = null;
	export let expandSingle: boolean = false;
	export let iconAsc: string = '▲';
	export let iconDesc: string = '▼';
	export let iconSortable: string = '';
	export let iconExpand: string = '▼';
	export let iconExpanded: string = '▲';
	export let showExpandIcon: boolean = false;
	export let classNameTable: string = '';
	export let classNameThead: string = '';
	export let classNameTbody: string = '';
	export let classNameSelect: string = '';
	export let classNameInput: string = '';
	export let classNameRow: string = '';
	export let classNameCell: string = '';
	export let classNameRowExpanded: string = '';
	export let classNameExpandedContent: string = '';
	export let classNameCellExpand: string = '';
</script>

<SvelteTable
	{columns}
	{rows}
	{sortBy}
	{sortOrder}
	{sortOrders}
	{iconAsc}
	{iconDesc}
	{iconSortable}
	{iconExpand}
	{iconExpanded}
	{classNameTable}
	{classNameThead}
	{classNameTbody}
	{classNameSelect}
	{classNameInput}
	{classNameRow}
	{classNameRowExpanded}
	{classNameExpandedContent}
	{classNameCell}
	{classNameCellExpand}
	{expanded}
	{expandSingle}
	{expandRowKey}
	{filterSelections}
	{showExpandIcon}
/>
