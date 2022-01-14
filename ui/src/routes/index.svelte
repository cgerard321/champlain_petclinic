<script lang="ts">
	import { createForm } from 'svelte-forms-lib';
	import * as yup from 'yup';

	let status: {
		message: string;
		isError: boolean;
	} = null;
	const { form, errors, handleChange, handleSubmit, isSubmitting } = createForm({
		initialValues: {
			email: '',
			password: ''
		},
		validationSchema: yup.object().shape({
			email: yup.string().required(),
			password: yup.string().required()
		}),
		onSubmit: async (values: { email: string; password: string }) => {
			const [statusCode, body] = await doSubmit(values);
			if (statusCode === 401 || statusCode === 403) {
				status = { isError: true, message: 'Invalid login' };
			} else if (statusCode !== 200) {
				status = { isError: true, message: 'Something went wrong' };
			} else {
				status = { isError: false, message: 'Success' };
			}
		}
	});

	async function doSubmit({
		email,
		password
	}: {
		email: string;
		password: string;
	}): Promise<[number, any]> {
		const res = await fetch('/api/login', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({ email, password })
		});
		return [res.status, await res.json()];
	}
</script>

<div class="flex flex-col h-full justify-center">
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
