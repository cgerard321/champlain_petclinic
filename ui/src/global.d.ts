/// <reference types="@sveltejs/kit" />
/// <reference types="svelte" />

interface DynamicSvelteComponent {
	component: SvelteComponent;
	props: Record<string, unknown>;
}

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
	renderComponent?: SvelteComponent | DynamicSvelteComponent;
}

interface Specialty {
	id: number;
	specialtyId: number;
	name: string;
}

interface Vet {
	specialties: Specialty[];
	vetId: number;
	firstName: string;
	lastName: string;
	email: string;
	phoneNumber: string;
	image: string | null;
	resume: string;
	workday: string;
	isActive: number;
}

interface Role {
	id: number;
	name: string;
	parent: Role;
}

interface User {
	id: number;
	username: string;
	email: string;
	roles: Role[];
}

interface GenericError {
	statusCode: number;
	message: string;
	timestamp: string;
}

interface NavItem {
	text: string;
	href: string;
}

interface LoginResponse {
	status: number;
	body: User | GenericError;
	message: string;
	token?: string;
}
interface Vet {
	vetId: number;
	firstName: string;
	lastName: string;
	email: string;
	phoneNumber: string;
	image: null;
	resume: string;
	workday: string;
	isActive: number;
	specialties: Specialty[];
}

interface Specialty {
	id: number;
	specialtyId: number;
	name: string;
}
