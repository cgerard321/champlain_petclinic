<script lang="ts">
	import { goto } from '$app/navigation';
	import { user } from '$lib/stores/auth';
	import authClient from '$lib/clients/auth';

	import { createForm } from 'svelte-forms-lib';
	import * as yup from 'yup';

	let status: {
		message: string;
		isError: boolean;
	} = null;
	const { form, errors, handleChange, handleSubmit } = createForm({
		initialValues: {
			email: '',
			password: ''
		},
		validationSchema: yup.object().shape({
			email: yup.string().required(),
			password: yup.string().required()
		}),
		onSubmit: async (values: { email: string; password: string }) => {
			const { status: statusCode, body, message } = await authClient.login(values);
			const isError = statusCode > 399;

			if (isError === false) {
				user.set(body as User);
			}

			status = {
				isError,
				message
			};
		}
	});

	$: {
		if (status?.isError === false) {
			// redirect to home
			goto('/');
		}
	}
</script>

<div class="flex flex-col justify-center m-0 relative top-1/2 -translate-y-1/2">
	<div class="flex justify-center">
		<div class="w-1/6">
			<form
				class="flex flex-col break-words bg-base-200 border-base-200 border-2 rounded shadow-md md:m-h-[36vh]"
				on:submit|preventDefault={handleSubmit}
			>
				<div class="font-semibold bg-base-300 text-primary-focus py-3 px-6 mb-0">Login</div>

				<div class="p-6 flex-grow flex flex-col justify-evenly">
					{#if status}
						<div class="text-center mb-4" class:text-error={status.isError}>
							{status.message}
						</div>
					{/if}
					<div class="form-control">
						<label class="label-form" for="email">
							<span>Email</span>
							<input
								placeholder="email"
								class="input-form"
								type="text"
								name="email"
								on:change={handleChange}
								bind:value={$form.email}
							/>
						</label>
						{#if $errors.email}
							<div class="error-message">{$errors.email}</div>
						{/if}
					</div>

					<div class="form-control mt-6">
						<label class="label-form" for="password">
							<span>Password</span>
							<input
								placeholder="password"
								class="input-form"
								type="password"
								name="password"
								on:change={handleChange}
								bind:value={$form.password}
							/>
						</label>
						{#if $errors.password}
							<div class="error-message">{$errors.password}</div>
						{/if}
					</div>

					<button class="btn btn-primary mt-6">Login</button>
				</div>
			</form>
		</div>
	</div>
</div>

<style lang="postcss">
	.label-form {
		@apply input-group input-group-vertical input-group-md text-secondary-focus;
	}

	.input-form {
		@apply input input-bordered input;
	}

	.error-message {
		@apply text-error text-sm italic mt-2;
	}
</style>
