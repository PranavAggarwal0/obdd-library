<script>
	let foo = '';
	let ord = '';
	let result = '';
	let show_info = false;

	async function doPost() {
		const res = await fetch('http://localhost:8080/build/expression', {
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

	function toggleInfo() {
		show_info = !show_info;
	}
</script>

<br />

{#if show_info}
	<p id="desc">
		OBDDs can be used to canonically represent boolean functions by means of a DAG. The value of the
		function can be found by making 'decisions' in the DAG at each node for the truth value of its
		corresponding variable. To maintain canonicity, the variables in the function are ordered, a
		variable cannot appear in the DAG after another variable that has a larger index than it.
	</p>

	<p id="inst">
		To create an OBDD, enter an ordering of variables (separated by spaces), and the formula you
		want to compute the OBDD for. The formula should use the connectives &&, ||, !, -> and
		parentheses.
	</p>

	<button type="button" on:click={toggleInfo}> Back </button>
{/if}

{#if !show_info}
	<p id="inst"><b>Build OBDD</b></p>
	<input bind:value={ord} placeholder="Enter Ordering" />
	<input bind:value={foo} placeholder="Enter Formula" />
	<button type="button" on:click={doPost}> Get OBDD </button>
	<button type="button" on:click={toggleInfo}> Info </button>
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
	#desc {
		position: relative;
		width: 800px;
		word-wrap: break-word;
	}
	#inst {
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
