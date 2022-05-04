<script lang="ts">
	import { page as activePage } from '$app/stores';
	export let pages: NavItem[] = [];
	export let authPages: NavItem[] = [];
</script>

<nav class="navbar mb-2 shadow-lg bg-neutral text-neutral-content rounded-box sm:rounded-none">
	<span class="text-3xl text-primary-focus">PetClinic</span>
	<div class="flex flex-col w-full">
		<ul class="flex gap-4 px-4 w-full">
			{#each pages as page}
				<li class="flex">
					<a href={page.href} class="text-base text-primary-focus"
						>{page.text}
						{#if page.href === $activePage.url.pathname}
							<span class="text-primary-focus"> (current)</span>
						{/if}
					</a>
				</li>
			{/each}

			{#each authPages as page, i}
				<li class:ml-auto={i === 0}>
					<a href={page.href} class="text-base text-primary-focus">{page.text}</a>
				</li>
			{/each}
		</ul>
	</div>
</nav>
