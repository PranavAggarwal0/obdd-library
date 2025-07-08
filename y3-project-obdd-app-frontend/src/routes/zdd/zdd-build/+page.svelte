<script>
	let foo = '';
	let result = '';
	let ord = '';
	let show_info = false;

	async function doPost() {
		const res = await fetch('http://localhost:8080/zdd/build', {
			method: 'POST',
			mode: 'cors',
			headers: { Accept: 'image/svg+xml' },
			body: JSON.stringify({
				sets: foo.split('\n').map((line) => line.split(' ')),
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
		ZDDs are a variant of OBDDs, used to represent sets. These are especially useful when the sets
		that need to be represented are sparse, such as those appearing in the solutions of
		combinatorial problems. They are ordered as well, but follow different reduction rules than
		OBDDs. In ZDDs, nodes whose high edge points to the 0 terminal node are eliminated. <br />
		You can enter the sets separated by lines. For example, to get the ZDD for the set [[a,b], [c,d,e]],
		you can enter a,b on one line, then c,d,e on the next line.
	</p>

	<button type="button" on:click={toggleInfo}> Back </button>
{/if}

{#if !show_info}
	<p id="init"><b>Build ZDD</b></p>
	<br />
	<input bind:value={ord} placeholder="Enter Ordering" />
	<textarea bind:value={foo} placeholder="Enter Sets" />
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
