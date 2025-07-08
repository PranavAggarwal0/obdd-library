<script>
	let foo = '';
	let ord = '';
	let result = '';
	let show_info = false;

	async function doPost() {
		const res = await fetch('http://localhost:8080/negation', {
			method: 'POST',
			mode: 'cors',
			headers: { Accept: 'image/svg+xml' },
			body: JSON.stringify({
				formula: foo,
				ordering: ord.split(' ')
			})
		});

		const data = await res.text();
		result = data;
	}

	function toggleShowInfo() {
		show_info = !show_info;
	}
</script>

{#if show_info}
	<p id="init">
		This page allows you to see how a node can be produced in the global DAG that represents the
		negation of another node that exists in the DAG (the formula for which you can enter). The input
		node is coloured in blue, and the output node is coloured red.
	</p>
	<button type="button" on:click={toggleShowInfo}> Back </button>
{/if}

{#if !show_info}
	<p id="init"><b>Negation</b></p>
	<br />
	<input bind:value={ord} placeholder="Enter Ordering" />
	<input bind:value={foo} placeholder="Enter Formula" />
	<button type="button" on:click={doPost}> Get OBDD </button>
	<button type="button" on:click={toggleShowInfo}> Info </button>
	<p><b>Result:</b></p>
	<pre id="image">
{@html result}
</pre>
{/if}

<style>
	input {
		position: relative;
		top: 40%;
		left: 16%;
		width: 50%;
	}
	button {
		position: relative;
		top: 45%;
		left: 16%;
	}
	#init {
		position: relative;
		width: 800px;
		word-wrap: break-word;
	}
	p {
		position: relative;
		top: 35%;
		left: 16%;
	}
	#image {
		position: relative;
		left: 16%;
		top: 2%;
	}
</style>
