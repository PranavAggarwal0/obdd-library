<script>
	let foo = '';
	let bar = '';
	let ord = '';
	let result = null;
	let show_info = false;

	async function doPost() {
		const res = await fetch('http://localhost:8080/zdd/difference', {
			method: 'POST',
			mode: 'cors',
			headers: { Accept: 'image/svg+xml' },
			body: JSON.stringify({
				zdd1: foo.split('\n').map((line) => line.split(' ')),
				zdd2: bar.split('\n').map((line) => line.split(' ')),
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

{#if show_info}
	<p id="init">
		ZDDs represent sets of sets. Taking the 'difference' of two ZDDs should therefore yield a ZDD
		that represents the difference between the two sets. This is exactly what this page allows you
		to do. If for example you enter the sets [[a], [b], [c]] and [[b]], you will get a ZDD that
		represents [[a], [c]].
	</p>

	<button type="button" on:click={toggleInfo}> Back </button>
{/if}

{#if !show_info}
	<p id="init"><b>Difference</b></p>
	<br />
	<input bind:value={ord} placeholder="Enter Ordering" />
	<textarea bind:value={foo} /> <textarea bind:value={bar} />
	<button type="button" on:click={doPost}> Get ZDD </button>
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
	textarea {
		position: relative;
		top: 40%;
		left: 16%;
		width: 50%;
	}
	button {
		position: relative;
		top: 40%;
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
