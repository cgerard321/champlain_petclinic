/// <reference types="@sveltejs/kit" />
/// <reference types="svelte" />

interface TableColumn<T> {
	key: string;
	title: string;
	value: (v: T) => T[keyof T];
	class?: string;
	sortable?: boolean;
	searchValue?: (v: T) => unknown;
	filterOptions?: ((v: Thing[]) => Record<string, unknown>[]) | Y[];
	filterValue?: (v: T) => unknown;
	headerFilterClass?: string;
	renderValue?: (v: T) => T[keyof T];
	headerClass?: string;
	renderComponent?: SvelteComponent;
}
