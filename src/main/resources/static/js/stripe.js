const stripe = Stripe('pk_test_51QXnln08B8D45f992wcmllQ9f7d9N38yAyLlhBK5gPodcjaJHO9SYO81TWtfncUQlwbGT3Gj4cSqlruygGYQUw9v00k0pMH3Rw');

document.addEventListener('DOMContentLoaded', function () {
	const paymentButton = document.querySelector('#paymentButton');

	paymentButton.addEventListener('click', (event) => {
		event.preventDefault(); // クリックしたリンクのデフォルト動作を防止
		
		stripe.redirectToCheckout({ sessionId: sessionId }).then(function (result) {
			if (result.error) {
				console.error(result.error.message);
			}
		});
	});
});