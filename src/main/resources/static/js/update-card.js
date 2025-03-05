document.getElementById('updateCardButton').addEventListener('click', function(event) {
    event.preventDefault();
    var userId = this.getAttribute('data-user-id');

    fetch('/custom/save-card', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ userId: userId })
    })
    .then(function(response) {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.text();
    })
    .then(function(data) {
        console.log('Response from custom endpoint:', data);

        fetch('/create-update-card-session?userId=' + userId)
            .then(function(response) {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.text();
            })
            .then(function(sessionId) {
                if (sessionId) {
                    var stripe = Stripe('pk_test_51QXnln08B8D45f992wcmllQ9f7d9N38yAyLlhBK5gPodcjaJHO9SYO81TWtfncUQlwbGT3Gj4cSqlruygGYQUw9v00k0pMH3Rw');
                    return stripe.redirectToCheckout({ sessionId: sessionId });
                } else {
                    alert('Something went wrong. Please try again.');
                }
            })
            .catch(function(error) {
                console.error('Error:', error);
                alert('Error creating update card session: ' + error.message);
            });
    })
    .catch(function(error) {
        console.error('Error:', error);
        alert('Error saving card: ' + error.message);
    });
});
