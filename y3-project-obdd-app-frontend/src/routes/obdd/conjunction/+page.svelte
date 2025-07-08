<script>
	let foo = '';
	let bar = '';
	let ord = '';
	let result = null;
	let show_info = false;

	async function doPost() {
		const res = await fetch('http://localhost:8080/conjunction', {
			method: 'POST',
			mode: 'cors',
			headers: { Accept: 'image/svg+xml' },
			body: JSON.stringify({
				formula1: foo,
				formula2: bar,
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
		Nodes in an OBDD can be combined in different ways to produce different nodes. This page allows
		you to see how two nodes (the formulas for which you can enter) can be combined to produce a
		third node that represents the conjunction of the two nodes. The two input nodes are coloured in
		blue, and the output node is coloured red.
	</p>
	<button type="button" on:click={toggleShowInfo}> Back </button>
{/if}

{#if !show_info}
	<p id="inst"><b>Conjunction</b></p>
	<input bind:value={ord} placeholder="Enter Ordering" />
	<input bind:value={foo} placeholder="Enter Formula 1" />
	<input bind:value={bar} placeholder="Enter Formula 2" />
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
